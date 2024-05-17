package com.cqt.ivr.entity.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 上报响应信息
 *
 */
@Data
@ApiModel("上报响应信息")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SubmissionRes implements Serializable {

    private static final long serialVersionUID = -1L;

    @ApiModelProperty(value = "响应结果", example = "yes")
    private String result;

    @ApiModelProperty(value = "响应码", example = "0")
    private String code;

    @ApiModelProperty(value = "上报响应码  1:成功 2：失败")
    private String submissionCode;


}