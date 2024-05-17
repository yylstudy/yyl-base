package com.cqt.model.call.dto;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * @author linshiqiang
 * date:  2023-02-06 15:11
 * 淘宝接口查询绑定入参
 */
@Data
public class StartCallRequest {

    private static final long serialVersionUID = 1351396541228396981L;

    private String apiMethodName;

    @JSONField(name = "call_id")
    private String callId;

    @JSONField(name = "call_no")
    private String callNo;

    @JSONField(name = "call_time")
    private String callTime;

    @JSONField(name = "extension")
    private String extension;

    @JSONField(name = "record_type")
    private String recordType;

    @JSONField(name = "secret_no")
    private String secretNo;

    @JSONField(name = "vendor_key")
    private String vendorKey;

    public StartCallRequest() {
    }

    public StartCallRequest(String callId,
                            String callNo,
                            String callTime,
                            String extension,
                            String recordType,
                            String secretNo,
                            String vendorKey) {
        this.callId = callId;
        this.callNo = callNo;
        this.callTime = callTime;
        this.extension = extension;
        this.recordType = recordType;
        this.secretNo = secretNo;
        this.vendorKey = vendorKey;
    }

    public String getApiMethodName() {
        return "alibaba.aliqin.axb.vendor.call.control";
    }

    public Map<String, String> getTextParams(StartCallRequest startCallRequest) {
        HashMap<String, String> txtParams = new HashMap<>(1);
        txtParams.put("start_call_request", JSON.toJSONString(startCallRequest));
        return txtParams;
    }
}
