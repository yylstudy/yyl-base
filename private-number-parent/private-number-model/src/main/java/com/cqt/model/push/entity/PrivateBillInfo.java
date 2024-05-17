package com.cqt.model.push.entity;

import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * 通用话单实体类
 *
 * @author hlx
 * @date 2022-02-24
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PrivateBillInfo {

    /**
     * 为接入方分配的appkey 必选
     */
    @JSONField(name = "appkey")
    private String appKey;

    /**
     * 签名 必选
     */
    private String sign;

    /**
     * 暂时不处理，unix time时间戳 单位秒 必选
     */
    private long ts;

    /**
     * 本次通话唯一标识 长度小于128 必选
     */
    @JSONField(name = "record_id")
    @JsonProperty("record_id")
    private String recordId;

    /**
     * 绑定id
     */
    @JSONField(name = "bind_id")
    @JsonProperty("bind_id")
    private String bindId;

    /**
     * ayb 的源 id
     */
    @JSONField(name = "source_bind_id")
    @JsonProperty("source_bind_id")
    private String sourceBindId;

    /**
     * ayb 的源requestId
     */
    @JSONField(name = "source_request_id")
    @JsonProperty("source_request_id")
    private String sourceRequestId;

    /**
     * 绑定类型
     * 10:axb
     * 22:axe
     * 20:axeyb_axe
     * 21:axeyb_ayb
     */
    @JSONField(name = "service_code",serialize = false)
    @JsonProperty("service_code")
    private Integer serviceCode;

    /**
     * 区号
     */
    @JSONField(name = "area_code")
    @JsonProperty("area_code")
    private String areaCode;

    /**
     * 真实号码  必选
     */
    @JSONField(name = "tel_a")
    @JsonProperty("tel_a")
    private String telA;

    /**
     * 真实号码  必选
     */
    @JSONField(name = "tel_b")
    @JsonProperty("tel_b")
    private String telB;

    /**
     * 虚拟号 必选
     */
    @JSONField(name = "tel_x")
    @JsonProperty("tel_x")
    private String telX;

    /**
     * 虚拟号ayb:ax方式中自动绑定的虚拟号y 必选
     */
    @JSONField(name = "tel_y")
    @JsonProperty("tel_y")
    private String telY;

    /**
     * 针对tel_a号码  10：通话主叫  11：通话被叫 必选
     */
    @JSONField(name = "call_type",serialize = false)
    @JsonProperty("call_type")
    private int callType;

    /**
     * 绑定时间 yyyy-MM-dd HH:mm:ss  必选
     */
    @JSONField(name = "bind_time")
    @JsonProperty("bind_time")
    private String bindTime;

    /**
     * 主叫号码拨通虚拟号的时刻 yyyy-MM-dd HH:mm:ss 必选
     */
    @JSONField(name = "begin_time")
    @JsonProperty("begin_time")
    private String beginTime;

    /**
     * 被叫开始呼叫时刻 yyyy-MM-dd HH:mm:ss  必选
     */
    @JSONField(name = "callout_time")
    @JsonProperty("callout_time")
    private String calloutTime;

    /**
     * 被叫接通时刻 yyyy-MM-dd HH:mm:ss  必选
     */
    @JSONField(name = "connect_time")
    @JsonProperty("connect_time")
    private String connectTime;

    /**
     * 被叫振铃时间 yyyy-MM-dd HH:mm:ss 必选
     */
    @JSONField(name = "alerting_time")
    @JsonProperty("alerting_time")
    private String alertingTime;

    /**
     * 通话结束时刻 yyyy-MM-dd HH:mm:ss  必选
     */
    @JSONField(name = "release_time")
    @JsonProperty("release_time")
    private String releaseTime;

    /**
     * 主被叫之间的通话时长，单位为秒
     */
    @JSONField(name = "call_duration")
    @JsonProperty("call_duration")
    private int callDuration;

    /**
     * 通话状态
     */
    @JSONField(name = "call_result")
    @JsonProperty("call_result")
    private int callResult;

    /**
     * 通话录音url 长度限制1024字符
     */
    @JSONField(name = "record_file_url")
    @JsonProperty("record_file_url")
    private String recordFileUrl;

    /**
     * 绑定时由业务侧提供的user_data字段内容 可选
     */
    @JSONField(name = "user_data")
    @JsonProperty("user_data")
    private String userData;

    /**
     * 绑定关系请求id
     */
    @JSONField(name = "request_id")
    @JsonProperty("request_id")
    private String requestId;

    /**
     * 录音开始时间
     */
    @JSONField(name = "record_start_time")
    @JsonProperty("record_start_time")
    private String recordStartTime;

    /**
     * 是否录音
     */
    @JSONField(name = "record_flag")
    @JsonProperty("record_flag")
    private Integer recordFlag;

    /**
     * 分机号
     */
    private String ext;

}
