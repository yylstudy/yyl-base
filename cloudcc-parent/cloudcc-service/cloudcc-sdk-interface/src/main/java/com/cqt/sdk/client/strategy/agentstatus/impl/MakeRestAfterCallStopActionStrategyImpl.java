package com.cqt.sdk.client.strategy.agentstatus.impl;

import com.cqt.base.enums.agent.AgentStatusEnum;
import com.cqt.base.enums.agent.AgentStatusTransferActionEnum;
import com.cqt.model.agent.dto.AgentStatusDTO;
import com.cqt.model.client.dto.ClientChangeStatusDTO;
import com.cqt.model.client.vo.ClientChangeStatusVO;
import com.cqt.sdk.client.service.DataQueryService;
import com.cqt.sdk.client.service.DataStoreService;
import com.cqt.sdk.client.strategy.agentstatus.AgentStatusTransferStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * @author linshiqiang
 * date:  2023-07-18 10:36
 * 通话结束后自动进入小休
 * 提前修改设置坐席通话结束后状态, 在hangup事件使用, 修改坐席状态
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MakeRestAfterCallStopActionStrategyImpl extends AbstractAgentStatusChecker implements AgentStatusTransferStrategy {

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
        callingChecker(agentStatusDTO);
        // 修改坐席实时状态redis, 设置坐席状态到hash-实时监控使用
        agentStatusDTO.setAfterCallStopAgentStatus(AgentStatusEnum.REST.name());
        agentStatusDTO.setAfterCallStopAction(AgentStatusTransferActionEnum.MAKE_REST_AFTER_CALL_STOP.name());
        agentStatusDTO.setAfterCallStopRestMin(clientChangeStatusDTO.getRestMin());
        dataStoreService.updateActualAgentStatus(agentStatusDTO);

        return ClientChangeStatusVO.response(clientChangeStatusDTO, "0", "结束通话后进入小休状态!!");
    }

    @Override
    public AgentStatusTransferActionEnum getTransferActionEnum() {
        return AgentStatusTransferActionEnum.MAKE_REST_AFTER_CALL_STOP;
    }
}
