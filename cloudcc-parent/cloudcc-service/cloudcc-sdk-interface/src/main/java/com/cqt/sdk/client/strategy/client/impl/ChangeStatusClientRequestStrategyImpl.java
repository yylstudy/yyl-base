package com.cqt.sdk.client.strategy.client.impl;

import com.cqt.base.enums.MsgTypeEnum;
import com.cqt.model.client.dto.ClientChangeStatusDTO;
import com.cqt.model.client.vo.ClientChangeStatusVO;
import com.cqt.sdk.client.strategy.agentstatus.AgentStatusTransferStrategyFactory;
import com.cqt.sdk.client.strategy.client.ClientRequestStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author linshiqiang
 * date:  2023-07-03 14:42
 * 切换状态（包括强制）操作
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChangeStatusClientRequestStrategyImpl extends AbstractClientChecker implements ClientRequestStrategy {

    private final AgentStatusTransferStrategyFactory agentStatusTransferStrategyFactory;

    @Override
    public MsgTypeEnum getMsgType() {
        return MsgTypeEnum.change_status;
    }

    @Override
    public ClientChangeStatusVO deal(String requestBody) throws Exception {
        ClientChangeStatusDTO clientChangeStatusDTO = convert(requestBody, ClientChangeStatusDTO.class);
        return agentStatusTransferStrategyFactory.transferAgentStatus(clientChangeStatusDTO);
    }
}
