package com.cqt.sdk.client.event.arrange;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * @author linshiqiang
 * date:  2023-08-08 10:38
 */
@Getter
public class CallStopArrangeCancelEvent extends ApplicationEvent {

    private final String companyCode;

    private final String agentId;

    public CallStopArrangeCancelEvent(Object source, String companyCode, String agentId) {
        super(source);
        this.companyCode = companyCode;
        this.agentId = agentId;
    }
}
