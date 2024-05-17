package com.cqt.hmyc.web.fotile.model.dto;

import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.io.Serializable;

/**
 * @author linshiqiang
 * @date 2021/4/8 10:49
 * 绑定关系操作 入参
 */
public class BindOperationDTO implements Serializable {

    private static final long serialVersionUID = 1603693391636361757L;

    /**
     * 流水号
     */
    @ApiModelProperty("流水号")
    @NotBlank(message = "streamNumber 不能为空!")
    private String streamNumber;

    /**
     * 消息id
     */
    @ApiModelProperty("消息id")
    private String messageId;

    /**
     * 业务id
     */
    @ApiModelProperty("业务id")
    @NotBlank(message = "businessId 不能为空!")
    @Pattern(regexp = "([0-9]{4})", message = "businessId 只能为4位数字!")
    private String businessId;

    /**
     * 操作类型
     * 0：新增
     * 1：修改
     * 2：删除
     */
    @ApiModelProperty("操作类型  0：新增, 1：修改, 2：删除")
    @NotNull(message = "operation 不能为空!")
    private Integer operation;

    /**
     * 绑定ID
     */
    @ApiModelProperty("绑定ID")
    @NotBlank(message = "bindId 不能为空!")
    private String bindId;

    /**
     * 绑定时间
     */
    @ApiModelProperty("绑定时间")
    @NotBlank(message = "bindTime 不能为空!")
    @Pattern(regexp = "^\\d{14}$", message = "bindTime 只能为14位数字, 如20210522092336")
    private String bindTime;

    /**
     * 号码A
     */
    @ApiModelProperty("号码A")
    @NotBlank(message = "aPhone 不能为空!")
    @Pattern(regexp = "^\\d{5,12}$", message = "aPhone 只能为5到12位数字")
    private String aPhone;

    /**
     * 号码B
     */
    @ApiModelProperty("号码B")
    @NotBlank(message = "bPhone 不能为空!")
    @Pattern(regexp = "^\\d{5,12}$", message = "bPhone 只能为5到12位数字")
    private String bPhone;

    /**
     * 号码X
     */
    @ApiModelProperty("号码X")
    @NotBlank(message = "xPhone 不能为空!")
    @Pattern(regexp = "^\\d{5,12}$", message = "xPhone 只能为5到12位数字")
    private String xPhone;

    /**
     * 号码Y
     */
    @ApiModelProperty("号码Y")
    @NotBlank(message = "yPhone 不能为空!")
    @Pattern(regexp = "^\\d{5,12}$", message = "yPhone 只能为5到12位数字")
    private String yPhone;

    /**
     * 有效时间 UNIXTIME
     */
    @ApiModelProperty("有效时间 20210522092336")
    @NotNull(message = "effectTime 不能为空!")
    private String effectTime;

    /**
     * 是否通话录音 默认不录音，0：不录音，1：录音
     */
    @ApiModelProperty("是否通话录音 默认不录音，0：不录音，1：录音")
    private Integer isRadioRecord;

    /**
     * 主叫放音文件名
     */
    @ApiModelProperty("主叫放音文件名")
    private String callerIvrName;

    /**
     * 被叫放音文件
     */
    @ApiModelProperty("被叫放音文件")
    private String calledIvrName;

    @ApiModelProperty(value = "地市编码")
    private String areaCode;

    @ApiModelProperty(value = "透传字段")
    private String userData;

    @ApiModelProperty(value = "城市编码, 全国池传0000")
    private String cityCode;

    @ApiModelProperty(value = " 是否使用全国/省池, 1 是, 默认0 否", example = "0")
    private Integer wholeArea;

    public String getUserData() {
        return userData;
    }

    public void setUserData(String userData) {
        this.userData = userData;
    }

    public String getStreamNumber() {
        return streamNumber;
    }

    public void setStreamNumber(String streamNumber) {
        this.streamNumber = streamNumber;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getBusinessId() {
        return businessId;
    }

    public void setBusinessId(String businessId) {
        this.businessId = businessId;
    }

    public Integer getOperation() {
        return operation;
    }

    public void setOperation(Integer operation) {
        this.operation = operation;
    }

    public String getBindId() {
        return bindId;
    }

    public void setBindId(String bindId) {
        this.bindId = bindId;
    }

    public String getBindTime() {
        return bindTime;
    }

    public void setBindTime(String bindTime) {
        this.bindTime = bindTime;
    }

    public String getaPhone() {
        return aPhone;
    }

    public void setaPhone(String aPhone) {
        this.aPhone = aPhone;
    }

    public String getbPhone() {
        return bPhone;
    }

    public void setbPhone(String bPhone) {
        this.bPhone = bPhone;
    }

    public String getxPhone() {
        return xPhone;
    }

    public void setxPhone(String xPhone) {
        this.xPhone = xPhone;
    }

    public String getyPhone() {
        return yPhone;
    }

    public void setyPhone(String yPhone) {
        this.yPhone = yPhone;
    }

    public String getEffectTime() {
        return effectTime;
    }

    public void setEffectTime(String effectTime) {
        this.effectTime = effectTime;
    }

    public Integer getIsRadioRecord() {
        return isRadioRecord;
    }

    public void setIsRadioRecord(Integer isRadioRecord) {
        this.isRadioRecord = isRadioRecord;
    }

    public String getCallerIvrName() {
        return callerIvrName;
    }

    public void setCallerIvrName(String callerIvrName) {
        this.callerIvrName = callerIvrName;
    }

    public String getCalledIvrName() {
        return calledIvrName;
    }

    public void setCalledIvrName(String calledIvrName) {
        this.calledIvrName = calledIvrName;
    }

    public String getAreaCode() {
        return areaCode;
    }

    public void setAreaCode(String areaCode) {
        this.areaCode = areaCode;
    }

    public String getCityCode() {
        return cityCode;
    }

    public void setCityCode(String cityCode) {
        this.cityCode = cityCode;
    }

    public Integer getWholeArea() {
        return wholeArea;
    }

    public void setWholeArea(Integer wholeArea) {
        this.wholeArea = wholeArea;
    }

    @Override
    public String toString() {
        return "BindOperationDTO{" +
                "streamNumber='" + streamNumber + '\'' +
                ", messageId='" + messageId + '\'' +
                ", businessId='" + businessId + '\'' +
                ", operation=" + operation +
                ", bindId='" + bindId + '\'' +
                ", bindTime='" + bindTime + '\'' +
                ", aPhone='" + aPhone + '\'' +
                ", bPhone='" + bPhone + '\'' +
                ", xPhone='" + xPhone + '\'' +
                ", yPhone='" + yPhone + '\'' +
                ", effectTime='" + effectTime + '\'' +
                ", isRadioRecord=" + isRadioRecord +
                ", callerIvrName='" + callerIvrName + '\'' +
                ", calledIvrName='" + calledIvrName + '\'' +
                ", areaCode='" + areaCode + '\'' +
                ", cityCode='" + cityCode + '\'' +
                ", wholeArea=" + wholeArea +
                '}';
    }
}
