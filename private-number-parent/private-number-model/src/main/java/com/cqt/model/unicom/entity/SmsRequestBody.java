package com.cqt.model.unicom.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @author zhengsuhao
 * @date 2022/12/12
 */
@Data
public class SmsRequestBody implements Serializable {

    private static final long serialVersionUID = -6780691630188919064L;

    /**
     * 主叫号码
     */
    @JsonProperty
    private String aPhoneNumber;
    /**
     * 入服务号的imsi
     */
    @JsonProperty
    private String oPhoneIMSI;
    /**
     * 入服务号
     */
    @JsonProperty
    private String inPhoneNumber;
    /**
     * 被叫号码
     */
    @JsonProperty
    private String bPhoneNumber;
    /**
     * 出服务号
     */
    @JsonProperty
    private String outPhoneNumber;
    /**
     * gt码
     */
    @JsonProperty
    private String gtCode;
    /**
     * 短信中心
     */
    @JsonProperty
    private String gsmCenter;
    /**
     * 短信内容
     */

    @JsonProperty
    private String inContent;
    /**
     * 短信内容
     */
    @JsonProperty
    private String requestTime;
    /**
     * 发送时间
     */

    @JsonProperty
    private String sendTime;
    /**
     * 主叫号码（发短信号码）
     */
    @JsonProperty
    private String callerNumber;
    /**
     * 被叫号码（收短信号码）
     */
    @JsonProperty
    private String calledNumber;
    /**
     * 失败码
     */
    @JsonProperty
    private String failCode;
    /**
     * 失败原因
     */
    @JsonProperty
    private String failReason;
    /**
     * vccid
     */
    @JsonProperty
    private String vccId;
    @JsonProperty
    private int messageId;
    @JsonProperty
    private int totalNumber;
    @JsonProperty
    private int currentNumber;
    @JsonProperty
    private String bindId;

    /**
     * 短信条数
     */
    private Integer smsNumber;
    /**
     * 禁用短信 0：不禁用 1禁用
     */
    private Integer type;

    public SmsRequestBody() {
    }

    public SmsRequestBody(String aPhoneNumber, String oPhoneIMSI, String inPhoneNumber, String bPhoneNumber, String outPhoneNumber, String gtCode, String gsmCenter, String inContent, String requestTime, String sendTime, String callerNumber, String calledNumber, String failCode, String failReason, String vccId, int messageId, int totalNumber, int currentNumber, Integer smsNumber) {
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
        this.messageId = messageId;
        this.totalNumber = totalNumber;
        this.currentNumber = currentNumber;
        this.smsNumber = smsNumber;
    }
}
