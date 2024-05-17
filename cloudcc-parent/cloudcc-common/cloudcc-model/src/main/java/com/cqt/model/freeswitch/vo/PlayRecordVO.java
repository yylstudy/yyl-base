package com.cqt.model.freeswitch.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@NoArgsConstructor
@Data
public class PlayRecordVO implements Serializable {
    private static final long serialVersionUID = -6842057420803459739L;

    @JsonProperty("req_id")
    private String reqId;
    @JsonProperty("result")
    private Boolean result;
    @JsonProperty("msg")
    private String msg;
    @JsonProperty("ws_rtsp")
    private String wsRtsp;
    @JsonProperty("record_url_rtsp")
    private String recordUrlRtsp;
    @JsonProperty("record_url")
    private String recordUrl;
    @JsonProperty("record_url_in")
    private String recordUrlIn;
}
