package com.cqt.model.sms.dto;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.annotation.JSONField;
import io.swagger.annotations.Api;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * @author zhengsuhao
 * @date 2023/2/6
 */
@Api(tags = "青海阿里隐私号短信入参")
@Data
public class SmsInterceptRequest {
    private static final long serialVersionUID = 1351396541278396982L;

    @JSONField(name = "secret_no")
    private String secretNo;


    @JSONField(name = "call_no")
    private String callNo;


    @JSONField(name = "sms_content")
    private String smsContent;

    @JSONField(name = "mt_time")
    private String mtTime;


    @JSONField(name = "call_id")
    private String callId;


    @JSONField(name = "subs_id")
    private String subsId;


    @JSONField(name = "vendor_key")
    private String vendorKey;


    public String getApiMethodName() {
        return "alibaba.aliqin.axb.vendor.sms.intercept";
    }

    public Map<String, String> getTextParams(SmsInterceptRequest smsInterceptRequestDTO) {
        HashMap<String, String> txtParams = new HashMap<>(1);
        txtParams.put("sms_intercept_request", JSON.toJSONString(smsInterceptRequestDTO));
        return txtParams;
    }

    public SmsInterceptRequest() {
    }

    public SmsInterceptRequest(String secretNo, String callNo, String smsContent, String mtTime, String callId, String subsId, String vendorKey) {
        this.secretNo = secretNo;
        this.callNo = callNo;
        this.smsContent = smsContent;
        this.mtTime = mtTime;
        this.callId = callId;
        this.subsId = subsId;
        this.vendorKey = vendorKey;
    }
}
