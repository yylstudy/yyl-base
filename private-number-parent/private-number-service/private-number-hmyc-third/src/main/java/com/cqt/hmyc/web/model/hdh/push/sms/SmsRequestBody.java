/**
 * Copyright © 2017 公司名. All rights reserved.
 */
package com.cqt.hmyc.web.model.hdh.push.sms;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class SmsRequestBody {
    //主叫号码
    @JsonProperty
    private String aPhoneNumber;
    //入服务号的imsi
    private String oPhoneIMSI;
    //入服务号
    private String inPhoneNumber;
    //被叫号码
    @JsonProperty
    private String bPhoneNumber;
    //出服务号
    private String outPhoneNumber;
    //gt码
    private String gtCode;
    //短信中心
    private String gsmCenter;
    //短信内容
    private String inContent;
    //短信内容
    private String requestTime;
    //发送时间
    private String sendTime;
    //主叫号码（发短信号码）
    private String callerNumber;
    //被叫号码（收短信号码）
    private String calledNumber;
    //失败码
    private String failCode;
    //失败原因
    private String failReason;
    //vccid
    private String vccId;
    //绑定id
    private String bindId;
    //0:下行，1上行
    private String direction ;

    //短信条数
    private  Integer smsNumber;

    private  String supplierId;
    /**
     * 禁用短信 0：不禁用 1禁用
     */
    private Integer type;
}

