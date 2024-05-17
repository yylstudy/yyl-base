package com.cqt.ivr.entity.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 判断货物重量请求信息
 *
 */
@Data
@ApiModel("判断货物重量请求信息")
public class JudgeWeightReq implements Serializable {

    private static final long serialVersionUID = -1L;

    @ApiModelProperty(value = "按键信息", example = "100", required = true)
    private String weightPushKey;

    @ApiModelProperty(value = "最大重量（超过定值重量） 和minWeight最少有一个要有值", example = "130")
    private String maxWeight;

    @ApiModelProperty(value = "最小重量（小于定值重量） 和maxWeight最少有一个要有值", example = "80")
    private String minWeight;

}