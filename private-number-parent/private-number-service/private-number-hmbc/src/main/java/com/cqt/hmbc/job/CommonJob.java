package com.cqt.hmbc.job;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.cqt.common.constants.HmbcConstants;
import com.cqt.common.constants.PrivateCacheConstant;
import com.cqt.common.enums.DialTestTypeEnum;
import com.cqt.common.enums.LocationUpdateStatusEnum;
import com.cqt.common.enums.NumberStateEnum;
import com.cqt.hmbc.retry.RetryPushDTO;
import com.cqt.hmbc.service.CorpPushService;
import com.cqt.hmbc.service.PrivateDialTestTaskRecordDetailsService;
import com.cqt.hmbc.service.PrivateDialTestTaskRecordService;
import com.cqt.hmbc.service.PrivateDialTestTimingConfService;
import com.cqt.hmbc.service.impl.RedisService;
import com.cqt.model.hmbc.dto.DialTestNumberDTO;
import com.cqt.model.hmbc.dto.DialTestNumberQueryDTO;
import com.cqt.model.hmbc.dto.HmbcTaskInfo;
import com.cqt.model.hmbc.entity.PrivateDialTestTaskRecordDetails;
import com.cqt.model.hmbc.entity.PrivateDialTestTimingConf;
import com.cqt.model.hmbc.properties.HmbcProperties;
import com.cqt.model.hmbc.vo.HmbcResult;
import com.xxl.job.core.context.XxlJobHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * CommonJob
 *
 * @author Xienx
 * @date 2023年02月08日 15:50
 */
@Slf4j
@RequiredArgsConstructor
public abstract class CommonJob implements HmbcConstants, PrivateCacheConstant {
    protected final RedisService redisService;
    protected final HmbcProperties hmbcProperties;
    protected final CorpPushService corpPushService;
    protected final PrivateDialTestTimingConfService timingConfService;
    protected final PrivateDialTestTaskRecordService taskRecordService;
    protected final PrivateDialTestTaskRecordDetailsService taskRecordDetailsService;

    protected final Duration JOB_LOCK_EXPIRED_TIME = Duration.ofMinutes(30);

    /**
     * 标识当前任务的类型
     *
     * @return DialTestTypeEnum
     */
    abstract DialTestTypeEnum getDialTestType();


    /**
     * 根据任务id查询定时拨测任务配置
     *
     * @return PrivateDialTestTimingConf
     */
    protected PrivateDialTestTimingConf getTimingConfByJobId() {
        PrivateDialTestTimingConf testTimingConf = timingConfService.getByJobId(XxlJobHelper.getJobId());
        if (testTimingConf == null) {
            log.warn("<=<= jobId=>{}, 未查询到对应的拨测任务配置, 忽略本次执行", XxlJobHelper.getJobId());
            return null;
        }
        return testTimingConf;
    }

    /**
     * 根据定时拨测任务配置查询出需要拨测的号码详情信息
     *
     * @param timingConf 号码拨测配置
     * @return List 拨测号码信息
     */
    protected List<DialTestNumberDTO> getConfigNumberList(PrivateDialTestTimingConf timingConf) {
        DialTestNumberQueryDTO queryDTO = new DialTestNumberQueryDTO();
        queryDTO.setTimingConfId(timingConf.getId());
        queryDTO.setNumberRange(timingConf.getNumberRange());
        queryDTO.setVccId(timingConf.getVccId());

        return timingConfService.findNumbers(queryDTO);
    }

    /**
     * 获取企业名称
     *
     * @param dataList 号码信息
     * @return String 企业名称
     */
    protected String getVccName(List<DialTestNumberDTO> dataList) {
        return dataList.stream()
                .map(DialTestNumberDTO::getVccName)
                .findFirst()
                .orElse("");
    }

    /**
     * 从该企业配置的定时隐私拨测号码中挑选出可以进行拨测的号码
     * 不符合位拨测的号码, 保存到失败的任务详单中
     * </p>
     *
     * @param dataList 配置需要定时拨测的号码集合
     * @return List<DialTestNumberDTO> 待执行的号码集合
     */
    protected List<DialTestNumberDTO> getExecNumbers(List<DialTestNumberDTO> dataList) {
        return dataList.stream()
                // 如果号码已经下线, 则就不再进行隐私号拨测了, 该号码拨测失败
                .filter(e -> !NumberStateEnum.OFFLINE.getCode().equals(e.getState()))
                // 如果该号码位置更新失败, 那么就不进行号码拨测
                .filter(e -> LocationUpdateStatusEnum.SUCCESS.getCode().equals(e.getLocationUpdateStatus()))
                .collect(Collectors.toList());
    }

    /**
     * 从该企业配置的定时拨测号码中挑选出可以进行位置更新的号码
     * 不符合位拨测的号码, 保存到失败的任务详单中
     * </p>
     *
     * @param vlrInfos gt信息
     * @param dataList 配置需要定时拨测的号码集合
     * @return List<DialTestNumberDTO> 待执行的号码集合
     */
    protected List<DialTestNumberDTO> getLocationExecNumbers(Map<String, String> vlrInfos,
                                                             List<DialTestNumberDTO> dataList) {
        if (vlrInfos.isEmpty()) {
            return Collections.emptyList();
        }

        return dataList.stream()
                // 如果号码已经下线, 则就不再进行位置更新, 该号码拨测失败
                .filter(e -> !NumberStateEnum.OFFLINE.getCode().equals(e.getState()))
                // 没有配置gtCode属性的也不进行位置更新, 该号码拨测失败
                .filter(e -> StrUtil.isNotEmpty(e.getGtCode()))
                // 没有配置imsi属性的也不进行位置更新, 该号码拨测失败
                .filter(e -> StrUtil.isNotEmpty(e.getImsi()))
                // 如果gt信息配置错误也不行位置更新
                .filter(e -> vlrInfos.containsKey(e.getGtCode()))
                .collect(Collectors.toList());
    }

    /**
     * 生成拨测结果信息
     *
     * @param numberInfo 号码信息
     * @param taskInfo   号码拨测信息
     * @param failCause  失败原因
     */
    protected PrivateDialTestTaskRecordDetails detailsRecordBuild(DialTestNumberDTO numberInfo,
                                                                  HmbcTaskInfo taskInfo, String failCause) {
        PrivateDialTestTaskRecordDetails detailInfo = new PrivateDialTestTaskRecordDetails();
        detailInfo.setNumber(numberInfo.getNumber());
        detailInfo.setJobId(taskInfo.getJobId());
        detailInfo.setRecordId(taskInfo.getTaskRecordId());
        detailInfo.setImsi(numberInfo.getImsi());
        detailInfo.setVccId(numberInfo.getVccId());
        detailInfo.setGtCode(numberInfo.getGtCode());
        detailInfo.setExecutionTime(DateUtil.date());
        detailInfo.setState(DialTestState.SUCCESS.getCode());

        if (StrUtil.isNotEmpty(failCause)) {
            detailInfo.setFailCause(failCause);
            detailInfo.setState(DialTestState.FAILED.getCode());
        }

        return detailInfo;
    }

    /**
     * 生成taskInfo 信息
     *
     * @param timingConf 定时任务配置信息
     * @param execNums   待执行的号码信息
     * @return HmbcTaskInfo
     */
    protected HmbcTaskInfo startTask(PrivateDialTestTimingConf timingConf, List<DialTestNumberDTO> execNums) {
        String vccName = getVccName(execNums);
        HmbcTaskInfo taskInfo = new HmbcTaskInfo(timingConf);
        taskInfo.setVccName(vccName);
        taskInfo.setNumberInfos(execNums);
        taskInfo.setTotalCount(execNums.size());
        taskInfo.setPushRequired(true);
        // 判断该企业是否配置企业推送URL
        if (StrUtil.isEmpty(timingConf.getPushUrl())) {
            log.warn("vccId: {}, 未配置企业推送URL, 忽略本次拨测结果推送", taskInfo.getVccId());
            taskInfo.setPushRequired(false);
        }
        // 判断该企业是否进行推送
        if (DialTestPushType.NOTHING_PUSH.getCode().equals(timingConf.getPushType())) {
            log.warn("vccId: {}, 未开启推送, 忽略本次拨测结果推送", taskInfo.getVccId());
            taskInfo.setPushRequired(false);
        }
        
        // 先生成一条定时拨测的任务记录
        String taskRecordId = taskRecordService.taskRecordStart(timingConf, execNums.size());
        taskInfo.setTaskRecordId(taskRecordId);

        return taskInfo;
    }


    protected void handleResult(List<PrivateDialTestTaskRecordDetails> list) {
        if (CollUtil.isEmpty(list)) {
            return;
        }
        // 按照拨测状态进行分组
        Map<Integer, List<PrivateDialTestTaskRecordDetails>> groupingMap = list.stream()
                .collect(Collectors.groupingBy(PrivateDialTestTaskRecordDetails::getState));
        // 根据分组的结果得出拨测成功的号码信息, 并对这部分号码进行处理
        handleSuccessResult(groupingMap.getOrDefault(DialTestState.SUCCESS.getCode(), Collections.emptyList()));
        // 根据分组的结果得出拨测失败的号码信息, 并对这部分号码进行处理
        handleFailResult(groupingMap.getOrDefault(DialTestState.FAILED.getCode(), Collections.emptyList()));
    }

    protected void handleSuccessResult(List<PrivateDialTestTaskRecordDetails> successList) {
        successList.forEach(this::handleSuccessResult);
    }

    protected void handleFailResult(List<PrivateDialTestTaskRecordDetails> failedList) {
        failedList.forEach(this::handleFailResult);
    }

    private void handleSuccessResult(PrivateDialTestTaskRecordDetails successRecord) {
        if (successRecord == null) {
            return;
        }
        // 查询redis是否存在上一次失败的信息
        String key = String.format(HMBC_FAILED_NUMBER_KEY, successRecord.getNumber());
        if (redisService.hasKey(key)) {
            log.info("拨测号码: [{}] 存在上一次失败记录, 本次恢复可用", successRecord.getNumber());
            successRecord.setState(DialTestState.RECOVERY.getCode());
            // 判断完成后, 需要将原先的记录删除, 否则在这个key过期前拨测可能都会是恢复可用的状态
            redisService.delKey(key);
        }
    }

    private void handleFailResult(PrivateDialTestTaskRecordDetails failedRecord) {
        if (failedRecord == null) {
            return;
        }
        String key = String.format(HMBC_FAILED_NUMBER_KEY, failedRecord.getNumber());
        redisService.setStr(key, failedRecord.getNumber(), Duration.ofDays(hmbcProperties.getFailedNumberExpire()));
    }

    /**
     *
     */
    protected <T> List<T> doWithRetry(List<T> list,
                                      int maxAttempts, int interval,
                                      Function<List<T>, List<T>> action) {
        int retryCount = 0;
        int sucCount;
        int totalCount = list.size();

        List<T> retryList = list;
        do {
            log.info("[{}] 第 {} 次重试, 本次共 {} 个号码", getDialTestType().getText(), retryCount, retryList.size());
            retryList = action.apply(retryList);
            sucCount = totalCount - retryList.size();
            log.info("[{}] 第 {} 次重试, 累计成功数量 {}", getDialTestType().getText(), retryCount, sucCount);
            ThreadUtil.sleep(interval);
        } while (++retryCount <= maxAttempts && !retryList.isEmpty());

        return retryList;
    }

    protected RetryPushDTO convert2PushDTO(HmbcTaskInfo taskInfo, PrivateDialTestTaskRecordDetails details) {
        HmbcResult hmbcResult = new HmbcResult(details);

        RetryPushDTO retryPushDTO = new RetryPushDTO();
        retryPushDTO.setUrl(taskInfo.getPushUrl());
        retryPushDTO.setVccId(taskInfo.getVccId());
        retryPushDTO.setVccName(taskInfo.getVccName());
        retryPushDTO.setNumber(hmbcResult.getNumber());
        retryPushDTO.setJobType(getDialTestType().getCode());
        retryPushDTO.setBody(JSON.toJSONString(hmbcResult));
        return retryPushDTO;
    }

    protected List<RetryPushDTO> convert2PushDTO(HmbcTaskInfo taskInfo, List<PrivateDialTestTaskRecordDetails> list) {
        return list.stream()
                .map(e -> convert2PushDTO(taskInfo, e))
                .collect(Collectors.toList());
    }
}
