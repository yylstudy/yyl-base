package com.cqt.common.enums;

import lombok.Getter;

/**
 * @author linshiqiang
 * date:  2023-02-06 15:19
 * 行为类型,CALL:呼叫行为,SMS:短信行为
 */
@Getter
public enum BehaviorType {

    /**
     * 呼叫行为
     */
    CALL,

    /**
     * 短信行为
     */
    SMS;
}
