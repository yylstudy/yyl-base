package com.cqt.sdk.client.strategy.agentstatus.impl;

import com.cqt.base.enums.OperateTypeEnum;
import com.cqt.base.enums.agent.AgentServiceModeEnum;
import com.cqt.base.enums.agent.AgentStatusEnum;
import com.cqt.base.enums.agent.AgentStatusTransferActionEnum;
import com.cqt.model.agent.dto.AgentStatusDTO;
import com.cqt.model.agent.dto.AgentStatusTransferDTO;
import com.cqt.model.client.dto.ClientChangeStatusDTO;
import com.cqt.model.client.vo.ClientChangeStatusVO;
import com.cqt.sdk.client.converter.ModelConverter;
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
 * 示闲操作
 * 页面点击
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MakeFreeActionStrategyImpl extends AbstractAgentStatusChecker implements AgentStatusTransferStrategy {

    private final ApplicationContext applicationContext;

    private final DataQueryService dataQueryService;

    private final DataStoreService dataStoreService;

    @Override
    public ClientChangeStatusVO deal(ClientChangeStatusDTO clientChangeStatusDTO) throws Exception {
        String companyCode = clientChangeStatusDTO.getCompanyCode();
        String agentId = clientChangeStatusDTO.getAgentId();
        Optional<AgentStatusDTO> agentStatusOptional = dataQueryService.getAgentStatusDTO(companyCode, agentId);
        if (!agentStatusOptional.isPresent()) {
            return ClientChangeStatusVO.response(clientChangeStatusDTO, "1", "未查询到坐席状态信息, 请先确认是否已迁入!");
        }
        AgentStatusDTO agentStatusDTO = agentStatusOptional.get();
        offlineChecker(agentStatusDTO);
        if (AgentStatusEnum.FREE.name().equals(agentStatusDTO.getTargetStatus())) {
            return ClientChangeStatusVO.response(clientChangeStatusDTO, "1", "当前坐席已经是空闲状态, 示闲失败!");
        }

        // 修改坐席实时状态redis, 设置坐席状态到hash-实时监控使用
        long timestamp = System.currentTimeMillis();
        agentStatusDTO.setSourceStatus(agentStatusDTO.getTargetStatus());
        agentStatusDTO.setSourceSubStatus(agentStatusDTO.getTargetSubStatus());
        agentStatusDTO.setSourceDuration(agentStatusDTO.getTargetDuration());
        agentStatusDTO.setSourceTimestamp(agentStatusDTO.getTargetTimestamp());
        agentStatusDTO.setTargetStatus(AgentStatusEnum.FREE.name());
        agentStatusDTO.setTransferAction(AgentStatusTransferActionEnum.MAKE_FREE.name());
        agentStatusDTO.setTargetDuration(clientChangeStatusDTO.getRestMin());
        agentStatusDTO.setTargetTimestamp(timestamp);
        dataStoreService.updateActualAgentStatus(agentStatusDTO);

        Integer serviceMode = agentStatusDTO.getServiceMode();
        AgentServiceModeEnum serviceModeEnum = AgentServiceModeEnum.parse(serviceMode);
        // 企业坐席空闲队列-添加
        applicationContext.publishEvent(new FreeAgentQueueEvent(this, companyCode, agentId,
                serviceModeEnum, timestamp, OperateTypeEnum.INSERT));
        log.info("[示闲] 企业坐席空闲队列-添加, 企业: {}, 坐席: {}", companyCode, agentId);

        // 发送spring事件进行入库
        AgentStatusTransferDTO agentStatusTransferDTO = ModelConverter.INSTANCE.status2transfer(agentStatusDTO);
        applicationContext.publishEvent(new AgentStatusLogStoreEvent(this, agentStatusTransferDTO));

        // 移除事后处理任务
        applicationContext.publishEvent(new CallStopArrangeCancelEvent(this, companyCode, agentId));
        return ClientChangeStatusVO.response(clientChangeStatusDTO, agentStatusDTO, "0", "示闲成功!");
    }

    @Override
    public AgentStatusTransferActionEnum getTransferActionEnum() {
        return AgentStatusTransferActionEnum.MAKE_FREE;
    }
}
