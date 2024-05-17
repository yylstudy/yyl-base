package com.cqt.sms.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;

/**
 *绑定关系
 */
@Data
public class BindInfo implements Serializable {


    @JsonProperty("code")
    private Integer code;
    @JsonProperty("message")
    private String message;
    @JsonProperty("called_num")
    private String calledNum;
    @JsonProperty("caller_ivr")
    private String callerIvr;
    @JsonProperty("called_ivr")
    private String calledIvr;
    @JsonProperty("caller_ivr_before")
    private String callerIvrBefore;
    @JsonProperty("enable_record")
    private int enableRecord;
    @JsonProperty("num_type")
    private String numType;
    @JsonProperty("type")
    private int type;
    @JsonProperty("max_duration")
    private int maxDuration;
    @JsonProperty("area_code")
    private String areaCode;
    @JsonProperty("bind_id")
    private String bindId;
    @JsonProperty("display_num")
    private String displayNum;
    @JsonProperty("user_data")
    private String userData;
    @JsonProperty("vccId")
    private String vccId;
    @JsonProperty("call_num")
    private String callNum;

    /**
     * 绑定关系透传字段
     */
    @JsonProperty("transfer_data")
    private Object transferData;

    private long ts;
    private String sign;
    private String request_id;


    private String called_ivr_before;

    private Object source_bind_id;
    private Object source_request_id;
    private String source_bind_time;
    private String bind_time;
    private String call_type;
    private Object ayb_first;
}
