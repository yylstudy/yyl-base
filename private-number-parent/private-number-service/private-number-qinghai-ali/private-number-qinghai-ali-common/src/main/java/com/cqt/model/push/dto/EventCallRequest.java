package com.cqt.model.push.dto;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * @author zhengsuhao
 * date:  2023-02-08
 * 青海移动阿里供应商推送通话事件入参
 */
@Data
public class EventCallRequest {

    private static final long serialVersionUID = 1351498541278396982L;


    @JSONField(name = "call_id")
    private String callId;

    @JSONField(name = "called_no")
    private String calledNo;

    @JSONField(name = "call_no")
    private String callNo;

    @JSONField(name = "extension_no")
    private String extensionNo;

    @JSONField(name = "event_type")
    private String eventType;

    @JSONField(name = "subs_id")
    private String subsId;

    @JSONField(name = "vendor_key")
    private String vendorKey;

    @JSONField(name = "secret_no")
    private String secretNo;

    @JSONField(name = "event_time")
    private String eventTime;

    @JSONField(name = "called_display_no")
    private String calledDisplayNo;

    @JSONField(name = "call_time")
    private String callTime;

    public String getApiMethodName() {
        return "alibaba.aliqin.axb.vendor.push.call.event";
    }

    public Map<String, String> getTextParams(EventCallRequest eventCallRequest) {
        HashMap<String, String> txtParams = new HashMap<>(1);
        txtParams.put("event_call_request", JSON.toJSONString(eventCallRequest));
        return txtParams;
    }

    public EventCallRequest() {
    }

    public EventCallRequest(String callId, String calledNo, String callNo, String extensionNo, String eventType, String subsId, String vendorKey, String secretNo, String eventTime, String calledDisplayNo, String callTime) {
        this.callId = callId;
        this.calledNo = calledNo;
        this.callNo = callNo;
        this.extensionNo = extensionNo;
        this.eventType = eventType;
        this.subsId = subsId;
        this.vendorKey = vendorKey;
        this.secretNo = secretNo;
        this.eventTime = eventTime;
        this.calledDisplayNo = calledDisplayNo;
        this.callTime = callTime;
    }
}
