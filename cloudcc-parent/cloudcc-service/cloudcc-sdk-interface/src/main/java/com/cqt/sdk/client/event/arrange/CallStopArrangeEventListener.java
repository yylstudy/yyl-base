package com.cqt.sdk.client.event.arrange;

import com.cqt.base.enums.OperateTypeEnum;
import com.cqt.base.enums.agent.AgentServiceModeEnum;
import com.cqt.base.enums.agent.AgentStatusEnum;
import com.cqt.base.enums.agent.AgentStatusTransferActionEnum;
import com.cqt.base.initialize.ApplicationInfoInitialize;
import com.cqt.base.util.CacheUtil;
import com.cqt.cloudcc.manager.service.CommonDataOperateService;
import com.cqt.model.agent.bo.AgentStatusTransferBO;
import com.cqt.model.agent.dto.AgentStatusDTO;
import com.cqt.model.client.dto.ClientCheckinDTO;
import com.cqt.model.client.vo.ClientAgentStatusChangeVO;
import com.cqt.rpc.sdk.SdkInterfaceRemoteService;
import com.cqt.sdk.client.event.agentstatus.FreeAgentQueueEvent;
import com.cqt.sdk.client.event.agentstatus.OfflineAgentQueueEvent;
import com.cqt.sdk.client.service.DataStoreService;
import com.cqt.starter.redis.util.RedissonUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * @author linshiqiang
 * date:  2023-07-26 15:26
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CallStopArrangeEventListener implements ApplicationListener<CallStopArrangeEvent> {

    private static final HashedWheelTimer TIMER = new HashedWheelTimer();

    private final CommonDataOperateService commonDataOperateService;

    private final DataStoreService dataStoreService;

    private final SdkInterfaceRemoteService sdkInterfaceRemoteService;

    private final ObjectMapper objectMapper;

    private final RedissonUtil redissonUtil;

    private final ApplicationContext applicationContext;

    @Override
    public void onApplicationEvent(@Nonnull CallStopArrangeEvent event) {
        // 在坐席挂断事件开始处理
        // 先查询坐席配置  是否开启事后处理和秒数
        // 开启 则修改状态为事后处理, 开启一个定时器, 等事后处理结束进入通话前的状态,
        // 假设事后处理倒计时未结束, 手动修改了状态, 定时器需要丢弃, 如何标记?
        // 在发起外呼或呼入时设置通话前的状态
        try {
            Integer arrangeSecond = event.getArrangeSecond();
            if (Objects.nonNull(event.getClientCheckinDTO())) {
                checkin(event);
                return;
            }
            if (Objects.nonNull(arrangeSecond)) {
                AgentStatusTransferBO agentStatusTransferBO = event.getAgentStatusTransferBO();
                String companyCode = agentStatusTransferBO.getCompanyCode();
                String agentId = agentStatusTransferBO.getAgentId();
                Timeout task = TIMER.newTimeout(timeout -> {
                    try {
                        log.info("[事后处理任务] data: {}", objectMapper.writeValueAsString(agentStatusTransferBO));
                        // arrange后应该是空闲或忙碌 小休, 不可能是振铃
                        AgentStatusEnum targetStatus = agentStatusTransferBO.getTargetStatus();
                        if (AgentStatusEnum.RINGING.equals(targetStatus)) {
                            agentStatusTransferBO.setTargetStatus(AgentStatusEnum.BUSY);
                            agentStatusTransferBO.setTargetSubStatus(null);
                        }
                        if (AgentStatusEnum.ARRANGE.equals(targetStatus)) {
                            agentStatusTransferBO.setTargetStatus(AgentStatusEnum.FREE);
                            agentStatusTransferBO.setTargetSubStatus(null);
                        }
                        sdkInterfaceRemoteService.agentStatusChangeTransfer(agentStatusTransferBO);
                    } catch (Exception e) {
                        Objects.requireNonNull(log).error("[事后处理任务] 调用坐席状态迁移接口, 执行失败: ", e);
                    }
                }, arrangeSecond, TimeUnit.SECONDS);
                String serverIp = ApplicationInfoInitialize.SERVER_IP;
                String arrangeTaskKey = CacheUtil.getArrangeTaskKey(companyCode, agentId);
                ArrangeTaskCache.add(companyCode, agentId, task);
                log.info("[事后处理任务] add task cache, serverIp: {}, arrangeTaskKey: {}, cache size: {}",
                        serverIp, arrangeTaskKey, ArrangeTaskCache.size());
                redissonUtil.set(arrangeTaskKey, serverIp, arrangeSecond + 5, TimeUnit.SECONDS);
            }
        } catch (Exception e) {
            log.error("[事后处理任务] 添加任务异常: ", e);
        }
    }

    private void checkin(CallStopArrangeEvent event) {
        Integer arrangeSecond = event.getArrangeSecond();
        ClientCheckinDTO clientCheckinDTO = event.getClientCheckinDTO();
        String companyCode = clientCheckinDTO.getCompanyCode();
        String agentId = clientCheckinDTO.getAgentId();
        Timeout task = TIMER.newTimeout(timeout -> {
            // arrange -> free
            Optional<AgentStatusDTO> agentStatusOptional = commonDataOperateService.getActualAgentStatus(companyCode, agentId);
            if (agentStatusOptional.isPresent()) {
                AgentStatusDTO agentStatusDTO = agentStatusOptional.get();
                agentStatusDTO.setSourceStatus(agentStatusDTO.getTargetStatus());
                agentStatusDTO.setSourceSubStatus(agentStatusDTO.getTargetSubStatus());
                agentStatusDTO.setSourceDuration(agentStatusDTO.getTargetDuration());
                agentStatusDTO.setSourceTimestamp(agentStatusDTO.getTargetTimestamp());
                agentStatusDTO.setTargetStatus(AgentStatusEnum.FREE.name());
                agentStatusDTO.setTransferAction(AgentStatusTransferActionEnum.RECOVER.name());
                ClientAgentStatusChangeVO agentStatusChangeVO = ClientAgentStatusChangeVO.build(agentStatusDTO);
                dataStoreService.updateActualAgentStatus(agentStatusDTO);
                dataStoreService.notifySdkAgentStatus(agentStatusChangeVO);
                AgentServiceModeEnum serviceMode = AgentServiceModeEnum.parse(agentStatusDTO.getServiceMode());
                applicationContext.publishEvent(new FreeAgentQueueEvent(this,
                        companyCode, agentId, serviceMode, System.currentTimeMillis(), OperateTypeEnum.INSERT));
                applicationContext.publishEvent(new OfflineAgentQueueEvent(this,
                        companyCode, agentId, serviceMode, System.currentTimeMillis(), OperateTypeEnum.DELETE));
            }
        }, arrangeSecond, TimeUnit.SECONDS);
        String serverIp = ApplicationInfoInitialize.SERVER_IP;
        String arrangeTaskKey = CacheUtil.getArrangeTaskKey(companyCode, agentId);
        ArrangeTaskCache.add(companyCode, agentId, task);
        log.info("[事后处理任务] checkin add task cache, serverIp: {}, arrangeTaskKey: {}, cache size: {}",
                serverIp, arrangeTaskKey, ArrangeTaskCache.size());
        redissonUtil.set(arrangeTaskKey, serverIp, arrangeSecond + 5, TimeUnit.SECONDS);
    }

}
