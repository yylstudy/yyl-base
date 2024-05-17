package com.cqt.model.sms.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @author CQT
 * 通用短信发送记录推送接口参数
 * 客户提供接口
 */
@Data
public class CommonSmsBillPushDTO implements Serializable {

    private static final long serialVersionUID = -5424088175484243515L;

    private Long ts;

    private String sign;

    /**
     * 本次短信唯一标识
     */
    @JsonProperty("sms_id")
    private String smsId;

    /**
     * 区号
     */
    @JsonProperty("area_code")
    private String areaCode;

    /**
     * 绑定id
     */
    @JsonProperty("bind_id")
    private String bindId;

    /**
     * 发送者真实号码
     */
    private String sender;

    /**
     * 接收者真实号码
     */
    private String receiver;

    /**
     * 发送者分配号码
     */
    @JsonProperty("sender_show")
    private String senderShow;

    /**
     * 接收者分配号码
     */
    @JsonProperty("receiver_show")
    private String receiverShow;

    /**
     * 短信发送时刻
     * yyyy-MM-dd HH:mm:ss
     */
    @JsonProperty("transfer_time")
    private String transferTime;

    /**
     * 短信内容
     */
    @JsonProperty("sms_content")
    private String smsContent;

    /**
     * 短信状态。
     * 0：成功
     * 1：命中黑名单
     * 2：被叫号码不支持106开头号码
     * 3：短信发送数量达到上限
     * 4:短信中心不存在
     * 5:中间号限制短信业务
     * 99:其他错误
     */
    @JsonProperty("sms_result")
    private String smsResult;

    /**
     * 绑定时由业务侧提供的user_data字段内容
     */
    @JsonProperty("user_data")
    private String userData;

    @JsonProperty("sms_number")
    private Integer smsNumber;

}
