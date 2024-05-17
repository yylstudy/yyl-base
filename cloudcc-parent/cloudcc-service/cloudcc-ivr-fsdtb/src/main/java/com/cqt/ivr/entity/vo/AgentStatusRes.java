package com.cqt.ivr.entity.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 获取坐席状态响应信息
 *
 */
@Data
@ApiModel("获取坐席状态响应信息")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AgentStatusRes implements Serializable {

    private static final long serialVersionUID = -1L;

    @ApiModelProperty(value = "响应结果", example = "yes")
    private String result;

    @ApiModelProperty(value = "响应码", example = "0")
    private String code;

    @ApiModelProperty(value = "坐席信息  1:空闲     2：忙碌    3：离线")
    private String agentStatus;
}