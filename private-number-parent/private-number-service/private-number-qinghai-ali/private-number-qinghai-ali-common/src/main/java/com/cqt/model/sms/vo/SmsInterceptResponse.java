package com.cqt.model.sms.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * @author zhengsuhao
 * @date 2023/2/6
 */
@Data
public class SmsInterceptResponse {

    @JsonProperty("alibaba_aliqin_axb_vendor_sms_intercept_response")
    private AlibabaAliqinAxbVendorSmsInterceptResponse alibabaAliqinAxbVendorSmsInterceptResponse;


}
