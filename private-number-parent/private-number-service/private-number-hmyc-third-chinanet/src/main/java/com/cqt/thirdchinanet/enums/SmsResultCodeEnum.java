package com.cqt.thirdchinanet.enums;

import lombok.Getter;

/**
 * ServiceCode
 *
 * @author dingsh
 * @date 2022-08-01
 */
@Getter
public enum SmsResultCodeEnum {

    /**
     *
     */
    zero(0, "成功"),
    one(1, "命中黑名单"),

    two(2, "被叫号码不支持106 开头号码"),

    three(3, "短信发送数量达到上限"),

    four(4, "短信中心不存在"),

    five(5, "中间号限制短信业务"),


    six(99, "其他错误"),






    ;

    private final Integer code;
    private final String desc;

    SmsResultCodeEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

}
