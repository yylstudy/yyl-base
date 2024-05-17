package com.cqt.ivr.entity.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 任务实体类
 *
 * Created by xinson
 */
@Data
@ApiModel("通用IVR请求参数")
public class CommIvrReq implements Serializable {

    private static final long serialVersionUID = -1L;

    /**
     * 企业
     */
    @ApiModelProperty(value = "企业（所有接口必填）", required = true)
    private String company_code;

    /**
     * 坐席工号
     */
    @ApiModelProperty(value = "坐席工号")
    private String empAccId;

    /**
     * 页码
     */
    @ApiModelProperty(value = "页码", example = "1")
    private Integer pageNo;

    /**
     * 每页数量
     */
    @ApiModelProperty(value = "每页数量", example = "10")
    private Integer pageSize;

    /**
     * 关键字
     */
    @ApiModelProperty(value = "关键字（搜索条件）")
    private String keyword;

}
