package com.cqt.model.push.entity;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * 通话状态信息
 *
 * @author hlx
 * @date 2022-02-24
 */
@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class PrivateStatusInfo {

    /**
     * 为接入方分配的appkey 必选
     */
    @JSONField(name = "appkey")
    private String appKey;

    /**
     * 暂时不处理，unix time时间戳 单位毫秒 必选
     */
    private Long ts;

    /**
     * 暂时不处理 签名 必选
     */
    private String sign;

    /**
     * 企业id
     */
    @JSONField(name = "vcc_id", serialize = false)
    private String vccId;

    /**
     * 通话唯一id
     */
    @JSONField(name = "record_id")
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
    private String bindId;

    /**
     * x 号码  中间号
     */
    @JSONField(name = "tel_x")
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
    private String currentTime;

    /**
     * 分级号
     */
    private String ext;

    /**
     * 透传参数  对应解析关系如下
     * axeyb_axb PrivateBindInfoAxb
     * axeyb_axe PrivateBindInfoAxeyb
     * axb  PrivateBindInfoAxb
     */
    @JSONField(name = "transfer_data", serialize = false)
    @ToString.Exclude
    private String transferData;

    /**
     * 绑定类型
     */
    @JSONField(name = "num_type", serialize = false)
    private String numType;

    /**
     * 主叫类型
     */
    @JSONField(name = "call_type")
    private Integer callType;


    /**
     * 挂机事件
     */
    @JSONField(name = "call_result")
    private Integer callResult;

    @JSONField(name = "user_data")
    private String userData;

    /*
     * 被叫号显
     * */
    @JSONField(name = "called_display_no")
    private String calledDisplayNo;

    /**
     * 呼叫开始时间
     */
    @JSONField(name = "call_time")
    private String callTime;

}
