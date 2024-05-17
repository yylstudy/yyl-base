package com.cqt.sdk.client.event.arrange;

import com.cqt.model.agent.bo.AgentStatusTransferBO;
import com.cqt.model.client.dto.ClientCheckinDTO;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * @author linshiqiang
 * date:  2023-07-26 15:24
 * 通话结束, 开启事后处理事件
 */
@Getter
public class CallStopArrangeEvent extends ApplicationEvent {

    private final AgentStatusTransferBO agentStatusTransferBO;

    private final Integer arrangeSecond;

    private final ClientCheckinDTO clientCheckinDTO;

    public CallStopArrangeEvent(Object source, AgentStatusTransferBO agentStatusTransferBO, Integer arrangeSecond) {
        super(source);
        this.agentStatusTransferBO = agentStatusTransferBO;
        this.arrangeSecond = arrangeSecond;
        this.clientCheckinDTO = null;
    }

    public CallStopArrangeEvent(Object source, Integer arrangeSecond, ClientCheckinDTO clientCheckinDTO) {
        super(source);
        this.arrangeSecond = arrangeSecond;
        this.clientCheckinDTO = clientCheckinDTO;
        this.agentStatusTransferBO = null;
    }
}
