package com.cqt.wechat.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author huweizhong
 * date  2023/2/23 11:11
 */
@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class StatusInfo {
    /**
     * CallEvent
     */
    @JsonProperty("event")
    @ApiModelProperty(value = " CallEvent")
    private String event;

    /**
     * 话单基础信息
     */
    @JsonProperty("base_info")
    @ApiModelProperty(value = "话单基础信息")
    private WechatCdrInfo.CallBaseInfo baseInfo;


    /**
     * 话单状态信息
     */
    @JsonProperty("status")
    @ApiModelProperty(value = "话单状态信息")
    private WechatCdrInfo.CallStatus status;
}
