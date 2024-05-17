package com.cqt.ivr.entity.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 获取埋点信息响应实体类
 *
 */
@Data
@ApiModel("触发埋点信息响应参数")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BuryingPointRes implements Serializable {

    private static final long serialVersionUID = -1L;

    @ApiModelProperty(value = "响应结果", example = "yes")
    private String result;

    @ApiModelProperty(value = "响应码", example = "0")
    private String code;

    @ApiModelProperty(value = "埋点信息接收码  1：成功    2.失败")
    private String buringPointcode;

}