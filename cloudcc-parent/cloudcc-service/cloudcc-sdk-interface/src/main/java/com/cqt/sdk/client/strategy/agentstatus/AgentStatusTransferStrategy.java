package com.cqt.sdk.client.strategy.agentstatus;

import com.cqt.base.enums.agent.AgentStatusTransferActionEnum;
import com.cqt.model.client.dto.ClientChangeStatusDTO;
import com.cqt.model.client.vo.ClientChangeStatusVO;

/**
 * @author linshiqiang
 * date:  2023-07-03 13:36
 * 坐席状态迁移策略接口
 */
public interface AgentStatusTransferStrategy {

    ClientChangeStatusVO deal(ClientChangeStatusDTO clientChangeStatusDTO) throws Exception;

    AgentStatusTransferActionEnum getTransferActionEnum();
}
