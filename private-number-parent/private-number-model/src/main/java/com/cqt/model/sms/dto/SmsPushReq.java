package com.cqt.model.sms.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @Description: 短信推送请求体
 * @author: scott
 * @date: 2022年03月28日 14:59
 */
@Data
@ApiModel(description = "短信推送请求体")
public class SmsPushReq implements Serializable {
    private static final long serialVersionUID = -5796806024891791460L;

    /** 短信发送成功 */
    public static final String SEND_OK = "sendmessage";
    /** 短信发送失败 */
    public static final String SEND_ERR = "senderr";

    /**
     * 开发者ID，无填空
     * */
    @ApiModelProperty(value = "开发者ID，无填空")
    private String devid;

    /**
     * 应用ID，无填空
     * */
    @ApiModelProperty(value = "应用ID，无填空")
    private String appid;

    /**
     * 短信id
     * */
    @ApiModelProperty(value = "短信id")
    private String callid;

    /**
     * A号码
     * */
    @ApiModelProperty(value = "A号码")
    private String telA;

    /**
     * X号码
     * */
    @ApiModelProperty(value = "X号码")
    private String telX;

    /**
     * 短信发送时间戳
     * */
    @ApiModelProperty(value = "短信发送时间戳")
    private String calltime;

    /**
     * 短信内容
     * */
    @ApiModelProperty(value = "短信内容")
    private String smsContent;

    /**
     * 客户应用ID，由腾讯侧提供。需要与X号码绑定区分
     * */
    @ApiModelProperty(value = "客户应用ID，由腾讯侧提供。需要与X号码绑定区分")
    private String sdkappid;

    /**
     * 短信发送状态码: sendmessage：短信发送成功; senderr：短信发送失败
     * */
    @ApiModelProperty(value = "短信发送状态码: sendmessage：短信发送成功; senderr：短信发送失败 ")
    private String callStatus = SEND_OK;


}
