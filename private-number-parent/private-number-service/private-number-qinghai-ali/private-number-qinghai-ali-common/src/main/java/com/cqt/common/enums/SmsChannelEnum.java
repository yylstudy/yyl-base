package com.cqt.common.enums;

/**
 * @author linshiqiang
 * date:  2023-01-28 14:06
 * sms_channel 短信通道方式SMS_INTERCEPT(拦截推送阿里)，SMS_NORMAL_SEND(正常现网下发)，SMS_DROP(拦截丢弃)
 */
public enum SmsChannelEnum {

    /**
     * 拦截推送阿里
     */
    SMS_INTERCEPT,

    /**
     * 正常现网下发
     */
    SMS_NORMAL_SEND,

    /**
     * 拦截丢弃
     */
    SMS_DROP;
}
