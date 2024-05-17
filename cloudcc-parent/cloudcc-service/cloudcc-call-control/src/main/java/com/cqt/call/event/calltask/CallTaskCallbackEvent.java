package com.cqt.call.event.calltask;

import com.cqt.base.enums.calltask.CallTaskEnum;
import com.cqt.model.agent.vo.CallUuidContext;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * @author linshiqiang
 * date:  2023-08-21 18:27
 */
@Getter
public class CallTaskCallbackEvent extends ApplicationEvent {

    private final CallUuidContext clientContext;

    private final CallUuidContext agentContext;

    private final CallTaskEnum callTaskEnum;

    public CallTaskCallbackEvent(Object source,
                                 CallUuidContext clientContext,
                                 CallUuidContext agentContext,
                                 CallTaskEnum callTaskEnum) {
        super(source);
        this.clientContext = clientContext;
        this.agentContext = agentContext;
        this.callTaskEnum = callTaskEnum;
    }
}
