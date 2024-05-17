package com.cqt.model.push.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * @author zhengsuhao
 * date:  2023-02-08
 * 青海移动阿里供应商推送通话结束事件返回参数
 */
@Data
public class CallReleaseResponse {

    @JsonProperty("alibaba_aliqin_axb_vendor_push_call_release_response")
    private AlibabaAliqinAxbVendorPushCallReleaseResponse alibabaAliqinAxbVendorPushCallReleaseResponse;
}
