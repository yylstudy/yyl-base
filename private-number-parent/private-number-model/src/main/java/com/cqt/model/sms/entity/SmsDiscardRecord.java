package com.cqt.model.sms.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @Description: 短信推送丢弃记录
 * @author: scott
 * @date: 2022年03月25日 17:53
 */
@Data
@ApiModel(value = "短信推送丢弃记录")
public class SmsDiscardRecord implements Serializable {

    private static final long serialVersionUID = -9204009067485675455L;

    /**
     * 分布式主键ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    @ApiModelProperty(value = "分布式主键ID")
    private String id;

    /**
     * 企业vccId
     * */
    @ApiModelProperty(value = "企业vccId")
    private String vccId;

    /**
     * 短信业务流水号
     * */
    @ApiModelProperty(value = "短信业务流水号")
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
     * 接收时间
     * */
    @ApiModelProperty(value = "接收时间")
    private Date receiveTime;

    /**
     * 短信内容
     * */
    @ApiModelProperty(value = "短信内容")
    private String smsContent;

    /**
     * 丢弃原因
     * */
    @ApiModelProperty(value = "丢弃原因")
    private String discardInfo;

    /**
     * 请求json
     * */
    @ApiModelProperty(value = "请求json")
    private String reqJson;

    /**
     * 创建时间
     * */
    @ApiModelProperty(value = "创建时间")
    private Date createTime;
}
