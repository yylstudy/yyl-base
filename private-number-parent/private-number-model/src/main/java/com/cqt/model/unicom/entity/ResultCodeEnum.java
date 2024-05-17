package com.cqt.model.unicom.entity;

import lombok.Getter;

/**
 * 通话返回码枚举
 *
 * @author zhengsuhao
 * @date 2022/12/15
 */
@Getter
public enum ResultCodeEnum {

    /**
     *
     */
    one(1, "Normal connection"),

    two(2, "Call is busy"),

    three(3, "The user is not in the service area"),

    four(4, "No response from user "),

    five(5, "User shutdown"),

    six(6, "Space number"),

    seven(7, "halt"),

    eight(8, "Number expired"),

    nine(9, "The caller answers and hangs up before the callee answers"),

    ten(10, "Call in progress"),

    eleven(11, "Reject"),

    twenty(20, "Actively cancel the call"),

    ninety_nine(99, "other"),

    ;
    private final Integer code;
    private final String desc;

    ResultCodeEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

}
