package com.cqt.model.hmbc.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 话单表实体
 *
 * @author jeecg-boot
 * @date 2022-07-20
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@ApiModel(value = "AcrRecordDTO", description = "话单表实体")
public class AcrRecordDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 话单UUID
     */
    @ApiModelProperty(value = "话单UUID")
    private String forwardNumber;

    /**
     * 通话开始时间 yyyyMMddHHmmss
     */
    @ApiModelProperty(value = "通话开始时间 yyyyMMddHHmmss")
    private String startDateAndTime;

    /**
     * 通话结束时间 yyyyMMddHHmmss
     */
    @ApiModelProperty(value = "通话结束时间 yyyyMMddHHmmss")
    private String stopDateAndTime;

    /**
     * 主叫通话时长，单位秒
     */
    @ApiModelProperty(value = "主叫通话时长，单位秒")
    private Integer duration;

    /**
     * 0 应答后主叫挂机
     * 1 应答后被叫挂机
     * 10 应答前主叫放弃 （通话时长都是0）
     * 99 未接通，分辨不出来原因
     * >=300被叫未接通错误码 （通话时长都是0）
     */
    @ApiModelProperty(value = "0 应答后主叫挂机\n" +
            "1 应答后被叫挂机\n" +
            "10 应答前主叫放弃 （通话时长都是0）\n" +
            "99 未接通，分辨不出来原因\n" +
            ">=300被叫未接通错误码 （通话时长都是0）\n")
    private Integer releaseCause;

    /**
     * 呼叫类型，指发端或终端
     * 1：主叫话单
     * 2：被叫话单
     * 3：前转话单
     * 4：号码解析转接话单
     * 100：质检
     * 注：servicekey=900003 的referTo转移话单的时候，  ACRTYPE 这个字段的值填的是  3：前转话单 ；ACRTYPE=3 前传话单是对于CTD 来说跟主叫是一样，会产生话费。
     */
    @ApiModelProperty(value = "呼叫类型，指发端或终端\n" +
            "1：主叫话单\n" +
            "2：被叫话单\n" +
            "3：前转话单\n" +
            "4：号码解析转接话单\n" +
            "100：质检\n" +
            "注：servicekey=900003 的referTo转移话单的时候，  ACRTYPE 这个字段的值填的是  3：前转话单 ；ACRTYPE=3 前传话单是对于CTD 来说跟主叫是一样，会产生话费。\n")
    private Integer acrType;

    /**
     * 被叫号码，即用户拨叫的号码
     * 对于主叫话单，填写主叫实际拨打的号码
     * 对于被叫话单，填写被叫计费的16位分机号
     * 对于前转话单，填写前转流程处理前的被叫号码
     */
    @ApiModelProperty(value = "被叫号码，即用户拨叫的号码\n" +
            "对于主叫话单，填写主叫实际拨打的号码\n" +
            "对于被叫话单，填写被叫计费的16位分机号\n" +
            "对于前转话单，填写前转流程处理前的被叫号码\n")
    private String calledPartyNumber;

    /**
     * 主叫号码
     * 对于主叫话单，填写主叫计费的16位分机号
     * 对于被叫话单，填写主叫号码，即INVITE的PAI消息头，如果PAI消息头为空，则填写INVITE的From消息头
     * 对于前转话单，填写前转计费的16位分机号
     */
    @ApiModelProperty(value = "主叫号码 \n" +
            "对于主叫话单，填写主叫计费的16位分机号\n" +
            "对于被叫话单，填写主叫号码，即INVITE的PAI消息头，如果PAI消息头为空，则填写INVITE的From消息头\n" +
            "对于前转话单，填写前转计费的16位分机号\n")
    private String callingPartyNumber;

    /**
     * 内网录音下载地址
     */
    @ApiModelProperty(value = "内网录音下载地址")
    private String transparentparamet;
}
