package com.cqt.common.enums;

import lombok.Getter;

/**
 * ServiceCode
 *
 * @author dingsh
 * @date 2022-08-01
 */
@Getter
public enum CallResultCodeEnum {

    /**
     *
     */
    one(1, "正常接通"),

    two(2, "呼叫遇忙"),

    three(3, "用户不在服务区"),

    four(4, "用户无应答"),

    five(5, "用户关机"),

    six(6, "空号"),

    seven(7, "停机"),

    eight(8, "号码过期"),

    nine(9, "主叫应答，被叫应答前挂机(振铃后挂机) 有振铃时间"),

    ten(10, "正在通话中"),

    eleven(11, "拒接"),

    twelve(12, "请不要挂机"),

    twenty(20, "主动取消呼叫"),

    ninety_nine(99, "其他"),



    ;

    private final Integer code;
    private final String desc;

    CallResultCodeEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

}
