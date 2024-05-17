package com.cqt.model.freeswitch.base;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @author linshiqiang
 * date:  2023-07-03 11:07
 * fs 事件基类
 */
@Data
public class FreeswitchEventBase implements Serializable {

    private static final long serialVersionUID = 2221097955764807176L;

    @JsonProperty("event")
    private String event;

    @JsonProperty("server_id")
    private String serverId;

    /**
     * 业务码
     */
    @JsonProperty("service_code")
    private String serviceCode;

    /**
     * 时间戳
     */
    @JsonProperty("timestamp")
    private Long timestamp;

    /**
     * 通话id
     */
    @JsonProperty("uuid")
    private String uuid;

    /**
     * 企业id
     */
    @JsonProperty("company_code")
    private String companyCode;

}
