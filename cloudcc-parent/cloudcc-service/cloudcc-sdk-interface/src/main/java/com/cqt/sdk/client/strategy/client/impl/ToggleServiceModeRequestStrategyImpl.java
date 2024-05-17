package com.cqt.sdk.client.strategy.client.impl;

import com.cqt.base.enums.MsgTypeEnum;
import com.cqt.base.enums.OperateTypeEnum;
import com.cqt.base.enums.SdkErrCode;
import com.cqt.base.enums.agent.AgentServiceModeEnum;
import com.cqt.cloudcc.manager.service.CommonDataOperateService;
import com.cqt.model.agent.dto.AgentStatusDTO;
import com.cqt.model.client.base.ClientResponseBaseVO;
import com.cqt.model.client.dto.ClientToggleServiceModeDTO;
import com.cqt.model.client.vo.ClientToggleServiceModeVO;
import com.cqt.sdk.client.event.agentstatus.FreeAgentQueueEvent;
import com.cqt.sdk.client.event.agentstatus.OfflineAgentQueueEvent;
import com.cqt.sdk.client.strategy.client.ClientRequestStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * @author linshiqiang
 * date:  2023-07-03 14:42
 * 切换坐席服务模式
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ToggleServiceModeRequestStrategyImpl extends AbstractClientChecker implements ClientRequestStrategy {

    private final ApplicationContext applicationContext;

    private final CommonDataOperateService commonDataOperateService;

    @Override
    public MsgTypeEnum getMsgType() {
        return MsgTypeEnum.toggle_service_mode;
    }

    @Override
    public ClientResponseBaseVO deal(String requestBody) throws Exception {
        ClientToggleServiceModeDTO modeDTO = convert(requestBody, ClientToggleServiceModeDTO.class);
        String companyCode = modeDTO.getCompanyCode();
        String agentId = modeDTO.getAgentId();
        long currentTimeMillis = System.currentTimeMillis();
        Optional<AgentStatusDTO> agentStatusOptional = commonDataOperateService.getActualAgentStatus(companyCode, agentId);
        if (!agentStatusOptional.isPresent()) {
            return ClientResponseBaseVO.response(modeDTO, SdkErrCode.AGENT_NOT_CHECK_IN);
        }
        AgentStatusDTO agentStatusDTO = agentStatusOptional.get();
        if (agentStatusDTO.getServiceMode().equals(modeDTO.getServiceMode())) {
            return ClientResponseBaseVO.response(modeDTO, SdkErrCode.AGENT_SERVICE_MODE_NOT_CHANGE);
        }
        AgentServiceModeEnum lastServiceMode = AgentServiceModeEnum.parse(agentStatusDTO.getServiceMode());
        // update agent status
        agentStatusDTO.setServiceMode(modeDTO.getServiceMode());
        commonDataOperateService.updateActualAgentStatus(agentStatusDTO);

        // 源
        // 删除离线坐席 队列
        applicationContext.publishEvent(new OfflineAgentQueueEvent(this,
                companyCode, agentId, lastServiceMode, currentTimeMillis, OperateTypeEnum.DELETE));
        // 删除空闲坐席 队列
        applicationContext.publishEvent(new FreeAgentQueueEvent(this, companyCode, agentId,
                lastServiceMode, currentTimeMillis, OperateTypeEnum.DELETE));

        // 目标
        AgentServiceModeEnum serviceMode = AgentServiceModeEnum.parse(modeDTO.getServiceMode());
        // 新增离线坐席 队列
        applicationContext.publishEvent(new OfflineAgentQueueEvent(this,
                companyCode, agentId, serviceMode, currentTimeMillis, OperateTypeEnum.INSERT));
        // 新增空闲坐席 队列
        applicationContext.publishEvent(new FreeAgentQueueEvent(this, companyCode, agentId,
                serviceMode, currentTimeMillis, OperateTypeEnum.INSERT));

        return ClientToggleServiceModeVO.response(modeDTO, SdkErrCode.OK);
    }
}
