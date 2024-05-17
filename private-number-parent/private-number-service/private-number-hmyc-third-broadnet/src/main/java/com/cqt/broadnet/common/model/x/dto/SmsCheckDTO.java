package com.cqt.broadnet.common.model.x.dto;

import com.cqt.broadnet.common.utils.FormatUtil;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import javax.xml.soap.Text;

/**
 * @author linshiqiang
 * date:  2023-04-26 14:46
 * 短信上报信息发给第三方应用 入参
 * 查询绑定 校验短信内容合法性，返回真实被叫号码
 */
@Data
public class SmsCheckDTO {


    /**
     * 话音开放平台给App分配的AppKey。
     */
    @JsonProperty("appKey")
    private String appKey;

    /**
     * 否	被叫号码，X模式时不携带
     * 此号码为呼叫接续方的真实号码。
     * 号码格式遵循国际电信联盟定义的E.164标准。
     */
    @JsonProperty("called")
    private String called;

    /**
     * 是	主叫号码
     * 此号码为呼叫发起方的真实号码。
     * 号码格式遵循国际电信联盟定义的E.164标准。
     */
    @JsonProperty("calling")
    private String calling;

    /**
     * 否	短信下发显示的主叫号码，X模式时不携带
     * 号码格式遵循国际电信联盟定义的E.164标准。
     */
    @JsonProperty("displayCalling")
    private String displayCalling;

    /**
     * 否	AXYB-SubID/AX-SubID模式时为必选参数。
     * X模式，如果涉及分机号，会通过该字段传递。
     * 其他模式不涉及
     */
    @JsonProperty("ottSubId")
    private String ottSubId;

    /**
     * 使用小号模式
     */
    @JsonProperty("funcFlag")
    private Long funcFlag;

    /**
     * 是	短信标识，由话音开放平台生成，唯一标识一个呼叫。
     */
    @JsonProperty("msgIdentifier")
    private String msgIdentifier;

    /**
     * 是	短消息总条数。
     */
    @JsonProperty("smNums")
    private Long smNums;

    /**
     * 否	短信内容，请使用UTF-8进行编码
     * 取值样例：E79FADE4BFA1E58685E5AEB9
     */
    @JsonProperty("smsContent")
    private String smsContent;

    /**
     * 是	呼叫事件发生的时间戳。
     * UTC时间。
     * 格式：YYYY-MMDDThh:mm:ss.SSSZ
     * SSS表示毫秒，最后一位固定为Z。
     */
    @JsonProperty("timeStamp")
    private String timeStamp;

    /**
     * 是	短信上报的被叫号码
     * 号码格式遵循国际电信联盟定义的E.164标准。
     */
    @JsonProperty("virtualCalled")
    private String virtualCalled;

    /**
     * 格式化号码为标准号码 去86等
     */
    public void transfer() {
        setCalled(FormatUtil.getNumber(this.called));
        setCalling(FormatUtil.getNumber(this.calling));
        setDisplayCalling(FormatUtil.getNumber(this.getDisplayCalling()));
        setVirtualCalled(FormatUtil.getNumber(this.getVirtualCalled()));
    }

}
