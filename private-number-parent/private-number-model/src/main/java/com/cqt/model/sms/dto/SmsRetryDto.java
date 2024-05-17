package com.cqt.model.sms.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @Description: 短信重推对象
 * @author: scott
 * @date: 2022年03月28日 14:58
 */
@Data
@ApiModel(description = "短信重推对象")
public class SmsRetryDto implements Serializable {
    private static final long serialVersionUID = 4421033642078772609L;

    /**
     * 企业短信推送URL
     */
    @ApiModelProperty(value = "企业短信推送URL")
    private String pushUrl;


    /**
     * vccId 企业vccId
     * */
    @ApiModelProperty(value = "企业vccId")
    private String vccId;

    /**
     * 短信流水号
     * */
    @ApiModelProperty(value = "短信流水号")
    private String msgId;

    /**
     * 主叫号码
     * */
    @ApiModelProperty(value = "主叫号码")
    private String telA;

    /**
     * 被叫号码
     * */
    @ApiModelProperty(value = "被叫号码")
    private String telX;

    /**
     * 短信内容
     * */
    @ApiModelProperty(value = "短信内容")
    private String smsContent;

    /**
     * 短信发送时间 yyyyMMddHHmmss 格式
     * */
    @ApiModelProperty(value = "短信发送时间")
    private String requestTime;

    /**
     * 当前失败次数
     * */
    @ApiModelProperty(value = "当前失败次数")
    private Integer currentRetryCount = 1;

    /**
     * 上次发送失败原因
     * */
    @ApiModelProperty(value = "上次发送失败原因")
    private String lastFailedReason;

    /**
     * 上次发送失败时间
     * */
    @ApiModelProperty(value = "上次发送失败时间")
    private Date lastSendTime;
}
