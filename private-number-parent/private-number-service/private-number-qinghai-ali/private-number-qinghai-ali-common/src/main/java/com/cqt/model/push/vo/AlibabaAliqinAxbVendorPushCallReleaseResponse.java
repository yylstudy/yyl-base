package com.cqt.model.push.vo;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;


/**
 * @author zhengsuhao
 * date:  2023-02-08
 * 青海移动阿里供应商推送通话结束事件返回参数
 */
@Data
public class AlibabaAliqinAxbVendorPushCallReleaseResponse {

    @JsonProperty("result")
    private Response result;

    @Data
    public static class Response {
        @JsonProperty("message")
        private String message;

        @JsonProperty("module")
        private boolean module;

        @JsonProperty("code")
        private String code;
    }
}
