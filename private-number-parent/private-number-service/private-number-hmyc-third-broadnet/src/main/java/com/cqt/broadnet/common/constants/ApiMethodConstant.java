package com.cqt.broadnet.common.constants;

/**
 * @author linshiqiang
 * date:  2023-02-15 13:55
 * API接口名称
 */
public interface ApiMethodConstant {

    /**
     * 1：呼转控制接口
     */
    String CALL_CONTROL = "axb.vendor.call.control";

    /**
     * 2：供应商推送通话结束事件
     */
    String CALL_RELEASE = "axb.vendor.call.release";

    /**
     * 3：AXB短信托收推送接口
     */
    String SMS_INTERCEPT = "axb.vendor.sms.intercept";

    /**
     * 4：供应商心跳上报接口
     */
    String HEART_BEAT = "axb.vendor.heart.beat";

    /**
     * 5：异常号码状态同步接口
     */
    String EXCEPTION_NO_SYNC = "axb.vendor.exception.no.sync";

    /**
     * 1.2.2.2.7 供应商推送呼出事件接口API
     */
    String PUSH_CALL_OUT = "axb.vendor.call.out";

    /**
     * 1.2.2.2.8 供应商推送振铃事件接口API
     */
    String PUSH_CALL_RINGING = "axb.vendor.call.ringing";

    /**
     * 1.2.2.2.9 供应商推送摘机事件接口API
     */
    String PUSH_CALL_ANSWER = "axb.vendor.call.answer";
}
