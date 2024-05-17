package com.cqt.ivr.entity.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 通过开头按键和工单获取tts信息响应实体类
 *
 */
@Data
@ApiModel("通过开头按键和工单获取tts信息响应参数")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SFWORes implements Serializable {

    private static final long serialVersionUID = -1L;

    @ApiModelProperty(value = "响应结果", example = "yes")
    private String result;

    @ApiModelProperty(value = "响应码", example = "0")
    private String code;

    @ApiModelProperty(value = "tts路径")
    private String ttsUrl;
}