package com.cqt.common.enums;

import lombok.Getter;

/**
 * ServiceCode
 *
 * @author dingsh
 * @date 2022-08-01
 */
@Getter
public enum HdhResultCodeEnum {

    /**
     *
     */
    zero(0,"无绑定关系"),

    one(1, "主叫挂机"),

    two(2, "被叫挂机"),

    three(3, "主叫放弃"),

    four(4, "被叫无应答"),

    five(5, "被叫忙"),

    six(6, "被叫不可及"),

    seven(7, "路由失败"),

    eight(8, "中间号状态异常"),

    nine(9, "订单超过有效期（自 1.9.18 版本开始弃用"),

    ten(10, "平台系统异常"),

    eleven(11, "关机"),

    twelve(12, "停机"),

    thirteen(13, "拒接"),

    fourteen(14, "空号"),

    twenty_one(21, "测试账号测试调用次数超出限额"),

    fifty(50, "验证码未通过"),



    ;

    private final Integer code;
    private final String desc;

    HdhResultCodeEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

}
