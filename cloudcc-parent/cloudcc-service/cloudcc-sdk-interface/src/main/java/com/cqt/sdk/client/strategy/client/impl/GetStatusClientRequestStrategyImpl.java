package com.cqt.sdk.client.strategy.client.impl;

import com.cqt.base.enums.MsgTypeEnum;
import com.cqt.cloudcc.manager.service.CallContextService;
import com.cqt.model.agent.dto.AgentStatusDTO;
import com.cqt.model.client.base.ClientResponseBaseVO;
import com.cqt.model.client.dto.ClientGetStatusDTO;
import com.cqt.model.client.vo.ClientGetStatusVO;
import com.cqt.sdk.client.service.DataQueryService;
import com.cqt.sdk.client.strategy.client.ClientRequestStrategy;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;

/**
 * @author linshiqiang
 * date:  2023-07-03 14:42
 * 查询坐席状态
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GetStatusClientRequestStrategyImpl extends AbstractClientChecker implements ClientRequestStrategy {

    private final ObjectMapper objectMapper;

    private final DataQueryService dataQueryService;

    private final CallContextService callContextService;

    @Override
    public MsgTypeEnum getMsgType() {
        return MsgTypeEnum.get_status;
    }

    @Override
    public ClientResponseBaseVO deal(String requestBody) throws Exception {
        ClientGetStatusDTO getStatusDTO = convert(requestBody, ClientGetStatusDTO.class);
        String companyCode = getStatusDTO.getCompanyCode();
        String agentId = getStatusDTO.getAgentId();
        Optional<AgentStatusDTO> agentStatusOptional = dataQueryService.getAgentStatusDTO(companyCode, agentId);
        if (agentStatusOptional.isPresent()) {
            AgentStatusDTO agentStatusDTO = agentStatusOptional.get();
            if (log.isInfoEnabled()) {
                log.info("[SDK-查询坐席状态] {}", objectMapper.writeValueAsString(agentStatusDTO));
            }
            // 查询inCallNumber
            Set<String> inCallNumbers = callContextService.getInCallNumbers(agentStatusDTO);
            return ClientGetStatusVO.build(getStatusDTO, agentStatusDTO, inCallNumbers);
        }
        // TODO 这个存在redis的临时状态是否需要持久化db?
        return ClientResponseBaseVO.response(getStatusDTO, "1", "获取不到坐席状态!");
    }
}
