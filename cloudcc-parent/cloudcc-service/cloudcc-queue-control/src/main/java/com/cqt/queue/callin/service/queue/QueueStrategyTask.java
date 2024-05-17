package com.cqt.queue.callin.service.queue;

import cn.hutool.core.util.IdUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.cqt.base.enums.agent.AgentServiceModeEnum;
import com.cqt.base.util.TraceIdUtil;
import com.cqt.cloudcc.manager.context.CompanyInfoContext;
import com.cqt.cloudcc.manager.service.CommonDataOperateService;
import com.cqt.model.common.CloudCallCenterProperties;
import com.cqt.model.queue.dto.TransferAgentQueueDTO;
import com.cqt.model.queue.dto.UserQueueUpDTO;
import com.cqt.queue.callin.service.DataQueryService;
import com.cqt.queue.callin.service.queue.matcher.CombineStrategyMatcher;
import com.cqt.queue.callin.service.queue.matcher.OfflinePhoneMatcher;
import com.cqt.queue.callin.service.queue.matcher.QueueDurationStrategyMatcher;
import com.cqt.queue.callin.service.queue.matcher.QueueTimeoutCheckMatcher;
import com.cqt.queue.config.QueueThreadPoolConfig;
import com.cqt.xxljob.annotations.XxlJobRegister;
import com.cqt.xxljob.enums.ExecutorRouteStrategyEnum;
import com.cqt.xxljob.util.XxlJobUtil;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.bytebuddy.matcher.ElementMatchers;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author linshiqiang
 * date:  2023-10-13 14:21
 * @since 7.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class QueueStrategyTask {

    private final DataQueryService dataQueryService;

    private final CommonDataOperateService commonDataOperateService;

    private final CloudCallCenterProperties cloudCallCenterProperties;

    @Resource(name = QueueThreadPoolConfig.POLLING_AGENT_POOL_NAME)
    private Executor pollingAgentExecutor;

    @SuppressWarnings("all")
    @XxlJobRegister(jobDesc = "呼入无空闲坐席-排队轮训",
            cron = "* * * * * ?",
            triggerStatus = 1,
            executorRouteStrategy = ExecutorRouteStrategyEnum.SHARDING_BROADCAST)
    @XxlJob("userQueuePollTask")
    public void userQueuePollTask() {
        Set<String> enableCompanyCode = commonDataOperateService.getEnableCompanyCode();
        List<String> list = enableCompanyCode.stream().sorted().collect(Collectors.toList());
        List<String> shardList = XxlJobUtil.getShardList(list);
        log.debug("[pollTask] company: {}", shardList);

        List<CompletableFuture<Void>> futureList = new ArrayList<>();
        for (String companyCode : shardList) {
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                try {
                    List<UserQueueUpDTO> userQueueUpList = dataQueryService.getUserQueueUpList(companyCode);
                    if (userQueueUpList.isEmpty()) {
                        return;
                    }
                    CompanyInfoContext.set(commonDataOperateService.getCompanyInfoDTO(companyCode));
                    TraceIdUtil.setTraceId(TraceIdUtil.buildTraceId(companyCode, IdUtil.objectId()));
                    while (true) {
                        boolean executeTask = executeTask(companyCode);
                        if (!executeTask) {
                            return;
                        }
                        long millis = cloudCallCenterProperties.getQueuePollingScheduleTime().toMillis();
                        TimeUnit.MILLISECONDS.sleep(millis);
                    }
                } catch (Exception e) {
                    log.error("[排队] executeTask 线程执行异常: ", e);
                } finally {
                    CompanyInfoContext.remove();
                    TraceIdUtil.remove();
                }
            }, pollingAgentExecutor);
            futureList.add(future);
        }
        CompletableFuture[] futures = new CompletableFuture[futureList.size()];
        futureList.toArray(futures);
        CompletableFuture.allOf(futures).join();
    }

    /**
     * 企业-执行任务
     *
     * @param companyCode 企业id
     */
    public boolean executeTask(String companyCode) {
        // 0. lock company_code
        try {
            // 2.1. 查询该企业下的空闲坐席队列
            List<TransferAgentQueueDTO> freeAgents = commonDataOperateService.getCompanyAgentQueue(companyCode,
                    AgentServiceModeEnum.CUSTOMER, true);
            // 2.2 查询正在排队的客户队列
            List<UserQueueUpDTO> userQueueUpList = dataQueryService.getUserQueueUpList(companyCode);
            int queueSize = userQueueUpList.size();
            int freeSize = freeAgents.size();
            if (queueSize == 0) {
                log.debug("[排队] 企业: {}, 排队客户: {} 个, 空闲坐席: {} 个, 没有客户在排队!",
                        companyCode, queueSize, freeSize);
                return false;
            }
            log.info("[排队] 企业: {}, 排队客户: {} 个, 空闲坐席: {} 个, 有客户在等待, 开始排队!",
                    companyCode, queueSize, freeSize);

            /*
             * 0. 验证排队客户, 是否超时, 是否已先挂断
             * 1. 查询空闲坐席
             * 2. 客户优先级最高的优先处理
             * 3. 客户 根据排队策略分组
             * 4. 组合策略(客户优先级, 坐席技能权值, 先进先出)-分配坐席
             * 5. 先进先出策略-分配坐席
             * 6. 离线坐席手机号分配
             */
            CallQueueContext callQueueContext = new CallQueueContext(companyCode, freeAgents, new ArrayList<>(userQueueUpList));
            ElementMatchers.any()
                    .and(SpringUtil.getBean(QueueTimeoutCheckMatcher.class))
                    .and(SpringUtil.getBean(CombineStrategyMatcher.class))
                    .and(SpringUtil.getBean(QueueDurationStrategyMatcher.class))
                    .and(SpringUtil.getBean(OfflinePhoneMatcher.class))
                    .matches(callQueueContext);
        } catch (Exception e) {
            log.error("[排队] 企业: {}, 排队异常: ", companyCode, e);
        }
        return true;
    }
}
