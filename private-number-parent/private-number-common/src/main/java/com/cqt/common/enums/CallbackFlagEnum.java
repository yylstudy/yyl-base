package com.cqt.common.enums;

import lombok.Getter;

/**
 * @author linshiqiang
 * @date 2021/9/17 18:53
 * AX tel是否可以回呼X找到最近联系人, 回呼有效时间为callback_expiration:
 * 1: 是
 * 0: 否(默认值)
 */
@Getter
public enum CallbackFlagEnum {

    /**
     *
     */
    CALLBACK(1, "AX允许回呼找到B"),

    NOT_CALLBACK(0, "AX不允许回呼找到B");

    private final Integer code;
    private final String message;

    CallbackFlagEnum(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}
