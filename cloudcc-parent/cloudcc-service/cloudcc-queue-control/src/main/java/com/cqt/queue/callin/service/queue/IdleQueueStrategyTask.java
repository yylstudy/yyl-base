package com.cqt.queue.callin.service.queue;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.cqt.base.enums.IdleStrategyEnum;
import com.cqt.base.enums.agent.AgentServiceModeEnum;
import com.cqt.base.model.ResultVO;
import com.cqt.base.util.CacheUtil;
import com.cqt.base.util.TraceIdUtil;
import com.cqt.cloudcc.manager.context.CompanyInfoContext;
import com.cqt.cloudcc.manager.service.CommonDataOperateService;
import com.cqt.model.common.CloudCallCenterProperties;
import com.cqt.model.queue.dto.TransferAgentQueueDTO;
import com.cqt.model.queue.dto.UserQueueUpDTO;
import com.cqt.model.queue.vo.CallInIvrActionVO;
import com.cqt.model.skill.entity.SkillInfo;
import com.cqt.queue.callin.service.DataStoreService;
import com.cqt.queue.callin.service.idle.IdleStrategyFactory;
import com.cqt.queue.config.QueueThreadPoolConfig;
import com.cqt.starter.redis.util.RedissonUtil;
import com.cqt.xxljob.annotations.XxlJobRegister;
import com.cqt.xxljob.enums.ExecutorRouteStrategyEnum;
import com.cqt.xxljob.util.XxlJobUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

/**
 * @author linshiqiang
 * date:  2023-10-13 14:21
 * @since 7.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class IdleQueueStrategyTask {

    private final DataStoreService dataStoreService;

    private final CommonDataOperateService commonDataOperateService;

    private final RedissonUtil redissonUtil;

    private final IdleStrategyFactory idleStrategyFactory;

    private final ObjectMapper objectMapper;

    private final CloudCallCenterProperties cloudCallCenterProperties;

    @Resource(name = QueueThreadPoolConfig.IDLE_POOL_NAME)
    private Executor idlePollExecutor;

    @Resource(name = QueueThreadPoolConfig.IDLE_SKILL_POOL_NAME)
    private Executor idleSkillPollExecutor;

    @SuppressWarnings("all")
    @XxlJobRegister(jobDesc = "呼入执行闲时策略",
            cron = "* * * * * ?",
            triggerStatus = 1,
            executorRouteStrategy = ExecutorRouteStrategyEnum.SHARDING_BROADCAST)
    @XxlJob("idlePollTask")
    public void idlePollTask() {
        Set<String> enableCompanyCode = commonDataOperateService.getEnableCompanyCode();
        List<String> list = enableCompanyCode.stream().sorted().collect(Collectors.toList());
        List<String> shardList = XxlJobUtil.getShardList(list);
        log.debug("[idle-pollTask] company: {}", shardList);

        List<CompletableFuture<Void>> futureList = new ArrayList<>();
        for (String companyCode : shardList) {
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                try {
                    CompanyInfoContext.set(commonDataOperateService.getCompanyInfoDTO(companyCode));
                    TraceIdUtil.setTraceId(TraceIdUtil.buildTraceId(companyCode, IdUtil.objectId()));
                    executeTask(companyCode);
                } catch (Exception e) {
                    log.error("[排队] executeTask 线程执行异常: ", e);
                } finally {
                    CompanyInfoContext.remove();
                    TraceIdUtil.remove();
                }
            }, idlePollExecutor);
            futureList.add(future);
        }
        CompletableFuture[] futures = new CompletableFuture[futureList.size()];
        futureList.toArray(futures);
        CompletableFuture.allOf(futures).join();
        log.debug("[idle-pollTask] end");
    }

    /**
     * 企业-执行任务
     *
     * @param companyCode 企业id
     */
    @SuppressWarnings("all")
    public boolean executeTask(String companyCode) {
        Map<String, String> skillMap = redissonUtil.readAllHash(companyCode + ":all:skill");
        if (CollUtil.isEmpty(skillMap)) {
            return false;
        }
        Set<String> skillIdSet = skillMap.keySet();
        List<String> skillIdList = skillIdSet.stream().sorted().collect(Collectors.toList());

        List<CompletableFuture<Void>> futureList = new ArrayList<>();
        for (String skillId : skillIdList) {
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                try {
                    /*
                     * free queue
                     * skill idl strategy
                     * call time stats
                     * call count stats
                     */
                    String skillInfoKey = CacheUtil.getSkillInfoKey(skillId);
                    String userQueueIdleKey = CacheUtil.getUserQueueIdleKey(companyCode, skillId);
                    SkillInfo skillInfo = null;
                    List<TransferAgentQueueDTO> freeAgents = null;
                    List<String> callTimeList = null;
                    List<String> callCountList = null;
                    long start = System.currentTimeMillis();
                    while (true) {
                        Duration idleTime = cloudCallCenterProperties.getDefaultConfig().getIdleTime();
                        if (System.currentTimeMillis() - start > idleTime.toMillis()) {
                            return;
                        }
                        String data = redissonUtil.pollBlockList(userQueueIdleKey);
                        if (StrUtil.isEmpty(data)) {
                            return;
                        }
                        UserQueueUpDTO userQueueUpDTO = objectMapper.readValue(data, UserQueueUpDTO.class);
                        boolean enable = checkEnable(userQueueUpDTO);
                        if (!enable) {
                            continue;
                        }
                        if (Objects.isNull(skillInfo)) {
                            skillInfo = redissonUtil.get(skillInfoKey, SkillInfo.class);
                        }
                        IdleStrategyEnum idleStrategy = IdleStrategyEnum.parse(skillInfo.getIdleStrategy());
                        if (CollUtil.isEmpty(freeAgents)) {
                            freeAgents = commonDataOperateService.getSkillAgentQueue(companyCode, skillId,
                                    AgentServiceModeEnum.CUSTOMER, true);
                        }
                        if (CollUtil.isEmpty(freeAgents)) {
                            dataStoreService.noneFreeAgentAndEnterQueue(userQueueUpDTO);
                            log.info("[idle] 企业: {}, 技能: {}, 来电uuid: {}, 无空闲坐席,加入排队.",
                                    companyCode, skillId, userQueueUpDTO.getUuid());
                            continue;
                        }
                        if (IdleStrategyEnum.TODAY_LEAST_CALL_COUNT.equals(idleStrategy)
                                || IdleStrategyEnum.AGENT_WEIGHT_AND_TODAY_LEAST_CALL_COUNT.equals(idleStrategy)) {
                            if (CollUtil.isEmpty(callCountList)) {
                                callCountList = commonDataOperateService.getCallCount(companyCode, skillId);
                            }
                        }
                        if (IdleStrategyEnum.TODAY_LEAST_CALL_TIME.equals(idleStrategy)
                                || IdleStrategyEnum.AGENT_WEIGHT_AND_TODAY_LEAST_CALL_TIME.equals(idleStrategy)) {
                            if (CollUtil.isEmpty(callTimeList)) {
                                callTimeList = commonDataOperateService.getCallTime(companyCode, skillId);
                            }
                        }
                        ResultVO<CallInIvrActionVO> resultVO = idleStrategyFactory.executeIdle(userQueueUpDTO, freeAgents, callTimeList, callCountList);
                        if (Objects.nonNull(resultVO) && resultVO.success()) {
                            continue;
                        }
                        dataStoreService.noneFreeAgentAndEnterQueue(userQueueUpDTO);
                        log.info("[idle] 企业: {}, 技能: {}, 来电uuid: {}, 分配失败: {}, 加入排队.",
                                companyCode, skillId, userQueueUpDTO.getUuid(), resultVO);
                    }
                } catch (Exception e) {
                    log.error("[idle] executeTask 线程执行异常: ", e);
                }
            }, idleSkillPollExecutor);
            futureList.add(future);
        }
        CompletableFuture[] futures = new CompletableFuture[futureList.size()];
        futureList.toArray(futures);
        CompletableFuture.allOf(futures).join();
        return true;
    }

    private boolean checkEnable(UserQueueUpDTO userQueueUpDTO) {
        String companyCode = userQueueUpDTO.getCompanyCode();
        String uuid = userQueueUpDTO.getUuid();
        String callerNumber = userQueueUpDTO.getCallerNumber();
        log.info("[idle-checkEnable] 企业: {}, 来电号码: {}, uuid: {}", companyCode, callerNumber, uuid);
        // 是否已挂断
        Boolean hangup = commonDataOperateService.isHangup(companyCode, uuid);
        if (Boolean.TRUE.equals(hangup)) {
            log.info("[idle-checkEnable] 企业: {}, 来电号码: {}, uuid: {}, 已挂断", companyCode, callerNumber, uuid);
            return false;
        }
        return true;
    }

}
