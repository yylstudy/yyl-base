package com.cqt.thirdchinanet.entity;

import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * 通话状态信息
 *
 * @author hwz
 * @date 2022-02-24
 */
@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class ChinanetStatusInfo {

    /**
     * 为接入方分配的appkey 必选
     */
    @JSONField(name = "appkey")
    private String appKey;

    /**
     * 暂时不处理，unix time时间戳 单位毫秒 必选
     */
    private long ts;

    /**
     * 暂时不处理 签名 必选
     */
    private String sign;


    /**
     * 通话唯一id
     */
    @JSONField(name = "record_id")
    @JsonProperty("record_id")
    private String recordId;

    /**
     * 当前事件
     * callin 呼入平台
     * callout 找到绑定关系呼出
     * ringing 振铃
     * answer 接通
     * hangup 挂断
     */
    private String event;

    /**
     * 绑定id
     */
    @JSONField(name = "bind_id")
    @JsonProperty("bind_id")
    private String bindId;

    /**
     * x 号码  中间号
     */
    @JSONField(name = "tel_x")
    @JsonProperty("tel_x")
    private String telX;


    /**
     * 主叫号码
     */
    private String caller;

    /**
     * 被叫号码
     */
    private String called;

    /**
     * 当前状态的发生时间
     */
    @JSONField(name = "current_time")
    @JsonProperty("current_time")
    private String currentTime;

    /**
     * 分级号
     */
    private String ext;


    /**
     * 挂机事件
     */
    @JSONField(name = "call_result")
    @JsonProperty("call_result")
    private Integer callResult;
}


