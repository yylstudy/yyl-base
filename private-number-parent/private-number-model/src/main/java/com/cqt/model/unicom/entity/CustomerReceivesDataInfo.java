package com.cqt.model.unicom.entity;

import io.swagger.annotations.Api;

/**
 * @author zhengsuhao
 * @date 2022/12/6
 */
@Api(tags="话单客户接受报文信息")
public class CustomerReceivesDataInfo {

    private String messageId;

    private String acrCallId;

    private String callInNum;

    private String calledNum;

    private String displayNumber;

    private String callerStreamNo;

    private String startCallTime;

    private String stopCallTime;

    private int duration;

    private int callCost;

    private String callerRelCause;

    private String callerrelCause;

    private String callerOriRescode;

    private String calledStreamNo;

    private String startCalledTime;

    private int calledDuration;

    private int calledCost;

    private int releaseCause;

    private int calledOriRescode;

    private String srfmsgid;

    private String chargeNumber;

    private String callerRelReason;

    private String calledRelReason;

    private String msserver;

    private String calledDisplayNum;

    private String callerDisplayNum;

    public String servicekey;

    private String middleNumber;

    private String abStartCallTime;

    private String abStopCallTime;

    private String callerDuration;

    private String middleStartTime;

    private String middleCallTime;

    private String palyMode;

    private String callOutTime;

    private String dtmfKey;

    private String callRingTime;

    private String callAnswerTime;

    private String costCount;

    private String clientId;

    private String confId;

    private String sid;

    private String vccId;

    private String uuId;

    private String callAcrUrl;

    private String calledRelCause;

    private String key1;
    private String key2;
    private String key3;
    private String key4;
    private String key5;
    private String key6;
    private String key7;
    private String key8;
    private String key9;
    private String key10;


    private String releasereason;


    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getAcrCallId() {
        return acrCallId;
    }

    public void setAcrCallId(String acrCallId) {
        this.acrCallId = acrCallId;
    }

    public String getCallInNum() {
        return callInNum;
    }

    public void setCallInNum(String callInNum) {
        this.callInNum = callInNum;
    }

    public String getCalledNum() {
        return calledNum;
    }

    public void setCalledNum(String calledNum) {
        this.calledNum = calledNum;
    }

    public String getDisplayNumber() {
        return displayNumber;
    }

    public void setDisplayNumber(String displayNumber) {
        this.displayNumber = displayNumber;
    }

    public String getCallerStreamNo() {
        return callerStreamNo;
    }

    public void setCallerStreamNo(String callerStreamNo) {
        this.callerStreamNo = callerStreamNo;
    }

    public String getStartCallTime() {
        return startCallTime;
    }

    public void setStartCallTime(String startCallTime) {
        this.startCallTime = startCallTime;
    }

    public String getStopCallTime() {
        return stopCallTime;
    }

    public void setStopCallTime(String stopCallTime) {
        this.stopCallTime = stopCallTime;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public int getCallCost() {
        return callCost;
    }

    public void setCallCost(int callCost) {
        this.callCost = callCost;
    }

    public String getCallerRelCause() {
        return callerRelCause;
    }

    public void setCallerRelCause(String callerRelCause) {
        this.callerRelCause = callerRelCause;
    }

    public String getCallerrelCause() {
        return callerrelCause;
    }

    public void setCallerrelCause(String callerrelCause) {
        this.callerrelCause = callerrelCause;
    }

    public String getCallerOriRescode() {
        return callerOriRescode;
    }

    public void setCallerOriRescode(String callerOriRescode) {
        this.callerOriRescode = callerOriRescode;
    }

    public String getCalledStreamNo() {
        return calledStreamNo;
    }

    public void setCalledStreamNo(String calledStreamNo) {
        this.calledStreamNo = calledStreamNo;
    }

    public String getStartCalledTime() {
        return startCalledTime;
    }

    public void setStartCalledTime(String startCalledTime) {
        this.startCalledTime = startCalledTime;
    }

    public int getCalledDuration() {
        return calledDuration;
    }

    public void setCalledDuration(int calledDuration) {
        this.calledDuration = calledDuration;
    }

    public int getCalledCost() {
        return calledCost;
    }

    public void setCalledCost(int calledCost) {
        this.calledCost = calledCost;
    }

    public int getReleaseCause() {
        return releaseCause;
    }

    public void setReleaseCause(int releaseCause) {
        this.releaseCause = releaseCause;
    }

    public int getCalledOriRescode() {
        return calledOriRescode;
    }

    public void setCalledOriRescode(int calledOriRescode) {
        this.calledOriRescode = calledOriRescode;
    }

    public String getSrfmsgid() {
        return srfmsgid;
    }

    public void setSrfmsgid(String srfmsgid) {
        this.srfmsgid = srfmsgid;
    }

    public String getChargeNumber() {
        return chargeNumber;
    }

    public void setChargeNumber(String chargeNumber) {
        this.chargeNumber = chargeNumber;
    }

    public String getCallerRelReason() {
        return callerRelReason;
    }

    public void setCallerRelReason(String callerRelReason) {
        this.callerRelReason = callerRelReason;
    }

    public String getCalledRelReason() {
        return calledRelReason;
    }

    public void setCalledRelReason(String calledRelReason) {
        this.calledRelReason = calledRelReason;
    }

    public String getMsserver() {
        return msserver;
    }

    public void setMsserver(String msserver) {
        this.msserver = msserver;
    }

    public String getCalledDisplayNum() {
        return calledDisplayNum;
    }

    public void setCalledDisplayNum(String calledDisplayNum) {
        this.calledDisplayNum = calledDisplayNum;
    }

    public String getCallerDisplayNum() {
        return callerDisplayNum;
    }

    public void setCallerDisplayNum(String callerDisplayNum) {
        this.callerDisplayNum = callerDisplayNum;
    }

    public String getServicekey() {
        return servicekey;
    }

    public void setServicekey(String servicekey) {
        this.servicekey = servicekey;
    }

    public String getMiddleNumber() {
        return middleNumber;
    }

    public void setMiddleNumber(String middleNumber) {
        this.middleNumber = middleNumber;
    }

    public String getAbStartCallTime() {
        return abStartCallTime;
    }

    public void setAbStartCallTime(String abStartCallTime) {
        this.abStartCallTime = abStartCallTime;
    }

    public String getAbStopCallTime() {
        return abStopCallTime;
    }

    public void setAbStopCallTime(String abStopCallTime) {
        this.abStopCallTime = abStopCallTime;
    }

    public String getCallerDuration() {
        return callerDuration;
    }

    public void setCallerDuration(String callerDuration) {
        this.callerDuration = callerDuration;
    }

    public String getMiddleStartTime() {
        return middleStartTime;
    }

    public void setMiddleStartTime(String middleStartTime) {
        this.middleStartTime = middleStartTime;
    }

    public String getMiddleCallTime() {
        return middleCallTime;
    }

    public void setMiddleCallTime(String middleCallTime) {
        this.middleCallTime = middleCallTime;
    }

    public String getPalyMode() {
        return palyMode;
    }

    public void setPalyMode(String palyMode) {
        this.palyMode = palyMode;
    }

    public String getCallOutTime() {
        return callOutTime;
    }

    public void setCallOutTime(String callOutTime) {
        this.callOutTime = callOutTime;
    }

    public String getDtmfKey() {
        return dtmfKey;
    }

    public void setDtmfKey(String dtmfKey) {
        this.dtmfKey = dtmfKey;
    }

    public String getCallRingTime() {
        return callRingTime;
    }

    public void setCallRingTime(String callRingTime) {
        this.callRingTime = callRingTime;
    }

    public String getCallAnswerTime() {
        return callAnswerTime;
    }

    public void setCallAnswerTime(String callAnswerTime) {
        this.callAnswerTime = callAnswerTime;
    }

    public String getCostCount() {
        return costCount;
    }

    public void setCostCount(String costCount) {
        this.costCount = costCount;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getConfId() {
        return confId;
    }

    public void setConfId(String confId) {
        this.confId = confId;
    }

    public String getSid() {
        return sid;
    }

    public void setSid(String sid) {
        this.sid = sid;
    }

    public String getVccId() {
        return vccId;
    }

    public void setVccId(String vccId) {
        this.vccId = vccId;
    }

    public String getUuId() {
        return uuId;
    }

    public void setUuId(String uuId) {
        this.uuId = uuId;
    }

    public String getCallAcrUrl() {
        return callAcrUrl;
    }

    public void setCallAcrUrl(String callAcrUrl) {
        this.callAcrUrl = callAcrUrl;
    }

    public String getCalledRelCause() {
        return calledRelCause;
    }

    public void setCalledRelCause(String calledRelCause) {
        this.calledRelCause = calledRelCause;
    }

    public String getKey1() {
        return key1;
    }

    public void setKey1(String key1) {
        this.key1 = key1;
    }

    public String getKey2() {
        return key2;
    }

    public void setKey2(String key2) {
        this.key2 = key2;
    }

    public String getKey3() {
        return key3;
    }

    public void setKey3(String key3) {
        this.key3 = key3;
    }

    public String getKey4() {
        return key4;
    }

    public void setKey4(String key4) {
        this.key4 = key4;
    }

    public String getKey5() {
        return key5;
    }

    public void setKey5(String key5) {
        this.key5 = key5;
    }

    public String getKey6() {
        return key6;
    }

    public void setKey6(String key6) {
        this.key6 = key6;
    }

    public String getKey7() {
        return key7;
    }

    public void setKey7(String key7) {
        this.key7 = key7;
    }

    public String getKey8() {
        return key8;
    }

    public void setKey8(String key8) {
        this.key8 = key8;
    }

    public String getKey9() {
        return key9;
    }

    public void setKey9(String key9) {
        this.key9 = key9;
    }

    public String getKey10() {
        return key10;
    }

    public void setKey10(String key10) {
        this.key10 = key10;
    }

    public String getReleasereason() {
        return releasereason;
    }

    public void setReleasereason(String releasereason) {
        this.releasereason = releasereason;
    }
}
