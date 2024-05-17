package com.cqt.model.push.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 内部话单类
 * @author hlx
 * @date 2021-09-15
 */
@Data
@NoArgsConstructor
public class AcrRecordOrg {
    private String messageId;
    private String acrCallId;
    private String callInNum;
    private String calledNum;
    private String displayNumber;
    private String callerStreamNo;
    private String startCallTime;
    private String stopCallTime;
    private String duration;
    private String callCost;

    public String getCallerrelCause() {
        return callerrelCause;
    }

    public void setCallerrelCause(String callerrelCause) {
        this.callerrelCause = callerrelCause;
    }

    private String callerrelCause;
    private String callerOriRescode;
    private String calledStreamNo;
    private String startCalledTime;
    private String calledDuration;
    private String calledCost;
    private String releaseCause;
    private String calledOriRescode;
    private String srfmsgid;
    private String chargeNumber;
    private String callerRelReason;
    private String calledRelReason;
    private String msserver;
    private String calledDisplayNum;
    private String callerDisplayNum;
    private String servicekey;
    private String middleNumber;
    private String abStartCallTime;
    private String abStopCallTime;
    private String callerDuration;
    private String middleStartTime;
    private String middleCallTime;
    private String playMode;
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

    public void setCallerRelCause(String callerRelCause) {
        this.callerRelCause = callerRelCause;
    }

    public String getCallerRelCause() {
        return callerRelCause;
    }

    private String callerRelCause;
    private Object key1;
    private String key2;
    private Object key3;
    private Object key4;
    private Object key5;
    private Object key6;
    private Object key7;
    private Object key8;
    private Object key9;
    private Object key10;
}
