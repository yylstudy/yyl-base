package com.cqt.common.enums;

import lombok.Getter;

/**
 * @author linshiqiang
 * @date 2021/9/17 18:53
 * 通话类型
 */
@Getter
public enum CallTypeEnum {

    /**
     *
     */
    CALLER("10", "通话主叫"),

    CALLED("11", "通话被叫");

    private final String code;
    private final String message;

    CallTypeEnum(String code, String message) {
        this.code = code;
        this.message = message;
    }
}
