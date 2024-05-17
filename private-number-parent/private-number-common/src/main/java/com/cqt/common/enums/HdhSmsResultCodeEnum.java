package com.cqt.common.enums;

import lombok.Getter;

/**
 * ServiceCode
 *
 * @author dingsh
 * @date 2022-08-01
 */
@Getter
public enum HdhSmsResultCodeEnum {

    /**
     *
     */
    zero(0, "短信网关返回发送失败"),
    one(1, "success"),

    two(2, "等待短信结果超时"),

    three(3, "中间号状态不正确"),

    four(4, "企业状态不正确"),

    five(5, "企业未开启短信功能"),


    six(6, "无绑定关系"),

    seven(7, " 订单超过有效期"),

    eight(8, "订单状态不正确"),

    nine(9, "测试账号测试调用次数超出限额"),

    nineteen(19, "测试账号测试调用次数超出限额"),

    twenty(20, "短信内容含涉敏关键词"),

    twenty_one(21, "短信内容含违规关键词"),





    ;

    private final Integer code;
    private final String desc;

    HdhSmsResultCodeEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

}
