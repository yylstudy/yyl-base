package com.cqt.queue.calltask.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.cqt.base.balancer.RoundRobinLoadBalancer;
import com.cqt.base.enums.MediaStreamEnum;
import com.cqt.base.enums.MsgTypeEnum;
import com.cqt.base.enums.calltask.CallTaskEnum;
import com.cqt.base.enums.calltask.CallTaskStatusEnum;
import com.cqt.base.exception.BizException;
import com.cqt.base.model.ResultVO;
import com.cqt.base.util.CacheUtil;
import com.cqt.base.util.TraceIdUtil;
import com.cqt.cloudcc.manager.service.CommonDataOperateService;
import com.cqt.mapper.task.IvrOutboundTaskMapper;
import com.cqt.mapper.task.IvrOutboundTaskNumberMapper;
import com.cqt.model.calltask.dto.CallTaskOperateDTO;
import com.cqt.model.calltask.entity.IvrOutboundTask;
import com.cqt.model.calltask.entity.IvrOutboundTaskNumber;
import com.cqt.model.client.base.ClientResponseBaseVO;
import com.cqt.model.client.dto.ClientOutboundCallTaskDTO;
import com.cqt.queue.calltask.aspect.TaskInfoContext;
import com.cqt.queue.calltask.factory.ExecutorFactory;
import com.cqt.queue.calltask.properties.CloudOutboundCallTaskProperties;
import com.cqt.queue.calltask.service.DataQueryService;
import com.cqt.queue.calltask.service.OutboundCallTaskStrategy;
import com.cqt.rpc.call.CallControlRemoteService;
import com.cqt.starter.redis.util.RedissonUtil;
import com.cqt.xxljob.enums.ExecutorBlockStrategyEnum;
import com.cqt.xxljob.enums.ExecutorRouteStrategyEnum;
import com.cqt.xxljob.enums.MisfireStrategyEnum;
import com.cqt.xxljob.model.XxlJobGroup;
import com.cqt.xxljob.model.XxlJobInfo;
import com.cqt.xxljob.service.XxlJobGroupService;
import com.cqt.xxljob.service.XxlJobInfoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.redisson.client.protocol.ScoredEntry;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * @author linshiqiang
 * date:  2023-10-25 14:57
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class IvrOutboundCallTaskStrategyImpl extends AbstractOutCallTask implements OutboundCallTaskStrategy {

    private static final String JOB_NAME = "ivrOutboundCallTaskJob";

    private final ObjectMapper objectMapper;

    private final IvrOutboundTaskMapper ivrOutboundTaskMapper;

    private final IvrOutboundTaskNumberMapper ivrOutboundTaskNumberMapper;

    private final RedissonUtil redissonUtil;

    private final RoundRobinLoadBalancer<String> roundRobinLoadBalancer;

    private final XxlJobInfoService xxlJobInfoService;

    private final XxlJobGroupService xxlJobGroupService;

    private final CloudOutboundCallTaskProperties cloudOutboundCallTaskProperties;

    private final CommonDataOperateService commonDataOperateService;

    private final DataQueryService dataQueryService;

    @DubboReference
    private CallControlRemoteService callControlRemoteService;

    @Override
    public CallTaskEnum getCallTaskEnum() {
        return CallTaskEnum.IVR;
    }

    @Override
    public ResultVO<Integer> addTask(CallTaskOperateDTO callTaskOperateDTO) {
        String taskId = callTaskOperateDTO.getTaskId();
        IvrOutboundTask ivrOutboundTask = getIvrOutboundTask(taskId);
        boolean registered = xxlJobGroupService.registerGroup();
        if (registered) {
            // 启动任务
            List<XxlJobGroup> jobGroups = xxlJobGroupService.getJobGroup();
            if (CollUtil.isEmpty(jobGroups)) {
                return ResultVO.fail("创建任务失败!");
            }
            XxlJobGroup xxlJobGroup = jobGroups.get(0);

            // 确认是否创建过, 有则更新
            XxlJobInfo xxlJobInfo = createXxlJobInfo(xxlJobGroup, ivrOutboundTask, null);
            Integer jobId = xxlJobInfoService.addJobInfo(xxlJobInfo);
            if (Objects.isNull(jobId)) {
                return ResultVO.fail("创建任务失败!");
            }
            log.info("[ivr外呼] xxl任务创建, jobId: {}", jobId);
            return ResultVO.ok("创建任务成功!", jobId);
        }
        return ResultVO.fail("创建任务失败!");
    }

    @Override
    public ResultVO<Void> startTask(CallTaskOperateDTO callTaskOperateDTO) {
        String taskId = callTaskOperateDTO.getTaskId();
        IvrOutboundTask ivrOutboundTask = getIvrOutboundTask(taskId);
        Integer jobId = callTaskOperateDTO.getJobId();
        boolean permit = isInPermitTime(ivrOutboundTask.getStartTime(), ivrOutboundTask.getEndTime());
        if (!permit) {
            stopCallTask(taskId, jobId);
            return ResultVO.fail("任务不在允许时间内, 结束任务.");
        }
        if (Objects.isNull(jobId)) {
            return ResultVO.fail("任务id为空!");
        }
        // 启动任务-xxljob
        boolean started = xxlJobInfoService.startJob(jobId);
        if (started) {
            return ResultVO.ok("启动成功!");
        }
        return ResultVO.fail("启动失败!");
    }

    @Override
    public ResultVO<Void> stopTask(CallTaskOperateDTO callTaskOperateDTO) {
        boolean stopJob = xxlJobInfoService.stopJob(callTaskOperateDTO.getJobId());
        if (stopJob) {
            return ResultVO.ok("暂停任务成功!");
        }
        return ResultVO.fail("暂停任务失败!");
    }

    @Override
    public ResultVO<Void> updateTask(CallTaskOperateDTO callTaskOperateDTO) {
        IvrOutboundTask ivrOutboundTask = getIvrOutboundTask(callTaskOperateDTO.getTaskId());
        List<XxlJobGroup> jobGroups = xxlJobGroupService.getJobGroup();
        if (CollUtil.isEmpty(jobGroups)) {
            return ResultVO.fail("创建任务失败!");
        }
        XxlJobGroup xxlJobGroup = jobGroups.get(0);
        XxlJobInfo xxlJobInfo = createXxlJobInfo(xxlJobGroup, ivrOutboundTask, callTaskOperateDTO.getJobId());
        boolean updated = xxlJobInfoService.updateJobInfo(xxlJobInfo);
        if (updated) {
            return ResultVO.ok("更新任务成功!");
        }
        return ResultVO.fail("更新任务失败!");
    }

    @Override
    public ResultVO<Void> delTask(CallTaskOperateDTO callTaskOperateDTO) {
        boolean del = xxlJobInfoService.delJob(callTaskOperateDTO.getJobId());
        if (del) {
            String ivrTaskNumberCallStatusKey = CacheUtil.getIvrTaskNumberCallStatusKey(callTaskOperateDTO.getTaskId());
            redissonUtil.delHash(ivrTaskNumberCallStatusKey);
            return ResultVO.ok("删除任务成功!");
        }
        return ResultVO.fail("删除任务失败!");
    }

    private IvrOutboundTask getIvrOutboundTask(String taskId) {
        CallTaskOperateDTO callTaskOperateDTO = TaskInfoContext.get();
        IvrOutboundTask ivrOutboundTask = callTaskOperateDTO.getIvrInfo();
        if (Objects.isNull(ivrOutboundTask)) {
            throw new BizException("任务信息不存在!");
        }
        if (!Objects.equals(ivrOutboundTask.getTaskId(), taskId)) {
            throw new BizException("任务信息不匹配!");
        }
        return ivrOutboundTask;
    }

    /**
     * xxljob
     */
    @XxlJob(JOB_NAME)
    public void ivrOutboundCallTaskJob() throws Exception {
        String taskId = XxlJobHelper.getJobParam();
        IvrOutboundTask ivrOutboundTask = ivrOutboundTaskMapper.selectById(taskId);
        try {
            String companyCode = ivrOutboundTask.getCompanyCode();
            TraceIdUtil.setTraceId(companyCode + StrUtil.AT + taskId);
            execute(ivrOutboundTask);
        } finally {
            TraceIdUtil.remove();
        }

    }

    @SuppressWarnings("all")
    public void execute(IvrOutboundTask ivrOutboundTask) throws Exception {
        Integer jobId = ivrOutboundTask.getJobId();
        boolean permit = isInPermitTime(ivrOutboundTask.getStartTime(), ivrOutboundTask.getEndTime());
        if (!permit) {
            stopCallTask(ivrOutboundTask.getTaskId(), jobId);
            log.debug("[ivr外呼] 任务已过期, 结束任务.");
            return;
        }
        boolean callable = checkCallable(ivrOutboundTask.getCallableTime());
        if (!callable) {
            log.debug("[ivr外呼] 当前时间, 不在允许呼叫时间段内.");
            return;
        }
        String taskId = ivrOutboundTask.getTaskId();
        Integer outboundRatio = ivrOutboundTask.getOutboundRatio();
        List<ScoredEntry<String>> ivrNumberList = getIvrNumberList(taskId, outboundRatio, ivrOutboundTask.getMaxAttemptCount());
        if (CollUtil.isEmpty(ivrNumberList)) {
            log.debug("[ivr外呼] 没有需要呼叫的号码.");
            return;
        }

        // 重呼间隔（秒）
        Integer attemptInterval = ivrOutboundTask.getAttemptInterval();
        // 外显号码
        List<String> displayNumberList = getDisplayNumberList(ivrOutboundTask.getDisplayNumber());

        // 判断第一个是否达到最大外呼次数
        boolean reachMax = checkAllReachMaxAttempt(ivrNumberList, ivrOutboundTask);
        if (reachMax) {
            return;
        }

        List<CompletableFuture<Void>> futureList = new ArrayList<>();
        for (ScoredEntry<String> scoredEntry : ivrNumberList) {
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                // 有排队的客户, 再开启一个线程处理
                try {
                    originate(ivrOutboundTask, displayNumberList, scoredEntry);
                } catch (Exception e) {
                    log.error("[ivr外呼] 号码: {}, 发起呼叫异常: ", scoredEntry.getValue(), e);
                }
            }, getExecutor(ivrOutboundTask));
            futureList.add(future);
        }
        CompletableFuture[] futures = new CompletableFuture[futureList.size()];
        futureList.toArray(futures);
        CompletableFuture.allOf(futures).join();
    }

    private Executor getExecutor(IvrOutboundTask ivrOutboundTask) {
        String poolName = ivrOutboundTask.getCompanyCode() + StrUtil.AT + getCallTaskEnum();
        Integer poolSize = cloudOutboundCallTaskProperties.getPoolSize();
        Integer queueCapacity = cloudOutboundCallTaskProperties.getQueueCapacity();
        return ExecutorFactory.INSTANCE.getExecutor(poolName, poolSize, queueCapacity);
    }

    private void originate(IvrOutboundTask ivrOutboundTask,
                           List<String> displayNumberList,
                           ScoredEntry<String> scoredEntry) throws Exception {
        String taskId = ivrOutboundTask.getTaskId();
        Integer attemptInterval = ivrOutboundTask.getAttemptInterval();
        String value = scoredEntry.getValue();
        Integer currentTime = Convert.toInt(scoredEntry.getScore());
        String outCallFlagKey = CacheUtil.getOutCallFlagKey(value);
        String flag = redissonUtil.get(outCallFlagKey);
        if (StrUtil.isNotEmpty(flag)) {
            return;
        }
        // 达到呼叫次数上限 移除zset, 接通移除zset
        int newTimes = currentTime + 1;
        if (newTimes > ivrOutboundTask.getMaxAttemptCount()) {
            commonDataOperateService.removeNumber(taskId, value, CallTaskEnum.IVR);
            return;
        }

        List<String> member = StrUtil.split(value, StrUtil.COLON);
        String numberId = member.get(0);
        String number = member.get(1);

        //  zset 确认上一次外呼时间 记录 timestamp
        if (currentTime > 1) {
            Long lastTimestamp = getLastTimestamp(taskId, value);
            if (Objects.nonNull(lastTimestamp)) {
                if ((System.currentTimeMillis() - lastTimestamp) / 1000 < attemptInterval) {
                    log.info("[ivr外呼] 号码: {}, 上一次外呼时间: {}, 未达到重呼间隔, 跳过.", value, lastTimestamp);
                    return;
                }
            }
        }

        // 开始外呼, 需要多线程
        String displayNumber = getDisplayNumber(taskId, displayNumberList);
        ClientOutboundCallTaskDTO callTaskDTO = new ClientOutboundCallTaskDTO();
        callTaskDTO.setReqId(IdUtil.fastUUID());
        callTaskDTO.setCompanyCode(ivrOutboundTask.getCompanyCode());
        callTaskDTO.setTaskId(ivrOutboundTask.getTaskId());
        // I don't think it's npe
        callTaskDTO.setTaskType(CallTaskEnum.parse(ivrOutboundTask.getTaskType()).name());
        callTaskDTO.setIvrId(ivrOutboundTask.getIvrId());
        callTaskDTO.setVoiceNotifyFileId(ivrOutboundTask.getVoiceNotifyFileId());
        callTaskDTO.setMaxRingTime(ivrOutboundTask.getMaxRingTime());
        callTaskDTO.setCallerNumber(displayNumber);
        callTaskDTO.setDisplayNumber(displayNumber);
        callTaskDTO.setCalleeNumber(number);
        callTaskDTO.setNumberId(numberId);
        callTaskDTO.setClientNumber(number);
        callTaskDTO.setPlatformNumber(displayNumber);
        callTaskDTO.setAudio(MediaStreamEnum.SENDRECV.getCode());
        callTaskDTO.setVideo(MediaStreamEnum.NONE.getCode());
        callTaskDTO.setCurrentTimes(newTimes);
        callTaskDTO.setMsgType(MsgTypeEnum.call_task.name());
        callTaskDTO.setMember(value);
        String data = objectMapper.writeValueAsString(callTaskDTO);
        log.info("[ivr外呼] 外呼参数: {}", data);
        ClientResponseBaseVO responseBaseVO = (ClientResponseBaseVO) callControlRemoteService.request(data);
        log.info("[ivr外呼] 外呼结果: {}", responseBaseVO);
        if (Objects.nonNull(responseBaseVO) && "0".equals(responseBaseVO.getCode())) {
            // db修改号码信息-呼叫状态, 接通状态, 呼叫次数, 发起呼叫时间,
            IvrOutboundTaskNumber ivrOutboundTaskNumber = new IvrOutboundTaskNumber();
            ivrOutboundTaskNumber.setNumberId(numberId);
            ivrOutboundTaskNumber.setCallStatus(1);
            ivrOutboundTaskNumber.setCallCount(newTimes);
            ivrOutboundTaskNumber.setCallTime(DateUtil.date());
            int update = ivrOutboundTaskNumberMapper.updateById(ivrOutboundTaskNumber);
            // cache号码外呼时间记录hash
            Boolean set = updateNumberCallCount(taskId, value, Convert.toDouble(newTimes));
            redissonUtil.set(outCallFlagKey, 1, Duration.ofSeconds(300));
            log.info("[ivr外呼] 号码: {}, 外呼成功, 更新号码信息, db: {}, {}", value, update, set);
        }
    }

    private boolean checkAllReachMaxAttempt(List<ScoredEntry<String>> ivrNumberList, IvrOutboundTask ivrOutboundTask) {
        ScoredEntry<String> scoredEntry = ivrNumberList.get(0);
        String value = scoredEntry.getValue();
        Integer currentTime = Convert.toInt(scoredEntry.getScore());
        // 最大外呼次数
        Integer maxAttemptCount = ivrOutboundTask.getMaxAttemptCount();
        if (currentTime >= maxAttemptCount) {
            // 所有号码 达到最大外呼次数, 停止任务
            stopCallTask(ivrOutboundTask.getTaskId(), ivrOutboundTask.getJobId());
            log.info("[ivr外呼] 号码: {}, 所有号码达到最大外呼次数: {}, 结束任务", value, currentTime);
            return true;
        }
        return false;
    }

    /**
     * 结束任务
     *
     * @param taskId 任务id
     */
    private void stopCallTask(String taskId, Integer jobId) {
        ivrOutboundTaskMapper.updateTaskState(taskId, CallTaskStatusEnum.END.getCode());
        // xxl停止任务
        if (Objects.isNull(jobId)) {
            return;
        }
        xxlJobInfoService.stopJob(jobId);
    }

    /**
     * 号码上一次外呼的时间戳
     *
     * @param taskId 任务id
     * @param item   号码
     * @return 上一次外呼的时间戳
     */
    private Long getLastTimestamp(String taskId, String item) {
        String ivrTaskNumberCallStatusKey = CacheUtil.getIvrTaskNumberCallStatusKey(taskId);
        Object data = redissonUtil.getHashByItem(ivrTaskNumberCallStatusKey, item);
        if (Objects.isNull(data)) {
            return null;
        }
        return Long.valueOf((String) data);
    }

    /**
     * 号码列表
     *
     * @param taskId 任务id
     * @param limit  外呼比例
     * @return 号码列表
     */
    private List<ScoredEntry<String>> getIvrNumberList(String taskId, Integer limit, Integer maxAttemptCount) {
        return dataQueryService.getIvrNumberList(taskId, limit, maxAttemptCount);
    }

    /**
     * 号码 呼叫次数更新
     */
    private Boolean updateNumberCallCount(String taskId, String member, Double score) {
        String ivrTaskNumberKey = CacheUtil.getIvrTaskNumberKey(taskId);
        return redissonUtil.addZset(ivrTaskNumberKey, member, score);
    }

    /**
     * 轮训获取外显号码
     */
    private String getDisplayNumber(String taskId, List<String> list) {
        return roundRobinLoadBalancer.get(taskId, list);
    }

    private XxlJobInfo createXxlJobInfo(XxlJobGroup xxlJobGroup, IvrOutboundTask ivrOutboundTask, Integer jobId) {
        XxlJobInfo xxlJobInfo = new XxlJobInfo();
        xxlJobInfo.setJobGroup(xxlJobGroup.getId());
        xxlJobInfo.setJobDesc(getJobDesc(ivrOutboundTask));
        xxlJobInfo.setAuthor(ivrOutboundTask.getTaskName());
        xxlJobInfo.setScheduleType("FIX_RATE");
        xxlJobInfo.setScheduleConf(Convert.toStr(ivrOutboundTask.getOutboundFrequency()));
        xxlJobInfo.setGlueType("BEAN");
        xxlJobInfo.setExecutorHandler(JOB_NAME);
        xxlJobInfo.setExecutorRouteStrategy(ExecutorRouteStrategyEnum.FAILOVER.name());
        xxlJobInfo.setMisfireStrategy(MisfireStrategyEnum.DO_NOTHING.name());
        xxlJobInfo.setExecutorBlockStrategy(ExecutorBlockStrategyEnum.DISCARD_LATER.name());
        xxlJobInfo.setExecutorTimeout(0);
        xxlJobInfo.setExecutorFailRetryCount(0);
        xxlJobInfo.setGlueRemark("");
        xxlJobInfo.setExecutorParam(ivrOutboundTask.getTaskId());
        xxlJobInfo.setTriggerStatus(0);
        if (CallTaskStatusEnum.ENABLE.getCode().equals(ivrOutboundTask.getTaskState())) {
            xxlJobInfo.setTriggerStatus(1);
        }
        if (Objects.nonNull(jobId)) {
            xxlJobInfo.setId(jobId);
        }
        return xxlJobInfo;
    }

    private String getJobDesc(IvrOutboundTask ivrOutboundTask) {
        String companyCode = ivrOutboundTask.getCompanyCode();
        String taskId = ivrOutboundTask.getTaskId();
        return StrUtil.join(StrUtil.DASHED, getCallTaskEnum(), companyCode, taskId, ivrOutboundTask.getTaskType());
    }
}
