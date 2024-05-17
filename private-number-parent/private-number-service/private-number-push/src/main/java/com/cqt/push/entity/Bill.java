package com.cqt.push.entity;


import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * @author hlx
 * @date 2021-09-15
 */
@Data
@ToString
@NoArgsConstructor
public class Bill {

    /**
     * 为接入方分配的appkey 必选
     */
    @JSONField(name = "appkey")
    private String appKey;

    /**
     * 腾讯分配的appId 必选
     */
    private String appId;

    /**
     * 暂时不处理，unix time时间戳 单位秒 必选
     */
    private long ts;

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
     * 绑定id 必选
     */
    @JSONField(name = "bind_id")
    private String bindId;

    /**
     * AYB绑定的id  可选
     */
    @JSONField(name = "relate_bind_id")
    private String relateBindId;

    /**
     *  10：axb  20：ax  21：ayb 必选
     */
    @JSONField(name = "service_code")
    private int serviceCode;

    /**
     * 区号  必选
     */
    @JSONField(name = "area_code")
    private String areaCode;

    /**
     * 真实号码  必选
     */
    @JSONField(name = "tel_a")
    private String telA;

    /**
     * 真实号码  必选
     */
    @JSONField(name = "tel_b")
    private String telB;

    /**
     * 虚拟号 axb：虚拟号x  ax：虚拟号x，分机号  ayb:ax方式中自动绑定的虚拟号y 必选
     */
    @JSONField(name = "tel_x")
    private String telX;

    /**
     * 虚拟号ayb:ax方式中自动绑定的虚拟号y 必选
     * 具体传值说明：
     * service_code=10,tel_y=空字符串
     * service_code=20,tel_y=ax方式中的自动绑定虚拟号y
     * service_code=21,tel_y=ax方式中的自动绑定虚拟号y
     */
    @JSONField(name = "tel_y")
    private String telY;

    /**
     * 针对tel_a号码  10：通话主叫  11：通话被叫 必选
     */
    @JSONField(name = "call_type")
    private int callType;

    /**
     * 绑定时间 unix time时间 单位为秒  必选
     */
    @JSONField(name = "bind_time")
    private int bindTime;

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
     * 通话录音url 长度限制1024字符
     */
    @JSONField(name = "record_file_url")
    private String recordFileUrl;

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

    /**
     * 绑定时由业务侧提供的user_data字段内容 可选
     */
    @JSONField(name = "user_data")
    private String userData;
}
