package com.cqt.ivr.entity.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 判断货物重量响应信息
 *
 */
@Data
@ApiModel("判断货物重量响应信息")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class JudgeWeightRes implements Serializable {

    private static final long serialVersionUID = -1L;

    @ApiModelProperty(value = "响应结果", example = "yes")
    private String result;

    @ApiModelProperty(value = "响应码", example = "0")
    private String code;

    @ApiModelProperty(value = "重量信息  1:输入错误     2：正常    3：超过或低于")
    private String weightCode;
}