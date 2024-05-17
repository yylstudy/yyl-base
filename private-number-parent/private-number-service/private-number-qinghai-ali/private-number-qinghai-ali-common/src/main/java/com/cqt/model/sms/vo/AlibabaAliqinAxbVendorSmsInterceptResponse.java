package com.cqt.model.sms.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.Api;
import lombok.Data;

/**
 * @author zhengsuhao
 * @date 2023/2/6
 */
@Api(tags = "青海阿里隐私号短信出参")
@Data
public class AlibabaAliqinAxbVendorSmsInterceptResponse {

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
