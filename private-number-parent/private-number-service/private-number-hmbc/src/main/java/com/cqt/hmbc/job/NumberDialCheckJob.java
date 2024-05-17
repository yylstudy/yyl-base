package com.cqt.hmbc.job;

import com.alibaba.fastjson.JSON;
import com.cqt.common.enums.DialTestTypeEnum;
import com.cqt.hmbc.retry.RetryPushDTO;
import com.cqt.hmbc.service.CorpPushService;
import com.cqt.hmbc.service.PrivateDialTestTaskRecordDetailsService;
import com.cqt.hmbc.service.PrivateDialTestTaskRecordService;
import com.cqt.hmbc.service.PrivateDialTestTimingConfService;
import com.cqt.hmbc.service.impl.RedisService;
import com.cqt.hmbc.util.RestTemplateRequest;
import com.cqt.model.hmbc.dto.CommandEntity;
import com.cqt.model.hmbc.dto.DialTestNumberDTO;
import com.cqt.model.hmbc.dto.HmbcTaskInfo;
import com.cqt.model.hmbc.entity.PrivateDialTestTaskRecordDetails;
import com.cqt.model.hmbc.entity.PrivateDialTestTimingConf;
import com.cqt.model.hmbc.properties.HmbcProperties;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * 定时小号拨测 任务
 *
 * @author Xienx
 * @date 2022年07月28日 15:37
 */
@Slf4j
@Component
public class NumberDialCheckJob extends CommonJob {
    private final RestTemplate restTemplate;

    public NumberDialCheckJob(RedisService redisService,
                              HmbcProperties hmbcProperties,
                              CorpPushService corpPushService,
                              PrivateDialTestTimingConfService timingConfService,
                              PrivateDialTestTaskRecordService taskRecordService,
                              PrivateDialTestTaskRecordDetailsService taskRecordDetailsService, RestTemplate restTemplate) {
        super(redisService, hmbcProperties, corpPushService, timingConfService, taskRecordService, taskRecordDetailsService);
        this.restTemplate = restTemplate;
    }

    @XxlJob("numberCheck")
    public void numberCheck() {
        long startTime = System.currentTimeMillis();
        log.info("=>=> jobId: {}, 号码外呼拨测任务开始执行", XxlJobHelper.getJobId());

        String jobKey = String.format(HMBC_JOB_LOCK_KEY, XxlJobHelper.getJobId());
        // 这里做互斥处理
        if (!redisService.tryLock(jobKey, JOB_LOCK_EXPIRED_TIME)) {
            log.info("<=<= jobId: {}, 号码外呼拨测任务执行中, 忽略本次", XxlJobHelper.getJobId());
            XxlJobHelper.handleFail("上一轮调度任务还未结束");
            return;
        }
        int totalCount = 0;
        // 2、先查询该jobId对应的定时隐私号拨测任务配置
        PrivateDialTestTimingConf timingConf = getTimingConfByJobId();
        if (timingConf == null) {
            XxlJobHelper.handleFail("未查询到对应的拨测计划配置");
            return;
        }
        try {
            List<DialTestNumberDTO> vccNumbers = getConfigNumberList(timingConf);

            // 这里获取待执行的号码信息
            List<DialTestNumberDTO> execNumbers = getExecNumbers(vccNumbers);
            if (execNumbers.isEmpty()) {
                // 在没有查询到拨测号码时, 则认为该次拨测任务结束
                log.info("<=<= jobId=>{}, vccId=>{}, 未查询到有效拨测号码, 本次号码外呼拨测任务结束", timingConf.getJobId(), timingConf.getVccId());
                XxlJobHelper.handleFail("未查询到有效拨测号码");
                taskRecordService.emptyTaskRecord(timingConf);
                return;
            }
            totalCount = execNumbers.size();

            HmbcTaskInfo taskInfo = writeCacheContext(timingConf, execNumbers);

            // 这里进行号码拨测
            commandRequestBatchWithRetry(taskInfo);
            XxlJobHelper.handleSuccess("定时隐私号拨测执行成功");
        } catch (Exception e) {
            log.error("定时隐私号拨测执行失败: ", e);
            XxlJobHelper.handleFail("定时隐私号拨测执行失败");
        } finally {
            redisService.releaseLock(jobKey);
            log.info("<=<= jobId=>{}, vccId:{}, 本次待拨测号码数量:{}, 定时隐私号拨测执行完成, 累计耗时:{} ms",
                    timingConf.getJobId(), timingConf.getVccId(), totalCount, System.currentTimeMillis() - startTime);
        }
    }


    /**
     * 将当前拨测任务执行配置写入缓存
     *
     * @param timingConf 定时拨测计划配置
     * @param execNums   执行号码信息
     * @return HmbcTaskInfo 号码拨测信息
     */
    protected HmbcTaskInfo writeCacheContext(PrivateDialTestTimingConf timingConf, List<DialTestNumberDTO> execNums) {
        // 这里将任务ID 与任务上下文信息存放到redis中
        HmbcTaskInfo taskInfo = startTask(timingConf, execNums);

        String configInfoKey = String.format(HMBC_OUTBOUND_CALL_CONFIG_KEY, taskInfo.getVccId());
        redisService.setStr(configInfoKey, JSON.toJSONString(taskInfo), Duration.ofDays(1));

        // 这里将前置校验异常的号码数量保存到redis
        String failCountKey = String.format(HMBC_OUTBOUND_CALL_EXECUTING_COUNT_KEY, taskInfo.getTaskRecordId());
        redisService.setObj(failCountKey, taskInfo.getTotalCount(), Duration.ofDays(1));

        return taskInfo;
    }

    private void commandRequestBatchWithRetry(HmbcTaskInfo execInfo) {
        int interval = hmbcProperties.getDialTest().getInterval();
        int maxAttempts = hmbcProperties.getDialTest().getMaxAttempts();
        log.info("{}, 定时拨测隐私号执行开始", execInfo);
        long startMill = System.currentTimeMillis();

        List<DialTestNumberDTO> numberInfos = execInfo.getNumberInfos();

        try {
            List<DialTestNumberDTO> lastFailNums = doWithRetry(numberInfos, maxAttempts, interval, this::commandRequestBatch);
            if (!lastFailNums.isEmpty()) {
                // 先转换成失败的号码信息
                List<PrivateDialTestTaskRecordDetails> failRecords = lastFailNums.stream()
                        .map(e -> detailsRecordBuild(e, execInfo, "拨测失败"))
                        .collect(Collectors.toList());
                // 先对拨测失败的结果进行处理
                handleFailResult(failRecords);
                taskRecordDetailsService.saveBatch(failRecords);

                // 如果该企业的号码全部拨测失败, 那么就将相应的拨测结果推送给企业
                if (failRecords.size() == numberInfos.size()) {
                    log.info("{}, 本次定时拨测隐私号全部失败, 现在开始推送企业", execInfo);
                    taskRecordService.taskRecordFinish(execInfo.getTaskRecordId(), 0, failRecords.size());
                }
                if (execInfo.isPushRequired()) {
                    pushFailRecord(execInfo, failRecords);
                }
            }
        } finally {
            log.info("{}, 定时拨测隐私号执行完成, 累计耗时: {} ms", execInfo, System.currentTimeMillis() - startMill);
        }
    }


    private List<DialTestNumberDTO> commandRequestBatch(List<DialTestNumberDTO> numberInfos) {
        List<DialTestNumberDTO> failNums = new CopyOnWriteArrayList<>();
        String url = hmbcProperties.getDialTest().getExecCommandUrl();
        int timeout = hmbcProperties.getDialTest().getTimeout();
        for (DialTestNumberDTO numberInfo : numberInfos) {
            CommandEntity command = getCommandEntity(numberInfo);
            String json = JSON.toJSONString(command);
            try {
                RestTemplateRequest.of(restTemplate)
                        .post(url)
                        .body(json)
                        .timeout(timeout)
                        .execute();
                // 这里执行完呼叫命令后, 等待一定时间间隔, 防止fs并发过高出现异常
                Thread.sleep(hmbcProperties.getDialTest().getWaitInterval());
            } catch (Exception e) {
                log.error("拨测号码: {}, 请求URL: {}, HTTP请求出现异常: ", numberInfo.getNumber(), url, e);
                failNums.add(numberInfo);
            }
        }
        return failNums;
    }



    /**
     * @param taskInfo 企业拨测配置
     * @param failList 拨测失败列表
     */
    private void pushFailRecord(HmbcTaskInfo taskInfo, List<PrivateDialTestTaskRecordDetails> failList) {
        List<RetryPushDTO> retryPushDTOList = convert2PushDTO(taskInfo, failList);
        corpPushService.pushBatch(retryPushDTOList);
    }


    /**
     * 将拨测号码信息转换成对应的拨测命令
     *
     * @param numberInfo 拨测号码信息
     * @return CommandEntity 拨测命令
     */
    private CommandEntity getCommandEntity(DialTestNumberDTO numberInfo) {
        List<String> callerNumbers = hmbcProperties.getDialTest().getCallerNumbers();
        String callee = numberInfo.getNumber();
        String vccId = numberInfo.getVccId();

        int seed = ThreadLocalRandom.current().nextInt(callerNumbers.size());
        String callerNum = callerNumbers.get(seed);

        String luaParam = String.format(hmbcProperties.getDialTest().getLuaParamFormat(), callerNum, callee, vccId);

        CommandEntity commandEntity = new CommandEntity();
        commandEntity.setOneMsg("fs_cli");
        commandEntity.setTwoMsg("-x");

        commandEntity.setThreeMsg(luaParam);

        return commandEntity;
    }

    @Override
    DialTestTypeEnum getDialTestType() {
        return DialTestTypeEnum.DIAL_TEST;
    }
}
