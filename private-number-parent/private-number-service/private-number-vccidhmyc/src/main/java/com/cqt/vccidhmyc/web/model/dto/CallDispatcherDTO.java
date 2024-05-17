package com.cqt.vccidhmyc.web.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * @author linshiqiang
 * date:  2023-03-27 14:13
 */
@Data
public class CallDispatcherDTO {

    @JsonProperty("CALLERNUM")
    String callerNum;

    @JsonProperty("CALLEDNUM")
    String calledNum;

    String callId;
}
