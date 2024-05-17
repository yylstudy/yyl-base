package com.cqt.hmbc.job;

import cn.hutool.core.lang.RegexPool;
import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import com.cqt.common.enums.DialTestTypeEnum;
import com.cqt.common.enums.MapLocationUpdatingStatusEnum;
import com.cqt.hmbc.mapper.VlrGtInfoMapper;
import com.cqt.hmbc.retry.RetryPushDTO;
import com.cqt.hmbc.service.CorpPushService;
import com.cqt.hmbc.service.PrivateDialTestTaskRecordDetailsService;
import com.cqt.hmbc.service.PrivateDialTestTaskRecordService;
import com.cqt.hmbc.service.PrivateDialTestTimingConfService;
import com.cqt.hmbc.service.impl.LocationUpdatingService;
import com.cqt.hmbc.service.impl.RedisService;
import com.cqt.model.hmbc.dto.DialTestNumberDTO;
import com.cqt.model.hmbc.dto.HmbcTaskInfo;
import com.cqt.model.hmbc.dto.LocationUpdatingReq;
import com.cqt.model.hmbc.dto.LocationUpdatingRsp;
import com.cqt.model.hmbc.entity.PrivateDialTestTaskRecordDetails;
import com.cqt.model.hmbc.entity.PrivateDialTestTimingConf;
import com.cqt.model.hmbc.entity.VlrGtInfo;
import com.cqt.model.hmbc.properties.HmbcProperties;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 定时位置更新 任务
 *
 * @author Xienx
 * @date 2022年07月28日 15:37
 */
@Slf4j
@Component
public class LocationUpdateJob extends CommonJob {
    private final VlrGtInfoMapper vlrGtInfoMapper;
    private final LocationUpdatingService locationUpdatingService;

    public LocationUpdateJob(RedisService redisService,
                             HmbcProperties hmbcProperties,
                             CorpPushService corpPushService,
                             VlrGtInfoMapper vlrGtInfoMapper,
                             PrivateDialTestTimingConfService timingConfService,
                             PrivateDialTestTaskRecordService taskRecordService,
                             PrivateDialTestTaskRecordDetailsService taskRecordDetailsService,
                             LocationUpdatingService locationUpdatingService) {
        super(redisService, hmbcProperties, corpPushService, timingConfService, taskRecordService, taskRecordDetailsService);
        this.vlrGtInfoMapper = vlrGtInfoMapper;
        this.locationUpdatingService = locationUpdatingService;
    }

    @XxlJob("locationUpdate")
    public void locationUpdateJob() {
        long startTime = System.currentTimeMillis();
        log.info("[定时位置更新] =>=> jobId=>{}, 定时位置更新任务开始执行", XxlJobHelper.getJobId());

        String jobKey = String.format(HMBC_JOB_LOCK_KEY, XxlJobHelper.getJobId());
        // 这里做互斥处理
        if (!redisService.tryLock(jobKey, JOB_LOCK_EXPIRED_TIME)) {
            log.info("[定时位置更新] <=<= jobId: {}, 定时位置更新任务执行中, 忽略本次", XxlJobHelper.getJobId());
            XxlJobHelper.handleFail("上一轮调度还未执行完成");
            return;
        }
        // 先查询该jobId对应的定时位置更新任务配置
        PrivateDialTestTimingConf timingConf = getTimingConfByJobId();
        if (timingConf == null) {
            XxlJobHelper.handleFail("未查询到定时位置更新拨测计划配置");
            return;
        }
        int totalCount = 0;
        int sucCount = 0;
        try {
            // 根据定时位置更新任务配置查询出相应的拨测号码
            List<DialTestNumberDTO> numberList = getConfigNumberList(timingConf);
            // 查询出所有gt和位置更新接口地址的映射集合
            Map<String, String> vlrGtInfos = getVlrGtMap();
            // 这里获取待更新的号码信息
            List<DialTestNumberDTO> waitExecNums = getLocationExecNumbers(vlrGtInfos, numberList);
            // 只有查询到对应的拨测号码, 才走位置更新拨测流程
            if (waitExecNums.isEmpty()) {
                // 在没有查询到拨测号码时, 则认为该次拨测任务结束
                log.info("[定时位置更新] <=<= jobId=>{}, vccId=>{}, 未查询到有效位置更新号码, 本次定时位置更新任务结束", timingConf.getJobId(), timingConf);
                taskRecordService.emptyTaskRecord(timingConf);
                XxlJobHelper.handleFail("未查询到有效拨测号码");
                return;
            }
            // 定时位置更新号码总数则为符合位置更新规则的号码数量
            totalCount = waitExecNums.size();
            // 开始任务
            HmbcTaskInfo taskInfo = startTask(timingConf, waitExecNums);
            // 这里执行位置更新
            List<PrivateDialTestTaskRecordDetails> resultList = new ArrayList<>();
            sucCount += locationUpdateWithRetry(taskInfo, vlrGtInfos, resultList);
            // 这里将拨测任务记录进行更新
            taskRecordService.taskRecordFinish(taskInfo.getTaskRecordId(), sucCount, totalCount - sucCount);
            // 将拨测完的结果推送给企业
            if (taskInfo.isPushRequired()) {
                pushLocationResult(taskInfo, resultList);
            }
            XxlJobHelper.handleSuccess("定时位置更新执行成功");
        } catch (Exception e) {
            log.error("[定时位置更新] 执行异常: ", e);
            XxlJobHelper.handleFail("定时位置更新执行异常: " + e.getMessage());
        } finally {
            redisService.releaseLock(jobKey);
            log.info("[定时位置更新] <=<= jobId=>{}, vccId:{}, 本次待更新号码数量:{}, 更新成功号码数量:{}, 执行完成, 累计耗时:{} ms",
                    timingConf.getJobId(), timingConf.getVccId(), totalCount, sucCount, System.currentTimeMillis() - startTime);
        }
    }


    private List<LocationUpdatingReq> locationUpdate(List<LocationUpdatingReq> reqInfos) {
        List<LocationUpdatingReq> failList = new ArrayList<>();
        Map<String, LocationUpdatingReq> reqInfoMap = reqInfos.stream()
                .collect(Collectors.toMap(LocationUpdatingReq::getNumber, e -> e));
        List<LocationUpdatingRsp> rspList = locationUpdatingService.locationUpdatingSync4Job(reqInfos);
        for (LocationUpdatingRsp element : rspList) {
            // 保存当前位置更新失败的号码信息，以便下一轮进行重试
            if (!MapLocationUpdatingStatusEnum.SUCCESS.getCode().equals(element.getStatus())) {
                LocationUpdatingReq reqInfo = reqInfoMap.get(element.getNumber());
                reqInfo.setFailCause(element.getErrorReason());
                failList.add(reqInfo);
            }
        }
        return failList;
    }


    private int locationUpdateWithRetry(HmbcTaskInfo execInfo,
                                        Map<String, String> vlrInfoMap,
                                        List<PrivateDialTestTaskRecordDetails> resultList) {
        int sucCount = 0;
        int maxAttempts = hmbcProperties.getLocationUpdating().getMaxAttempts();
        int interval = hmbcProperties.getLocationUpdating().getInterval();
        long startMills = System.currentTimeMillis();
        try {
            // 构造请求参数
            String gtCode;
            LocationUpdatingReq reqInfo;
            List<LocationUpdatingReq> reqInfos = new ArrayList<>();
            for (DialTestNumberDTO numberInfo : execInfo.getNumberInfos()) {
                gtCode = numberInfo.getGtCode();
                reqInfo = new LocationUpdatingReq(numberInfo, vlrInfoMap.get(gtCode));
                // 校验mapUrl是否正常, 对于不正确的url不再请求, 直接返回错误
                if (checkMapUrl(gtCode, vlrInfoMap.get(gtCode))) {
                    reqInfos.add(reqInfo);
                    continue;
                }
                String errorReason = String.format("[%s] config map url is not correct, [%s]", gtCode, vlrInfoMap.get(gtCode));
                resultList.add(detailsRecordBuild(numberInfo, execInfo, errorReason));
            }
            List<LocationUpdatingReq> failList = doWithRetry(reqInfos, maxAttempts, interval, this::locationUpdate);

            sucCount += reqInfos.size() - failList.size();

            // 这里存放号码与号码信息的映射关系
            Map<String, DialTestNumberDTO> numberInfoMap = execInfo.getNumberInfos()
                    .stream()
                    .collect(Collectors.toMap(DialTestNumberDTO::getNumber, e -> e));
            PrivateDialTestTaskRecordDetails detail;
            for (LocationUpdatingReq element : reqInfos) {
                DialTestNumberDTO numberInfo = numberInfoMap.get(element.getNumber());
                detail = detailsRecordBuild(numberInfo, execInfo, element.getFailCause());
                resultList.add(detail);
            }
            // 对结果进行处理
            handleResult(resultList);
            taskRecordDetailsService.saveBatch(resultList);
            return sucCount;
        } finally {
            log.info("{}, 定时位置更新执行完成, 累计耗时: {} ms", execInfo, System.currentTimeMillis() - startMills);
        }
    }


    /**
     * 将本次位置更新结果推送给企业
     *
     * @param taskInfo   任务配置信息
     * @param resultList 拨测结果详情
     */
    private void pushLocationResult(HmbcTaskInfo taskInfo, List<PrivateDialTestTaskRecordDetails> resultList) {
        // 如果企业定时位置更新任务配置的推送类型是仅推送异常结果, 则只查询定时位置更新失败的任务记录
        if (DialTestPushType.EXCEPTION_RESULT_PUSH.getCode().equals(taskInfo.getPushType())) {
            resultList = resultList.stream()
                    .filter(e -> DialTestState.FAILED.getCode().equals(e.getState()))
                    .collect(Collectors.toList());
        }
        resultList = resultList.stream().peek(e -> {
            if (DialTestState.FAILED.getCode().equals(e.getState())) {
                e.setFailCause("位置更新失败");
            }
        }).collect(Collectors.toList());
        log.info("[{}] =>=> vccId=>{}, 拨测结果推送开始, 推送URL:{}, 推送类型:{}", getDialTestType().getText(),
                taskInfo.getVccId(), taskInfo.getPushUrl(), taskInfo.getPushType());
        List<RetryPushDTO> retryPushDTOList = convert2PushDTO(taskInfo, resultList);
        corpPushService.pushBatch(retryPushDTOList);
    }

    private Map<String, String> getVlrGtMap() {
        return vlrGtInfoMapper.getVlrGtInfos()
                .stream()
                .collect(Collectors.toMap(VlrGtInfo::getGtCode, VlrGtInfo::getMapUrl));
    }


    private boolean checkMapUrl(String gtCode, String mapUrl) {
        if (StrUtil.isBlank(mapUrl)) {
            log.warn("[{}] config map request url is empty: [{}]", gtCode, mapUrl);
            return false;
        }
        // 这里实际上只能检查 给定的url是否以 http://或https:// 开头
        if (!ReUtil.isMatch(RegexPool.URL_HTTP, mapUrl)) {
            log.warn("[{}] config map request url is not correct: [{}]", gtCode, mapUrl);
            return false;
        }
        return true;
    }


    @Override
    DialTestTypeEnum getDialTestType() {
        return DialTestTypeEnum.LOCATION;
    }
}
