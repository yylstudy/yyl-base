package com.cqt.sdk.client.strategy.agentstatus.impl;

import com.cqt.base.enums.OperateTypeEnum;
import com.cqt.base.enums.agent.AgentServiceModeEnum;
import com.cqt.base.enums.agent.AgentStatusEnum;
import com.cqt.base.enums.agent.AgentStatusTransferActionEnum;
import com.cqt.model.agent.dto.AgentStatusDTO;
import com.cqt.model.agent.dto.AgentStatusTransferDTO;
import com.cqt.model.client.dto.ClientChangeStatusDTO;
import com.cqt.model.client.vo.ClientAgentStatusChangeVO;
import com.cqt.model.client.vo.ClientChangeStatusVO;
import com.cqt.sdk.client.event.agentstatus.FreeAgentQueueEvent;
import com.cqt.sdk.client.event.arrange.CallStopArrangeCancelEvent;
import com.cqt.sdk.client.event.mq.AgentStatusLogStoreEvent;
import com.cqt.sdk.client.service.DataQueryService;
import com.cqt.sdk.client.service.DataStoreService;
import com.cqt.sdk.client.strategy.agentstatus.AgentStatusTransferStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * @author linshiqiang
 * date:  2023-07-18 10:36
 * 强制示闲：忙碌、事后处理、小憩可发起强制示闲；
 * 强制示闲后提示：强制示闲成功；
 * 其他状态发起强制示闲提示：强制示闲失败，被操作坐席当前状态不可强制示闲
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ForceMakeFreeActionStrategyImpl extends AbstractAgentStatusChecker implements AgentStatusTransferStrategy {

    private final DataQueryService dataQueryService;

    private final DataStoreService dataStoreService;

    private final ApplicationContext applicationContext;

    @Override
    public AgentStatusTransferActionEnum getTransferActionEnum() {
        return AgentStatusTransferActionEnum.FORCE_MAKE_FREE;
    }

    @Override
    public ClientChangeStatusVO deal(ClientChangeStatusDTO clientChangeStatusDTO) throws Exception {
        // TODO 考虑抽离相同代码!!!
        String companyCode = clientChangeStatusDTO.getCompanyCode();
        String operatedAgentId = clientChangeStatusDTO.getOperatedAgentId();
        Optional<AgentStatusDTO> agentStatusOptional = dataQueryService.getAgentStatusDTO(companyCode, operatedAgentId);
        if (!agentStatusOptional.isPresent()) {
            return ClientChangeStatusVO.response(clientChangeStatusDTO, "1", "未查询到坐席状态信息, 请先确认是否已签入!");
        }
        AgentStatusDTO agentStatusDTO = agentStatusOptional.get();
        offlineChecker(agentStatusDTO);
        boolean enable = checkStatus(agentStatusDTO);
        if (!enable) {
            return ClientChangeStatusVO.response(clientChangeStatusDTO, "1", "强制示闲失败，被操作坐席当前状态不可强制示闲!");
        }

        long currentTimestamp = System.currentTimeMillis();
        // 坐席实时状态
        agentStatusDTO.setNull();
        agentStatusDTO.setSourceStatus(agentStatusDTO.getTargetStatus());
        agentStatusDTO.setSourceSubStatus(agentStatusDTO.getTargetSubStatus());
        agentStatusDTO.setSourceDuration(agentStatusDTO.getTargetDuration());
        agentStatusDTO.setSourceTimestamp(agentStatusDTO.getTargetTimestamp());
        agentStatusDTO.setCheckoutTime(currentTimestamp);
        agentStatusDTO.setTargetStatus(AgentStatusEnum.FREE.name());
        agentStatusDTO.setTargetTimestamp(currentTimestamp);
        agentStatusDTO.setTransferAction(AgentStatusTransferActionEnum.FORCE_MAKE_FREE.name());
        agentStatusDTO.setReason("被管理员强制示闲");
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
                .transferAction(AgentStatusTransferActionEnum.FORCE_MAKE_FREE.name())
                .targetStatus(AgentStatusEnum.FREE.name())
                .targetSubStatus("")
                .targetTimestamp(currentTimestamp)
                .build();
        applicationContext.publishEvent(new AgentStatusLogStoreEvent(this, agentStatusTransferDTO));

        Integer serviceMode = agentStatusDTO.getServiceMode();
        AgentServiceModeEnum serviceModeEnum = AgentServiceModeEnum.parse(serviceMode);
        // 坐席空闲坐席-新增
        applicationContext.publishEvent(new FreeAgentQueueEvent(this,
                companyCode, operatedAgentId, serviceModeEnum, currentTimestamp, OperateTypeEnum.INSERT));
        log.info("[强制示闲] 企业坐席空闲队列-新增, 企业: {}, 坐席: {}", companyCode, operatedAgentId);

        // 给被操作坐席发送消息
        dataStoreService.notifySdkAgentStatus(ClientAgentStatusChangeVO.build(agentStatusDTO));

        // 移除事后处理任务
        applicationContext.publishEvent(new CallStopArrangeCancelEvent(this, companyCode, operatedAgentId));
        return ClientChangeStatusVO.response(clientChangeStatusDTO, agentStatusDTO, "0", "强制示闲成功!!");
    }

    /**
     * 忙碌、事后处理、小憩可发起强制示闲；
     */
    private Boolean checkStatus(AgentStatusDTO agentStatusDTO) {
        String targetStatus = agentStatusDTO.getTargetStatus();
        return AgentStatusEnum.BUSY.name().equals(targetStatus)
                || AgentStatusEnum.REST.name().equals(targetStatus)
                || AgentStatusEnum.ARRANGE.name().equals(targetStatus);
    }

}
