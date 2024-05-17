package com.cqt.model.call.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * @author linshiqiang
 * date:  2023-02-06 16:28
 */
@Data
public class CallControlResponse {

    @JsonProperty("alibaba_aliqin_axb_vendor_call_control_response")
    private AlibabaAliqinAxbVendorCallControlResponse alibabaAliqinAxbVendorCallControlResponse;
}
