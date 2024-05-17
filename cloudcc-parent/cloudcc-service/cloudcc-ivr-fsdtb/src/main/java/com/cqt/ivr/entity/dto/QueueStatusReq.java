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
public class QueueStatusReq implements Serializable {

    private static final long serialVersionUID = -1L;

    @ApiModelProperty(value = "技能组id", example = "100000_54858958955", required = true)
    private String sysQueueid;

    @ApiModelProperty(value = "企业标识")
    private String company_code;

}