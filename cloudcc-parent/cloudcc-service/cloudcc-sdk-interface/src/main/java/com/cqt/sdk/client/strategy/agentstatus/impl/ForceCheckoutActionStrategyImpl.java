package com.cqt.sdk.client.strategy.agentstatus.impl;

import cn.hutool.core.util.StrUtil;
import com.cqt.base.enums.OperateTypeEnum;
import com.cqt.base.enums.agent.AgentServiceModeEnum;
import com.cqt.base.enums.agent.AgentStatusEnum;
import com.cqt.base.enums.agent.AgentStatusTransferActionEnum;
import com.cqt.base.enums.cdr.HangupCauseEnum;
import com.cqt.cloudcc.manager.service.CommonDataOperateService;
import com.cqt.cloudcc.manager.service.FreeswitchRequestService;
import com.cqt.model.agent.dto.AgentStatusDTO;
import com.cqt.model.agent.dto.AgentStatusTransferDTO;
import com.cqt.model.agent.vo.CallUuidContext;
import com.cqt.model.client.dto.ClientChangeStatusDTO;
import com.cqt.model.client.vo.ClientAgentStatusChangeVO;
import com.cqt.model.client.vo.ClientChangeStatusVO;
import com.cqt.model.freeswitch.dto.api.HangupDTO;
import com.cqt.sdk.client.event.agentstatus.FreeAgentQueueEvent;
import com.cqt.sdk.client.event.agentstatus.OfflineAgentQueueEvent;
import com.cqt.sdk.client.event.arrange.CallStopArrangeCancelEvent;
import com.cqt.sdk.client.event.mq.AgentStatusLogStoreEvent;
import com.cqt.sdk.client.service.DataQueryService;
import com.cqt.sdk.client.service.DataStoreService;
import com.cqt.sdk.client.strategy.agentstatus.AgentStatusTransferStrategy;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;

/**
 * @author linshiqiang
 * date:  2023-07-18 10:36
 * 强签：任意状态都可进行强制签出
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ForceCheckoutActionStrategyImpl extends AbstractAgentStatusChecker implements AgentStatusTransferStrategy {

    private final ObjectMapper objectMapper;

    private final ApplicationContext applicationContext;

    private final DataQueryService dataQueryService;

    private final DataStoreService dataStoreService;

    private final FreeswitchRequestService freeswitchRequestService;

    private final CommonDataOperateService commonDataOperateService;

    @Override
    public ClientChangeStatusVO deal(ClientChangeStatusDTO clientChangeStatusDTO) throws Exception {
        String companyCode = clientChangeStatusDTO.getCompanyCode();
        String operatedAgentId = clientChangeStatusDTO.getOperatedAgentId();
        Optional<AgentStatusDTO> agentStatusOptional = dataQueryService.getAgentStatusDTO(companyCode, operatedAgentId);
        if (!agentStatusOptional.isPresent()) {
            return ClientChangeStatusVO.response(clientChangeStatusDTO, "1", "未查询到坐席状态信息, 请先确认是否已签入!");
        }

        AgentStatusDTO agentStatusDTO = agentStatusOptional.get();
        // offlineChecker(agentStatusDTO);
        if (log.isInfoEnabled()) {
            log.info("[强签] 当前坐席状态: {}", objectMapper.writeValueAsString(agentStatusDTO));
        }
        // 中断话务
        stopCall(agentStatusDTO);

        long currentTimestamp = System.currentTimeMillis();
        // 坐席实时状态
        agentStatusDTO.setNull();
        agentStatusDTO.setSourceStatus(agentStatusDTO.getTargetStatus());
        agentStatusDTO.setSourceSubStatus(agentStatusDTO.getTargetSubStatus());
        agentStatusDTO.setSourceDuration(agentStatusDTO.getTargetDuration());
        agentStatusDTO.setSourceTimestamp(agentStatusDTO.getTargetTimestamp());
        agentStatusDTO.setCheckoutTime(currentTimestamp);
        agentStatusDTO.setTargetStatus(AgentStatusEnum.OFFLINE.name());
        agentStatusDTO.setTargetTimestamp(currentTimestamp);
        agentStatusDTO.setTransferAction(AgentStatusTransferActionEnum.FORCE_CHECKOUT.name());
        agentStatusDTO.setReason("被管理员强制签出");
        dataStoreService.updateActualAgentStatus(agentStatusDTO);

        // 迁出操作完成, 发送迁出日志记录事件
        AgentStatusTransferDTO agentStatusTransferDTO = AgentStatusTransferDTO.builder()
                .companyCode(agentStatusDTO.getCompanyCode())
                .uuid("")
                .agentId(agentStatusDTO.getAgentId())
                .extId(agentStatusDTO.getExtId())
                .os(agentStatusDTO.getOs())
                .sourceStatus(agentStatusDTO.getSourceStatus())
                .sourceSubStatus(agentStatusDTO.getSourceSubStatus())
                .sourceTimestamp(agentStatusDTO.getSourceTimestamp())
                .sourceDuration(agentStatusDTO.getSourceDuration())
                .transferAction(AgentStatusTransferActionEnum.FORCE_CHECKOUT.name())
                .targetStatus(AgentStatusEnum.OFFLINE.name())
                .targetSubStatus("")
                .targetTimestamp(currentTimestamp)
                .build();
        applicationContext.publishEvent(new AgentStatusLogStoreEvent(this, agentStatusTransferDTO));

        Integer serviceMode = agentStatusDTO.getServiceMode();
        AgentServiceModeEnum serviceModeEnum = AgentServiceModeEnum.parse(serviceMode);

        // 企业空闲坐席-移除
        applicationContext.publishEvent(new FreeAgentQueueEvent(this,
                companyCode, operatedAgentId, currentTimestamp, OperateTypeEnum.DELETE));
        log.info("[强制签出] 企业坐席空闲队列-删除, 企业: {}, 坐席: {}", companyCode, operatedAgentId);

        // 企业坐席离线队列-新增
        applicationContext.publishEvent(new OfflineAgentQueueEvent(this,
                companyCode, agentStatusDTO.getAgentId(), serviceModeEnum, currentTimestamp, OperateTypeEnum.INSERT));
        log.info("[强制签出] 企业坐席离线队列-新增, 企业: {}, 坐席: {}", companyCode, agentStatusDTO.getAgentId());

        // 给被操作坐席发送消息
        dataStoreService.notifySdkAgentStatus(ClientAgentStatusChangeVO.build(agentStatusDTO));

        // 移除事后处理任务
        applicationContext.publishEvent(new CallStopArrangeCancelEvent(this, companyCode, operatedAgentId));
        return ClientChangeStatusVO.response(clientChangeStatusDTO, agentStatusDTO, "0", "强签成功!!");
    }

    /**
     * 中断话务
     *
     * @param agentStatusDTO 坐席状态
     */
    private void stopCall(AgentStatusDTO agentStatusDTO) {
        String targetStatus = agentStatusDTO.getTargetStatus();
        if (AgentStatusEnum.CALLING.name().equals(targetStatus)
                || AgentStatusEnum.RINGING.name().equals(targetStatus)) {
            String companyCode = agentStatusDTO.getCompanyCode();
            String uuid = agentStatusDTO.getUuid();
            // 坐席离线
            CallUuidContext callUuidContext = commonDataOperateService.getCallUuidContext(companyCode, uuid);
            if (Objects.isNull(callUuidContext)) {
                return;
            }
            callUuidContext.getCurrent().setCheckoutToOffline(true);
            commonDataOperateService.saveCallUuidContext(callUuidContext);
            log.info("[强签] 企业: {}, uuid: {}, 挂断话务", companyCode, uuid);

            if (StrUtil.isEmpty(uuid)) {
                return;
            }
            //  挂断分机通话
            HangupDTO hangupDTO = HangupDTO.build(companyCode, true, uuid, HangupCauseEnum.FORCE_CHECKOUT);
            freeswitchRequestService.hangup(hangupDTO);
        }
    }

    @Override
    public AgentStatusTransferActionEnum getTransferActionEnum() {
        return AgentStatusTransferActionEnum.FORCE_CHECKOUT;
    }
}
