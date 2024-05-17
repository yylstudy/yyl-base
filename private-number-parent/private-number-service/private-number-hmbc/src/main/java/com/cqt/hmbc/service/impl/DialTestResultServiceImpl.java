package com.cqt.hmbc.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.cqt.common.constants.HmbcConstants;
import com.cqt.common.constants.PrivateCacheConstant;
import com.cqt.common.enums.DialTestTypeEnum;
import com.cqt.common.enums.ReleaseCauseEnum;
import com.cqt.hmbc.config.RabbitMqConfig;
import com.cqt.hmbc.config.ThreadPoolConfig;
import com.cqt.hmbc.handler.MqProducer;
import com.cqt.hmbc.retry.RetryPushDTO;
import com.cqt.hmbc.retry.RetryQueryDTO;
import com.cqt.hmbc.service.*;
import com.cqt.hmbc.util.RestTemplateRequest;
import com.cqt.model.hmbc.dto.CdrRecordSimpleEntity;
import com.cqt.model.hmbc.dto.CommandEntity;
import com.cqt.model.hmbc.dto.HmbcTaskInfo;
import com.cqt.model.hmbc.entity.PrivateDialTestTaskRecordDetails;
import com.cqt.model.hmbc.properties.HmbcProperties;
import com.cqt.model.hmbc.vo.HmbcResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * DialTestResultServiceImpl
 *
 * @author Xienx
 * @date 2023年02月09日 17:53
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DialTestResultServiceImpl implements DialTestResultService, PrivateCacheConstant {

    private final MqProducer mqProducer;
    private final RestTemplate restTemplate;
    private final RedisService redisService;
    private final HmbcProperties hmbcProperties;
    private final CorpPushService corpPushService;
    private final AcrRecordService acrRecordService;
    private final PrivateDialTestTaskRecordService taskRecordService;
    private final PrivateDialTestTaskRecordDetailsService recordDetailsService;


    @Async(ThreadPoolConfig.COMMON_EXECUTOR)
    @Override
    public void dialTestResult(CdrRecordSimpleEntity cdrRecord) {
        String gtCode = acrRecordService.getGtCodeByNumber(cdrRecord.getCalledNum());
        // 根据号码的GT, 得到数据源配置
        Map<String, String> gtDsMap = hmbcProperties.getDialTest().getGtDsMap();
        String dsName = gtDsMap.get(gtCode);

        RetryQueryDTO retryQueryDTO = new RetryQueryDTO();
        retryQueryDTO.setDsName(dsName);
        retryQueryDTO.setCdrRecord(cdrRecord);

        //判断是否上平台了, 关机、停机情况下不会有话单产生
        if (!checkReleaseCause(cdrRecord)) {
            // 这里则认为该号码未上平台
            dialTestResultSaveAndPush(cdrRecord, false);
            return;
        }
        retryHandle(retryQueryDTO);
    }

    /**
     * 这里将拨测详情保存入库并推送给企业
     *
     * @param cdrRecord 话单数据
     * @param isSuccess 是否拨测成功
     */
    private void dialTestResultSaveAndPush(CdrRecordSimpleEntity cdrRecord, boolean isSuccess) {

        // 从拨测结果里取出号码信息
        String number = cdrRecord.getCalledNum();

        // 根据企业vccId, 关联查询出本次执行任务Id
        String hmbcConfigKey = String.format(HMBC_OUTBOUND_CALL_CONFIG_KEY, cdrRecord.getVccId());
        String hmbcConfigJson = redisService.getStr(hmbcConfigKey);
        // 如果没有查询到关联的任务ID, 则不处理
        if (StrUtil.isBlank(hmbcConfigJson)) {
            log.warn("拨测结果保存, {}未查询到该号码对应的拨测任务配置缓存, 忽略本次", number);
            return;
        }
        HmbcTaskInfo taskInfo = JSON.parseObject(hmbcConfigJson, HmbcTaskInfo.class);

        // 假如拨测成功, 则成功计数加一
        String successCountKey = String.format(HMBC_OUTBOUND_CALL_SUCCESS_COUNT_KEY, taskInfo.getTaskRecordId());
        String failCountKey = String.format(HMBC_OUTBOUND_CALL_FAILED_COUNT_KEY, taskInfo.getTaskRecordId());

        // 20240115 增加拨测失败号码重试机制
        String numberRetryCountKey = String.format(HMBC_NUMBER_FAILED_COUNT_KEY, number);
        // 如果号码可用, 那么拨测成功计数自增, 否则拨测失败计数自增
        if (isSuccess) {
            redisService.increment(successCountKey, Duration.ofDays(1));
            redisService.delKey(numberRetryCountKey);
        } else {
            redisService.increment(failCountKey, Duration.ofDays(1));
        }

        // 收到拨测推送结果, 那么执行中号码数量则减一
        String executingCountKey = String.format(HMBC_OUTBOUND_CALL_EXECUTING_COUNT_KEY, taskInfo.getTaskRecordId());
        long executingCount = redisService.decrement(executingCountKey);
        // 20240115 如果拨测失败后, 还没有达到最大失败次数, 那需要将正在执行的任务数+1
        int retryCount = redisService.getInt(numberRetryCountKey, hmbcProperties.getDialTest().getCallMaxAttempts());
        if (retryCount > hmbcProperties.getDialTest().getCallMaxAttempts()) {
            redisService.delKey(numberRetryCountKey);
        }else {
            // 因为当前这个号码还没拨测完成，它还有下一轮的拨测重试，所以进行的任务数量需要增加一个，否则任务会结束
            executingCount = redisService.increment(executingCountKey);
        }
        log.info("{}, 当前执行中的号码数量:{}", taskInfo, executingCount);

        PrivateDialTestTaskRecordDetails detailRecord = handleResult(taskInfo, cdrRecord, isSuccess);
        recordDetailsService.save(detailRecord);
        // 只要需要推送的情况才进行推送
        if (taskInfo.isPushRequired()) {
            HmbcResult hmbcResult = new HmbcResult(detailRecord);
            RetryPushDTO retryPushDTO = new RetryPushDTO();
            retryPushDTO.setUrl(taskInfo.getPushUrl());
            retryPushDTO.setVccId(taskInfo.getVccId());
            retryPushDTO.setVccName(taskInfo.getVccName());
            retryPushDTO.setNumber(hmbcResult.getNumber());
            retryPushDTO.setJobType(DialTestTypeEnum.DIAL_TEST.getCode());
            retryPushDTO.setBody(JSON.toJSONString(hmbcResult));
            // 保存号码拨测记录以及推送企业
            corpPushService.pushWithRetry(retryPushDTO);
        }

        // 如果当前执行中的拨测号码数量为0 或小于0, 则说明本轮任务已经结束, 则开始将拨测任务结果推送给企业
        if (executingCount <= 0) {
            // 这里把vccId跟taskRecordId的关联删除
            redisService.delKey(hmbcConfigKey);
            // 如果全部拨测成功, 那么拨测失败数量即为0
            int successCount = redisService.getInt(successCountKey, 0);
            int failCount = redisService.getInt(failCountKey, 0);
            // 更新拨测任务状态
            taskRecordService.taskRecordFinish(taskInfo.getTaskRecordId(), successCount, failCount);
            redisService.delKey(executingCountKey);

            log.info("{}, 本轮外呼拨测任务执行完成, 其中成功: {}, 失败: {}", taskInfo, successCount, failCount);
        }
    }

    private PrivateDialTestTaskRecordDetails handleResult(HmbcTaskInfo taskInfo,
                                                          CdrRecordSimpleEntity cdrRecord, boolean isSuc) {
        // 如果号码是不可用的, 那么获取对应的不可用原因
        String failCause = isSuc ? null : ReleaseCauseEnum.getByCode(cdrRecord.getReleaseCause());

        PrivateDialTestTaskRecordDetails detailRecord = new PrivateDialTestTaskRecordDetails();
        Date execTime = cdrRecord.getCallOutTime() != null ? cdrRecord.getCallOutTime() : DateUtil.date();
        detailRecord.setRecordId(taskInfo.getTaskRecordId())
                .setNumber(cdrRecord.getCalledNum())
                .setState(BooleanUtil.toInt(isSuc))
                .setFailCause(failCause)
                .setVccId(taskInfo.getVccId())
                .setJobId(taskInfo.getJobId())
                .setExecutionTime(execTime)
                .setRemark(String.valueOf(cdrRecord.getReleaseCause()));
        // 查询redis是否存在上一次失败的信息
        String failRecordKey = String.format(HMBC_FAILED_NUMBER_KEY, cdrRecord.getCalledNum());
        if (isSuc) {
            detailRecord.setState(HmbcConstants.DialTestState.SUCCESS.getCode());
            if (redisService.hasKey(failRecordKey)) {
                log.info("拨测号码: [{}] 存在上一次失败记录, 本次恢复可用", cdrRecord.getCalledNum());
                detailRecord.setState(HmbcConstants.DialTestState.RECOVERY.getCode());
                // 判断完成后, 需要将原先的记录删除, 否则在这个key过期前拨测可能都会是恢复可用的状态
                redisService.delKey(failRecordKey);
            }
            return detailRecord;
        }
        // 如果这次拨测失败了, 那么就保存拨测信息
        redisService.setStr(failRecordKey, detailRecord.getNumber(), Duration.ofDays(hmbcProperties.getFailedNumberExpire()));

        return detailRecord;
    }

    /**
     * 根据挂机原因判断是否上平台
     *
     * @param cdrRecord 拨测话单数据
     * @return boolean
     */
    private boolean checkReleaseCause(CdrRecordSimpleEntity cdrRecord) {
        if (hmbcProperties.getDialTest().getAbnormalCodes().contains(cdrRecord.getReleaseCause())) {
            log.info("该号码: {}, 挂机原因值: {} 为未上平台的状态码", cdrRecord.getCalledNum(), cdrRecord.getReleaseCause());
            return false;
        }
        return true;
    }


    @Override
    public void retryHandle(RetryQueryDTO retryQueryDTO) {
        Long exist = acrRecordService.findAcr(retryQueryDTO.getCdrRecord(), retryQueryDTO.getDsName());
        // 如果数据库没有查询这个号码的话单, 则说明没有上平台, 根据配置进行延迟推送
        CdrRecordSimpleEntity cdrRecord = retryQueryDTO.getCdrRecord();
        if (exist != null) {
            // 这里是认为号码上平台了
            dialTestResultSaveAndPush(cdrRecord, true);
            return;
        }
        log.info("{} 该号码未查询到话单, 数据源: {}, 当前查询次数: {}", cdrRecord.getCalledNum(), retryQueryDTO.getDsName(), retryQueryDTO.getRetryCount());
        if (retryQueryDTO.getRetryCount() >= hmbcProperties.getDialTest().getCdrQueryMaxAttempts()) {
            log.info("{} 该号码查询次数达到上限, 不再查询", cdrRecord.getCalledNum());
            // 20240115 增加拨测失败号码重试机制
            outboundCallRetry(cdrRecord);
            // 这里则认为该号码未上平台
            dialTestResultSaveAndPush(cdrRecord, false);
            return;
        }
        // 拨测话单入库可能会比较慢, 这里先把记录推到延时队列中, 之后再进行重试
        mqProducer.sendDelayMsg(retryQueryDTO,
                hmbcProperties.getDialTest().getCdrQueryInterval(),
                RabbitMqConfig.HMBC_CDR_QUERY_DELAY_EXCHANGE, RabbitMqConfig.ROUTING_KEY);
    }

    @Override
    public void discardHandle(RetryQueryDTO param) {
        log.warn("{} 达到最大查询次数: {}, 丢弃", param.getBizId(), param.getRetryCount());
    }

    /**
     * 外呼拨测重试
     * 主要针对于部分识别结果异常的号码，进行重试处理
     * */
    private void outboundCallRetry(CdrRecordSimpleEntity cdrRecord) {
        String number = cdrRecord.getCalledNum();
        String key = String.format(HMBC_NUMBER_FAILED_COUNT_KEY, number);
        long current = redisService.increment(key, Duration.ofHours(8));
        if (current > hmbcProperties.getDialTest().getCallMaxAttempts()) {
            log.info("拨测号码: {}, 未查询到话单数据, 已达到最大重试次数 {}", number, current - 1);
            return;
        }
        log.info("拨测号码: {}, 第 {} 次拨测未查询到话单数据, 进行第 {} 次重试", number, current, current + 1);
        String url = hmbcProperties.getDialTest().getExecCommandUrl();
        CommandEntity command = getCommandEntity(cdrRecord);
        String reqJson = JSON.toJSONString(command);
        int timeout = hmbcProperties.getDialTest().getTimeout();

        try {
            RestTemplateRequest.of(restTemplate)
                    .post(url)
                    .body(reqJson)
                    .timeout(timeout)
                    .execute();
            // 这里执行完呼叫命令后, 等待一定时间间隔, 防止fs并发过高出现异常
            Thread.sleep(hmbcProperties.getDialTest().getWaitInterval());
        } catch (Exception e) {
            log.error("拨测号码: {}, 请求URL: {}, HTTP请求出现异常: ", number, url, e);
        }
    }


    /**
     * 将拨测号码信息转换成对应的拨测命令
     *
     * @param cdrRecord 拨测号码信息
     * @return CommandEntity 拨测命令
     */
    private CommandEntity getCommandEntity(CdrRecordSimpleEntity cdrRecord) {
        List<String> callers = hmbcProperties.getDialTest().getCallerNumbers();
        String vccId = cdrRecord.getVccId();
        String called = cdrRecord.getCalledNum();
        int size = callers.size();
        int index = ThreadLocalRandom.current().nextInt(size);
        String luaParam = String.format(hmbcProperties.getDialTest().getLuaParamFormat(), callers.get(index), called, vccId);

        CommandEntity commandEntity = new CommandEntity();
        commandEntity.setOneMsg("fs_cli");
        commandEntity.setTwoMsg("-x");

        commandEntity.setThreeMsg(luaParam);

        return commandEntity;
    }
}
