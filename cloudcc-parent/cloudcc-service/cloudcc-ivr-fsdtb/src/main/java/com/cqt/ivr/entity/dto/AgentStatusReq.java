package com.cqt.ivr.entity.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 获取坐席状态请求信息
 *
 */
@Data
@ApiModel("获取坐席状态请求信息")
public class AgentStatusReq implements Serializable {

    private static final long serialVersionUID = -1L;

    @ApiModelProperty(value = "企业标识", example = "100000", required = true)
    private String company_code;

    @ApiModelProperty(value = "坐席", example = "1000", required = true)
    private String agentid;

}