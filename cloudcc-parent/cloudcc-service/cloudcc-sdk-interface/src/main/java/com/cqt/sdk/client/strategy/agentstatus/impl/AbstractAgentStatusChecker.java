package com.cqt.sdk.client.strategy.agentstatus.impl;

import com.cqt.base.enums.SdkErrCode;
import com.cqt.base.enums.agent.AgentStatusEnum;
import com.cqt.base.exception.BizException;
import com.cqt.model.agent.dto.AgentStatusDTO;

/**
 * @author linshiqiang
 * date:  2023-11-25 14:53
 */
public abstract class AbstractAgentStatusChecker {

    public void offlineChecker(AgentStatusDTO agentStatusDTO) {
        if (AgentStatusEnum.OFFLINE.name().equals(agentStatusDTO.getTargetStatus())) {
            throw new BizException(SdkErrCode.AGENT_NOT_CHECK_IN);
        }
    }

    public void callingChecker(AgentStatusDTO agentStatusDTO) {
        if (AgentStatusEnum.CALLING.name().equals(agentStatusDTO.getTargetStatus())
                || AgentStatusEnum.RINGING.name().equals(agentStatusDTO.getTargetStatus())) {
            return;
        }
        throw new BizException(SdkErrCode.AGENT_NOT_IN_CALLING_STATUS);
    }
}
