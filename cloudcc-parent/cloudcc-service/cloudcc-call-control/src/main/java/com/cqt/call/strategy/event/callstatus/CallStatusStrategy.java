package com.cqt.call.strategy.event.callstatus;

import com.cqt.base.enums.CallStatusEventEnum;
import com.cqt.model.freeswitch.dto.event.CallStatusEventDTO;

/**
 * @author linshiqiang
 * date:  2023-07-03 13:36
 */
public interface CallStatusStrategy {

    void deal(CallStatusEventDTO callStatusEventDTO) throws Exception;

    CallStatusEventEnum getCallStatus();
}
