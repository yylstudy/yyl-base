package com.cqt.model.unicom.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;

@Data
public class MeituanSmsStatePush implements Serializable {
    //必选	为接入方分配的appkey
    private String appkey;
    //UNIXTIME时间戳，单位为毫秒。
    private Long ts;
    private String sign;
    //本次短信唯一标识
    @JsonProperty("sms_id")
    private String smsId;
    //区号
    @JsonProperty("area_code")
    private String areaCode;
    //绑定ID(号码绑定时返回的绑定ID)
    @JsonProperty("bind_id")
    private String bindId;
    //发送者真实号码
    private String sender;
    //接收者真实号码
    private String receiver;
    //发送者分配号码
    @JsonProperty("sender_show")
    private String senderShow;
    //接收者分配号码
    @JsonProperty("receiver_show")
    private String receiverShow;
    //短信发送时刻 UNIXTIME单位为秒
    @JsonProperty("transfer_time")
    private String transferTime;
    //业务编码
    //10: axb
    //11: axbn
    //20: axyb-ax
    //21: axyb-ayb
    //22: ax
    @JsonProperty("service_code")
    private Integer serviceCode;
    //短信内容
    @JsonProperty("sms_content")
    private String smsContent;
    //短信状态
    //0：成功
    //1：无绑定关系
    @JsonProperty("sms_result")
    private Integer smsResult;
    //绑定请求中携带的请求id
    @JsonProperty("request_id")
    private String requestId;
    //用户透传数据
    @JsonProperty("user_data")
    private Object userData;

    /**
     *  (第三方) 短信条数
     **/
    @JsonProperty("sms_number")
    private Integer smsNumber;
}
