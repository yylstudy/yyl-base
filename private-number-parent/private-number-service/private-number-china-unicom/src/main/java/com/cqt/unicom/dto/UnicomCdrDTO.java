package com.cqt.unicom.dto;

import io.swagger.annotations.ApiModelProperty;

/**
 * @author huweizhong
 * date  2023/7/10 11:19
 * 联通话单
 */

public class UnicomCdrDTO {

    @Override
    public String toString() {
        return "UnicomCdrDTO{" +
                "id='" + id + '\'' +
                ", uId='" + uId + '\'' +
                ", serviceType='" + serviceType + '\'' +
                ", inboundCaller='" + inboundCaller + '\'' +
                ", inboundCallee='" + inboundCallee + '\'' +
                ", outboundCaller='" + outboundCaller + '\'' +
                ", outboundCallee='" + outboundCallee + '\'' +
                ", startTime='" + startTime + '\'' +
                ", ringTime='" + ringTime + '\'' +
                ", connectTime='" + connectTime + '\'' +
                ", endTime='" + endTime + '\'' +
                ", talkTime=" + talkTime +
                ", releaseDir=" + releaseDir +
                ", releaseCause=" + releaseCause +
                ", recordUrl='" + recordUrl + '\'' +
                ", dtmfValue='" + dtmfValue + '\'' +
                ", smsCnt='" + smsCnt + '\'' +
                '}';
    }

    @ApiModelProperty(value = "语音 call-id")
    private String id;

    @ApiModelProperty(value = "客户平台指定 ID 标识")
    private String uId;

    @ApiModelProperty(value = "业务类型：voice")
    private String serviceType;

    @ApiModelProperty(value = "呼入主叫")
    private String inboundCaller;

    @ApiModelProperty(value = "呼入被叫")
    private String inboundCallee;

    @ApiModelProperty(value = "呼出主叫")
    private String outboundCaller;

    @ApiModelProperty(value = "呼出被叫")
    private String outboundCallee;

    @ApiModelProperty(value = "呼叫起始时间")
    private String startTime;

    @ApiModelProperty(value = "振铃时间")
    private String ringTime;

    @ApiModelProperty(value = "接通时间")
    private String connectTime;

    @ApiModelProperty(value = "呼叫结束时间")
    private String endTime;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setuId(String uId) {
        this.uId = uId;
    }

    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }

    public void setInboundCaller(String inboundCaller) {
        this.inboundCaller = inboundCaller;
    }

    public void setInboundCallee(String inboundCallee) {
        this.inboundCallee = inboundCallee;
    }

    public void setOutboundCaller(String outboundCaller) {
        this.outboundCaller = outboundCaller;
    }

    public void setOutboundCallee(String outboundCallee) {
        this.outboundCallee = outboundCallee;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public void setRingTime(String ringTime) {
        this.ringTime = ringTime;
    }

    public void setConnectTime(String connectTime) {
        this.connectTime = connectTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public void setTalkTime(Integer talkTime) {
        this.talkTime = talkTime;
    }

    public void setReleaseDir(Integer releaseDir) {
        this.releaseDir = releaseDir;
    }

    public void setReleaseCause(Integer releaseCause) {
        this.releaseCause = releaseCause;
    }

    public void setRecordUrl(String recordUrl) {
        this.recordUrl = recordUrl;
    }

    public void setDtmfValue(String dtmfValue) {
        this.dtmfValue = dtmfValue;
    }

    public void setSmsCnt(String smsCnt) {
        this.smsCnt = smsCnt;
    }

    public String getuId() {
        return uId;
    }

    public String getServiceType() {
        return serviceType;
    }

    public String getInboundCaller() {
        return inboundCaller;
    }

    public String getInboundCallee() {
        return inboundCallee;
    }

    public String getOutboundCaller() {
        return outboundCaller;
    }

    public String getOutboundCallee() {
        return outboundCallee;
    }

    public String getStartTime() {
        return startTime;
    }

    public String getRingTime() {
        return ringTime;
    }

    public String getConnectTime() {
        return connectTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public Integer getTalkTime() {
        return talkTime;
    }

    public Integer getReleaseDir() {
        return releaseDir;
    }

    public Integer getReleaseCause() {
        return releaseCause;
    }

    public String getRecordUrl() {
        return recordUrl;
    }

    public String getDtmfValue() {
        return dtmfValue;
    }

    public String getSmsCnt() {
        return smsCnt;
    }

    @ApiModelProperty(value = "通话时长")
    private Integer talkTime;

    /**
     * 0：主叫释放
     * 1：被叫释放
     * 2：平台释放
     */
    @ApiModelProperty(value = "释放原因 ")
    private Integer releaseDir;

    @ApiModelProperty(value = "话务分析 ")
    private Integer releaseCause;

    @ApiModelProperty(value = "录音文件 ")
    private String recordUrl;

    @ApiModelProperty(value = "挂断前的 DTMF 收号结果 ")
    private String dtmfValue;

    @ApiModelProperty(value = "短信（拆分）条数 ")
    private String smsCnt;

    public String getInteractDtmfValues() {
        return interactDtmfValues;
    }

    public void setInteractDtmfValues(String interactDtmfValues) {
        this.interactDtmfValues = interactDtmfValues;
    }

    @ApiModelProperty(value = "分机号")
    private String interactDtmfValues;


}
