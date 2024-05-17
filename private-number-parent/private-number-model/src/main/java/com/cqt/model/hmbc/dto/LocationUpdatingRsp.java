package com.cqt.model.hmbc.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 位置更新的响应参数
 *
 * @author Xienx
 * @date 2022年05月24日 14:06
 */
@Data
@ApiModel(value = "位置更新的响应参数")
public class LocationUpdatingRsp implements Serializable {

    private static final long serialVersionUID = 196076659040437355L;

    /**
     * X号码
     */
    @ApiModelProperty(value = "X号码")
    private String number;

    /**
     * 位置更新请求的唯一标识
     */
    @ApiModelProperty(value = "位置更新请求的唯一标识")
    private String messageId;

    /**
     * 号码的imsi码
     */
    @ApiModelProperty(value = "号码的imsi码")
    private String imsi;

    /**
     * 状态码, 详见对照表说明
     */
    @ApiModelProperty(value = "状态码, 详见对照表说明")
    private Integer status;

    /**
     * 失败原因, 详见对照表说明
     */
    @ApiModelProperty(value = "失败原因, 详见对照表说明")
    private String errorReason;
}
