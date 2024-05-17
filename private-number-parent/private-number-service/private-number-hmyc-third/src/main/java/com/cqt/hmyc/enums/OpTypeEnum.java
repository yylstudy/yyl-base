package com.cqt.hmyc.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author Xienx
 * @date 2023-06-07 17:38:17:38
 */
@Getter
@AllArgsConstructor
public enum OpTypeEnum {

    /**
     * 语音 - 0 - 挂断
     */
    CALL_HANG_UP("0"),

    /**
     * 语音 - 1 - 接通
     */
    CALL_CONTINUE("1"),

    /**
     * 语音 - 2 - 收号
     */
    CALL_IVR("2"),

    /**
     * 短信 - 0 - 拦截
     */
    SMS_DROP("0"),

    /**
     * 短信 - 1 - 转发
     */
    SMS_NORMAL_SEND("1"),

    /**
     * 短信 - 2 - 托收
     */
    SMS_INTERCEPT("2");

    private final String code;
}
