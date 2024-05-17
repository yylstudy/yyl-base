package com.cqt.model.hmbc.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 定时拨测号码详情查询
 *
 * @author scott
 * @date 2022年07月07日 17:09
 */
@Data
@ApiModel(description = "拨测号码")
public class DialTestNumberDTO implements Serializable {

    private static final long serialVersionUID = 3006721503003331012L;

    /**
     * 企业VCCID
     */
    @ApiModelProperty(value = "企业VCCID")
    private String vccId;

    /**
     * 企业名称
     */
    @ApiModelProperty(value = "企业名称")
    private String vccName;

    /**
     * 业务模式
     */
    @ApiModelProperty(value = "业务模式")
    private String businessType;

    /**
     * 号码池类型
     */
    @ApiModelProperty(value = "号码池类型")
    private String poolType;

    /**
     * 号码类型
     */
    @ApiModelProperty(value = "号码类型")
    private Integer numType;

    /**
     * 归属供应商
     */
    @ApiModelProperty(value = "归属供应商id")
    private String supplierId;

    /**
     * 归属供应商名称
     */
    @ApiModelProperty(value = "归属供应商名称")
    private String supplierName;

    /**
     * 所属地市编码
     */
    @ApiModelProperty(value = "所属地市编码")
    private String areaCode;

    /**
     * 所属地市名称
     */
    @ApiModelProperty(value = "所属地市名称")
    private String areaName;

    /**
     * 号码
     */
    @ApiModelProperty(value = "号码")
    private String number;

    /**
     * 位置更新状态
     */
    @ApiModelProperty(value = "位置更新状态")
    private Integer locationUpdateStatus;

    /**
     * 失败原因
     */
    @ApiModelProperty(value = "失败原因")
    private String failCause;

    /**
     * IMSI码, 仅移动小号有该属性
     */
    @ApiModelProperty(value = "IMSI码, 仅移动小号有该属性")
    private String imsi;

    /**
     * GT码, 仅移动小号有该属性
     */
    @ApiModelProperty(value = "GT码, 仅移动小号有该属性")
    private String gtCode;

    /**
     * 号码状态
     */
    @ApiModelProperty(value = "号码状态")
    private Integer state;
}
