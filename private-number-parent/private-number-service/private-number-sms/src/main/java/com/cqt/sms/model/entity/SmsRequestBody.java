/**
 * Copyright © 2017 公司名. All rights reserved.
 */
package com.cqt.sms.model.entity;

import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;


public class SmsRequestBody {
    //主叫号码
    @JsonProperty
    private String aPhoneNumber;
    //入服务号的imsi
    @JsonProperty
    private String oPhoneIMSI;
    //入服务号
    @JsonProperty
    private String inPhoneNumber;
    //被叫号码
    @JsonProperty
    private String bPhoneNumber;
    //出服务号
    @JsonProperty
    private String outPhoneNumber;
    //gt码
    @JsonProperty
    private String gtCode;
    //短信中心
    @JsonProperty
    private String gsmCenter;
    //短信内容
    @JsonProperty
    private String inContent;
    //短信内容
    @JsonProperty
    private String requestTime;
    //发送时间
    @JsonProperty
    private String sendTime;
    //主叫号码（发短信号码）
    @JsonProperty
    private String callerNumber;
    //被叫号码（收短信号码）
    @JsonProperty
    private String calledNumber;
    //失败码
    @JsonProperty
    private String failCode;
    //失败原因
    @JsonProperty
    private String failReason;
    //vccid
    @JsonProperty
    private String vccId;
    @JsonProperty
    private int message_id;
    @JsonProperty
    private int total_number;
    @JsonProperty
    private int current_number;
    @JsonProperty
    private String bindId;

    /**
     * 短信条数
     */
    private Integer smsNumber;

    public SmsRequestBody() {
    }

    public SmsRequestBody(String aPhoneNumber, String oPhoneIMSI, String inPhoneNumber, String bPhoneNumber, String outPhoneNumber, String gtCode, String gsmCenter, String inContent, String requestTime, String sendTime, String callerNumber, String calledNumber, String failCode, String failReason, String vccId, int message_id, int total_number, int current_number,Integer smsNumber) {
        this.aPhoneNumber = aPhoneNumber;
        this.oPhoneIMSI = oPhoneIMSI;
        this.inPhoneNumber = inPhoneNumber;
        this.bPhoneNumber = bPhoneNumber;
        this.outPhoneNumber = outPhoneNumber;
        this.gtCode = gtCode;
        this.gsmCenter = gsmCenter;
        this.inContent = inContent;
        this.requestTime = requestTime;
        this.sendTime = sendTime;
        this.callerNumber = callerNumber;
        this.calledNumber = calledNumber;
        this.failCode = failCode;
        this.failReason = failReason;
        this.vccId = vccId;
        this.message_id = message_id;
        this.total_number = total_number;
        this.current_number = current_number;
        this.smsNumber=smsNumber;
    }

    public String getaPhoneNumber() {
        return aPhoneNumber;
    }

    public void setaPhoneNumber(String aPhoneNumber) {
        this.aPhoneNumber = aPhoneNumber;
    }

    public String getoPhoneIMSI() {
        return oPhoneIMSI;
    }

    public void setoPhoneIMSI(String oPhoneIMSI) {
        this.oPhoneIMSI = oPhoneIMSI;
    }

    public String getInPhoneNumber() {
        return inPhoneNumber;
    }

    public void setInPhoneNumber(String inPhoneNumber) {
        this.inPhoneNumber = inPhoneNumber;
    }

    public String getbPhoneNumber() {
        return bPhoneNumber;
    }

    public void setbPhoneNumber(String bPhoneNumber) {
        this.bPhoneNumber = bPhoneNumber;
    }

    public String getOutPhoneNumber() {
        return outPhoneNumber;
    }

    public void setOutPhoneNumber(String outPhoneNumber) {
        this.outPhoneNumber = outPhoneNumber;
    }

    public String getGtCode() {
        return gtCode;
    }

    public void setGtCode(String gtCode) {
        this.gtCode = gtCode;
    }

    public String getGsmCenter() {
        return gsmCenter;
    }

    public void setGsmCenter(String gsmCenter) {
        this.gsmCenter = gsmCenter;
    }

    public String getInContent() {
        return inContent;
    }

    public void setInContent(String inContent) {
        this.inContent = inContent;
    }

    public String getRequestTime() {
        return requestTime;
    }

    public void setRequestTime(String requestTime) {
        this.requestTime = requestTime;
    }

    public String getSendTime() {
        return sendTime;
    }

    public void setSendTime(String sendTime) {
        this.sendTime = sendTime;
    }

    public String getCallerNumber() {
        return callerNumber;
    }

    public void setCallerNumber(String callerNumber) {
        this.callerNumber = callerNumber;
    }

    public String getCalledNumber() {
        return calledNumber;
    }

    public void setCalledNumber(String calledNumber) {
        this.calledNumber = calledNumber;
    }

    public String getFailCode() {
        return failCode;
    }

    public void setFailCode(String failCode) {
        this.failCode = failCode;
    }

    public String getFailReason() {
        return failReason;
    }

    public void setFailReason(String failReason) {
        this.failReason = failReason;
    }

    public String getVccId() {
        return vccId;
    }

    public void setVccId(String vccId) {
        this.vccId = vccId;
    }

    public int getMessage_id() {
        return message_id;
    }

    public void setMessage_id(int message_id) {
        this.message_id = message_id;
    }

    public int getTotal_number() {
        return total_number;
    }

    public void setTotal_number(int total_number) {
        this.total_number = total_number;
    }

    public int getCurrent_number() {
        return current_number;
    }

    public void setCurrent_number(int current_number) {
        this.current_number = current_number;
    }

    public String getBindId() {
        return bindId;
    }

    public void setBindId(String bindId) {
        this.bindId = bindId;
    }

    public Integer getSmsNumber() {
        return smsNumber;
    }

    public void setSmsNumber(Integer smsNumber) {
        this.smsNumber = smsNumber;
    }
}

