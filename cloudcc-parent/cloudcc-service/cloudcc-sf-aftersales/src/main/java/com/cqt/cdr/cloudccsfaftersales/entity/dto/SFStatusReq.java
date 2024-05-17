package com.cqt.cdr.cloudccsfaftersales.entity.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 顺丰请求体类
 *
 * Created by xinson
 */
@Data
@ApiModel("顺丰获取状态参数")
public class SFStatusReq implements Serializable {

    private static final long serialVersionUID = -1L;

    /**
     * 工号
     */
    @ApiModelProperty(value = "工号")
    private String agentid;

    /**
     * 企业标识
     */
    @ApiModelProperty(value = "企业标识")
    private String vccid;

    /**
     * token
     */
    @ApiModelProperty(value = "token")
    private String token;
}
