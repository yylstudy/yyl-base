package com.cqt.ivr.entity.dto;

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
@ApiModel("顺丰请求参数")
public class SFWOReq implements Serializable {

    private static final long serialVersionUID = -1L;

    /**
     * 工号
     */
    @ApiModelProperty(value = "工号")
    private String agentid;

    /**
     * 开头字母
     */
    @ApiModelProperty(value = "单号开头字母")
    private String numberbegin;

    /**
     * 单号数字部分
     */
    @ApiModelProperty(value = "单号数字部分")
    private String inquiryno;

    /**
     * 语速
     */
    @ApiModelProperty(value = "百度语速   填1-5")
    private String speed;

    @ApiModelProperty(value = "捷通华声音量   填[0,100]")
    private String volume;

}
