package com.cqt.common.enums;

import lombok.Getter;

/**
 * @author linshiqiang
 * @date 2021/7/8 11:43
 * 号码类型
 */
@Getter
public enum NumberTypeEnum {

    /**
     * A和B用户绑定一个虚拟号码X，A和B用户对对方只呈现X号码
     */
    AXB,

    /**
     * 平台为A绑定一个分机主号码X和一个分机号E, 其他用户拨打X号码再输入分机号E即可联系A用户
     */
    AXE,

    /**
     * AXE模式 Y显号码类型
     */
    AXE_Y,

    /**
     * fs 查询绑定关系, 分机号模式第一次调用接口, 或分机号不正确返回
     */
    XE,

    /**
     * AXEYB模式是AXB和AXE两个模式的组合应用
     * B拨打X号码再输入分机号E可联系A用户，A拨打Y号码可联系B用户。隐私号码X、Y作为呼叫时的号显号码
     */
    AXEYB,

    AXEYB_AXE,

    AXEYB_AYB,

    /**
     * AXEBN模式, A打X, 输入分机号E, 呼叫B; B打X直接呼叫A
     */
    AXEBN,

    /**
     * A打X, 输入分机号E
     */
    AXEBN_AXE,

    /**
     * B打X直接呼叫A
     */
    AXEBN_AXB,

    /**
     * 平台为A绑定一个虚拟号码X, X号码只专属于A号码，所有人均可拨打X联系A
     */
    AX,

    AXBN,

    AXYB,

    /**
     * 20: axyb-ax
     */
    AXYB_AX,

    /**
     * 21: axyb-ayb
     */
    AXYB_AYB,

    AYB,

    /**
     * 绑定关系在企业侧, 号码类型
     */
    OUT,

    /**
     * 短信专用号码 不可绑定
     */
    XSMS;

}
