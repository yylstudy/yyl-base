package com.cqt.model.unicom.entity;

import lombok.Getter;

/**
 * 短信返回码枚举
 *
 * @author zhengsuhao
 * @date 2022/12/15
 */
@Getter
public enum SmsCodeEnum {
    /**
     *
     */
    zero(0, "成功"),

    one(1, "命中黑名单"),


    two(2, "被叫号码不支持106开头号码"),

    three(3, "短信发送数量达到上限"),

    four(4, "短信中心不存在"),

    five(5, "中间号限制短信业务"),

    ninety_nine(99, "其他错误"),

    ;
    private final Integer code;
    private final String desc;

    SmsCodeEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
