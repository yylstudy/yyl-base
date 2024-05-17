package com.cqt.base.enums;

import lombok.Getter;

/**
 * @author linshiqiang
 * date:  2023-07-06 16:07
 * 呼叫类型
 * 坐席还是客户
 */
@Getter
public enum CallTypeEnum {

    /**
     * 坐席
     */
    AGENT(0),

    /**
     * 客户
     */
    CLIENT(1);

    private final Integer code;

    CallTypeEnum(Integer code) {
        this.code = code;
    }
}
