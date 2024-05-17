package com.cqt.common.enums;

import lombok.Getter;

/**
 * @author linshiqiang
 * @date 2021/9/17 18:53
 * AXB 号码池初始化标志
 */
@Getter
public enum InitFlagEnum {

    /**
     *
     */
    FIRST_INIT("1", "第一次初始化, 使用主池"),

    SECOND_INIT("2", "第二次初始化, 使用备池"),

    THREE_INIT("3", "使用全部号码");

    private final String code;
    private final String message;

    InitFlagEnum(String code, String message) {
        this.code = code;
        this.message = message;
    }
}
