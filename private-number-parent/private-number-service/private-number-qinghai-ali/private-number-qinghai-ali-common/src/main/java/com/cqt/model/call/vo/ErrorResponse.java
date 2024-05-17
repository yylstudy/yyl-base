package com.cqt.model.call.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ErrorResponse {

    @JsonProperty("code")
    private Long code;

    @JsonProperty("msg")
    private String msg;

    @JsonProperty("sub_code")
    private String subCode;

    @JsonProperty("sub_msg")
    private String subMsg;

}
