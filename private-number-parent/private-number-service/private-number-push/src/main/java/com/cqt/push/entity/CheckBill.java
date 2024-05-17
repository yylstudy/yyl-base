package com.cqt.push.entity;


import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

/**
 * @author hlx
 * @date 2021-09-15
 */
@Data
public class CheckBill {

    /**
     * 为接入方分配的appkey 必选
     */
    @JSONField(name = "appkey")
    private String appKey;

    /**
     * 腾讯分配的appId 必选
     */
//    private String appId;

    /**
     * 暂时不处理，unix time时间戳 单位秒 必选
     */
    private int ts;

    /**
     * 暂时不处理 签名 必选
     */
    private String sign;

    /**
     * 本次通话唯一标识 长度小于128 必选
     */
    @JSONField(name = "record_id")
    private String recordId;

    /**
     * 拨测请求id
     */
    @JSONField(name = "request_id")
    private String requestId;

    /**
     *  10：axb  20：ax
     */
    @JSONField(name = "service_code")
    private int serviceCode;

    /**
     * 区号  必选
     */
    @JSONField(name = "area_code")
    private String areaCode;

    /**
     * 用于拨测的主叫号码
     */
    @JSONField(name = "tel_calling")
    private String telCalling;

    /**
     * 被叫号码
     */
    @JSONField(name = "tel_called")
    private String telCalled;

    /**
     * 虚拟号 axb：虚拟号x  ax：虚拟号x，分机号  ayb:ax方式中自动绑定的虚拟号y 必选
     */
    @JSONField(name = "tel_x")
    private String telX;

    /**
     * 主叫号码拨通虚拟号的时刻 unix time时间 单位为秒  必选
     */
    @JSONField(name = "begin_time")
    private int beginTime;

    /**
     * 被叫接通时刻 unix time时间 单位为秒  必选
     */
    @JSONField(name = "connect_time")
    private int connectTime;

    /**
     * 被叫振铃时间 unix time时间 单位为秒  必选
     */
    @JSONField(name = "alerting_time")
    private int alertingTime;

    /**
     * 通话结束时刻 unix time时间 单位为秒  必选
     */
    @JSONField(name = "release_time")
    private int releaseTime;

    /**
     * 主被叫之间的通话时长，单位为秒
     */
    @JSONField(name = "call_duration")
    private int callDuration;

    /**
     * 计费时长，单位为秒
     */
    @JSONField(name = "bill_duration")
    private int billDuration;

    /**
     * 通话状态
     */
    @JSONField(name = "call_result")
    private int callResult;

    /**
     * 计费编码  必选
     */
    @JSONField(name = "bill_code")
    private String billCode;

    /**
     * 是否产生费用  0-未产生  1-产生
     */
    @JSONField(name = "has_cost")
    private int hasCost;

    /**
     * 通话费用 单位为元
     */
    @JSONField(name = "call_cost")
    private int callCost;
}
