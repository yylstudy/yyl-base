package com.cqt.call.strategy.event.callin;

import com.cqt.base.enums.CallInStrategyEnum;
import com.cqt.model.freeswitch.dto.event.CallInEventDTO;
import com.cqt.model.number.entity.NumberInfo;

/**
 * @author linshiqiang
 * date:  2023-07-21 9:20
 * 呼入事件策略接口
 */
public interface CallInEventStrategy {

    Boolean execute(CallInEventDTO callInEventDTO, NumberInfo numberInfo) throws Exception;

    CallInStrategyEnum getCallInStrategy();
}
