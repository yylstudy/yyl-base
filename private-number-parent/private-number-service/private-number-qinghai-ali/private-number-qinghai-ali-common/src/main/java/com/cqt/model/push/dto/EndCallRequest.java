package com.cqt.model.push.dto;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * @author zhengsuhao
 * date:  2023-02-08
 * 青海移动阿里供应商推送通话结束事件入参
 */
@Data
public class EndCallRequest {
    private static final long serialVersionUID = 1351398541278396982L;

    @JSONField(name = "release_cause")
    private Integer releaseCause;

    @JSONField(name = "call_id")
    private String callId;

    @JSONField(name = "ring_time")
    private String ringTime;

    @JSONField(name = "start_time")
    private String startTime;

    @JSONField(name = "secret_no")
    private String secretNo;

    @JSONField(name = "call_out_time")
    private String callOutTime;

    @JSONField(name = "release_dir")
    private Integer releaseDir;

    @JSONField(name = "release_time")
    private String releaseTime;

    @JSONField(name = "subs_id")
    private String subsId;

    @JSONField(name = "vendor_key")
    private String vendorKey;

    @JSONField(name = "free_ring_time")
    private String freeRingTime;

    @JSONField(name = "sms_number")
    private Integer smsNumber;

    @JSONField(name = "record_url")
    private String recordUrl;

    @JSONField(name = "call_result")
    private String callResult;

    @JSONField(name = "ringing_record_url")
    private String ringingRecordUrl;

    @JSONField(name = "call_type")
    private String callType;

    @JSONField(name = "call_no")
    private String callNo;

    @JSONField(name = "called_no")
    private String calledNo;

    @JSONField(name = "extension_no")
    private String extensionNo;

    @JSONField(name = "end_call_ivr_dtmf")
    private String endCallIvrDtmf;

    public String getApiMethodName() {
        return "alibaba.aliqin.axb.vendor.push.call.release";
    }

    public Map<String, String> getTextParams(EndCallRequest endCallRequest) {
        HashMap<String, String> txtParams = new HashMap<>(1);
        txtParams.put("end_call_request", JSON.toJSONString(endCallRequest));
        return txtParams;
    }

    public EndCallRequest() {
    }

    public EndCallRequest(Integer releaseCause, String callId, String ringTime, String startTime, String secretNo, String callOutTime, Integer releaseDir, String releaseTime, String subsId, String vendorKey, String freeRingTime, Integer smsNumber, String recordUrl, String callResult, String ringingRecordUrl, String callType, String callNo, String calledNo, String extensionNo, String endCallIvrDtmf) {
        this.releaseCause = releaseCause;
        this.callId = callId;
        this.ringTime = ringTime;
        this.startTime = startTime;
        this.secretNo = secretNo;
        this.callOutTime = callOutTime;
        this.releaseDir = releaseDir;
        this.releaseTime = releaseTime;
        this.subsId = subsId;
        this.vendorKey = vendorKey;
        this.freeRingTime = freeRingTime;
        this.smsNumber = smsNumber;
        this.recordUrl = recordUrl;
        this.callResult = callResult;
        this.ringingRecordUrl = ringingRecordUrl;
        this.callType = callType;
        this.callNo = callNo;
        this.calledNo = calledNo;
        this.extensionNo = extensionNo;
        this.endCallIvrDtmf = endCallIvrDtmf;
    }
}
 