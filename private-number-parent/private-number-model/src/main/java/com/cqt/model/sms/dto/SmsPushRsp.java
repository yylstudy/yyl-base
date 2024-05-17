package com.cqt.model.sms.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @Description: 短信推送结果
 * @author: scott
 * @date: 2022年03月29日 11:38
 */
@Data
@ApiModel(description = "短信推送结果")
public class SmsPushRsp implements Serializable {
    private static final long serialVersionUID = -7098861206723983663L;

    /**
     * 接口成功响应码 0
     * */
    public static final Integer SUC_CODE = 0;

    /**
     * 响应码
     * */
    @ApiModelProperty(value = "响应码")
    private Integer code;

    /**
     * 响应信息
     * */
    @ApiModelProperty(value = "响应信息")
    private String message;
}
