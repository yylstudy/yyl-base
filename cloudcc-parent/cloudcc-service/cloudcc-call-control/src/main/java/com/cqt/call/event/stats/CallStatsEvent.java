package com.cqt.call.event.stats;

import com.cqt.model.agent.vo.CallUuidContext;
import com.cqt.model.freeswitch.dto.event.CallStatusEventDTO;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * @author linshiqiang
 * date:  2023-10-11 13:35
 * 通话统计
 */
@Getter
public class CallStatsEvent extends ApplicationEvent {

    private final String agentId;

    private final CallStatusEventDTO callStatusEventDTO;

    private final CallUuidContext callUuidContext;

    public CallStatsEvent(Object source, String agentId, CallStatusEventDTO callStatusEventDTO, CallUuidContext callUuidContext) {
        super(source);
        this.agentId = agentId;
        this.callStatusEventDTO = callStatusEventDTO;
        this.callUuidContext = callUuidContext;
    }
}
