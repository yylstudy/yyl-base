package com.cqt.common.enums;

import lombok.Getter;

@Getter
public enum AliReleaseCauseEnum {
    /**/
    one(1, "未分配的号码（空号）"),

    three(3, "无至目的地的路由"),

    four(4, "停机"),

    six(6, "不可接受的信道"),

    sixteen(16, "正常清除"),

    seventeen(17, "用户忙"),

    eighteen(18, "无用户响应"),

    nineteen(19, "已有用户提醒，但无应答"),

    twenty_one(21, "呼叫拒绝"),

    twenty_two(22, "号码改变"),

    twenty_six(26, "清除未选择的用户"),

    twenty_seven(27, "终点故障"),

    twenty_eight(28, "无效号码格式（不完全的号码）"),

    twenty_nine(29, "设施被拒绝"),

    thirty(30, "对状态询问的响应"),

    thirty_one(31, "正常--未规定"),

    thirty_four(34, "无电路/信道可用"),

    thirty_eight(38, "网络故障"),

    forty_one(41, "临时故障"),

    forty_two(42, "交换设备拥塞"),

    forty_three(43, "接入信息被丢弃"),

    forty_four(44, "请求的电路/信道不可用"),

    forty_seven(47, "资源不可用--未规定"),

    forty_nine(49, "服务质量不可用"),

    fifty(50,"未预订所请求的设施"),

    fifty_five(55,"IncomingcallsbarredwithintheCUG"),

    fifty_seven(57,"承载能力未认可(未开通通话功能）"),

    fifty_eight(58,"承载能力目前不可用"),

    sixty_three(63,"无适用的业务或任选项目-未规定"),

    sixty_five(65,"承载业务不能实现"),

    sixty_eight(68,"ACMequaltoorgreaterthanACMmax"),

    sixty_nine(69,"所请求的设施不能实现"),

    seventy(70,"仅能获得受限数字信息承载能力"),

    seventy_nine(79,"业务不能实现-未规定"),

    eighty_one(81,"无效处理识别码"),

    eighty_seven(87,"87UsernotmemberofCUG"),

    eighty_eight(88,"非兼容目的地址"),

    ninety_one(91,"非兼容目的地址"),

    ninety_five(95,"无效消息-未规定"),

    ninety_six(96,"必选消息单元差错"),

    ninety_seven(97,"消息类型不存在或不能实现"),

    ninety_eight(98,"消息与控制状态不兼容-消息类型不存在或不能实现"),

    ninety_nine(99, "信息单元不存在或不能实现"),

    one_hundred(100,"无效信息单元内容"),

    one_zero_one(101,"消息与呼叫状态不兼容"),

    one_zero_two(102,"定时器超时恢复"),

    one_one_one(111,"协议差错-未规定"),

    one_two_seven(127,"互通-未规定"),

    nine_nine_nine_nine(9999,"短信话单时，传此值")

    ;
    private final Integer code;
    private final String desc;

    AliReleaseCauseEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }


}
