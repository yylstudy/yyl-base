package com.cqt.model.bind.axb.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author huweizhong
 * date  2023/8/25 16:54
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class AybBindDTO {

    private String appkey;
    private String ts;
    private String sign;
    @JsonProperty("request_id")
    private String requestId;
    @JsonProperty("tel_a")
    private String telA;
    @JsonProperty("tel_b")
    private String telB;
    @JsonProperty("area_code")
    private String areaCode;
    private String expiration;
    @JsonProperty("audio_a_call_x")
    private String audioACallX;
    @JsonProperty("audio_b_call_x")
    private String audioBCallX;
    @JsonProperty("audio_a_called_x")
    private String audioACalledX;
    @JsonProperty("audio_b_called_x")
    private String audioBCalledX;
    private String wholearea;
    @JsonProperty("enable_record")
    private String enableRecord;



}
