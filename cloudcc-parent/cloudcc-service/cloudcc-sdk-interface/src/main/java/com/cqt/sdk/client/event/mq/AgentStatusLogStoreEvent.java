package com.cqt.sdk.client.event.mq;

import com.cqt.model.agent.dto.AgentStatusTransferDTO;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * @author linshiqiang
 * date:  2023-07-04 13:34
 * 坐席状态迁移日志事件 发送mq进行入库 + 修改坐席redis状态
 */
public class AgentStatusLogStoreEvent extends ApplicationEvent {

    @Getter
    private AgentStatusTransferDTO agentStatusTransferDTO;

    public AgentStatusLogStoreEvent(Object source, AgentStatusTransferDTO agentStatusTransferDTO) {
        super(source);
        this.agentStatusTransferDTO = agentStatusTransferDTO;
    }
}
