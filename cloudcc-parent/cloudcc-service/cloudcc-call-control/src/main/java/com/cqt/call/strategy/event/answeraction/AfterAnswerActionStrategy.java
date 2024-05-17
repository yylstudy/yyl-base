package com.cqt.call.strategy.event.answeraction;

import com.cqt.base.enums.OriginateAfterActionEnum;
import com.cqt.model.agent.vo.CallUuidContext;
import com.cqt.model.freeswitch.dto.event.CallStatusEventDTO;

/**
 * @author linshiqiang
 * date:  2023-10-20 9:30
 */
public interface AfterAnswerActionStrategy {

    /**
     * 接通后要执行的动作
     *
     * @return OriginateAfterActionEnum
     */
    OriginateAfterActionEnum getOriginateAfterAction();

    /**
     * 执行动作
     *
     * @param callStatusEventDTO 呼叫时间
     * @param callUuidContext    当前uuid上下文
     */
    void execute(CallStatusEventDTO callStatusEventDTO, CallUuidContext callUuidContext);
}
