package com.cqt.model.hmbc.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 定时拨测任务配置号码二级查询参数
 *
 * @author scott
 * @date 2022年07月08日 15:53
 */
@Data
@NoArgsConstructor
public class DialTestNumberQueryDTO implements Serializable {

    private static final long serialVersionUID = 767778903064199973L;

    /**
     * 定时拨测任务主键id
     */
    @ApiModelProperty(value = "定时拨测任务主键id", hidden = true)
    private String timingConfId;

    /**
     * 拨测的号码范围（0：全部号码, 1:指定号码）
     */
    @ApiModelProperty(value = "拨测的号码范围（0：全部号码, 1:指定号码）")
    private Integer numberRange;

    /**
     * 企业vccId
     */
    @ApiModelProperty(value = "企业vccId")
    private String vccId;

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
     * 归属供应商id
     */
    @ApiModelProperty(value = "归属供应商id")
    private String supplierId;

    /**
     * 所属地市编码
     */
    @ApiModelProperty(value = "所属地市编码")
    private String areaCode;

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

}
