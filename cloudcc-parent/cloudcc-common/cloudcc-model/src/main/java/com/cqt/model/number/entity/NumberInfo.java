package com.cqt.model.number.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @author linshiqiang
 * date:  2023-07-21 9:28
 * 号码信息
 */
@Data
@TableName("cloudcc_number_info")
public class NumberInfo implements Serializable {

    private static final long serialVersionUID = -3767213741600302187L;

    /**
     * 企业号码
     */
    @TableId(type = IdType.INPUT)
    private String number;

    /**
     * 计费号码
     */
    private String chargeNumber;

    /**
     * 外显号码
     */
    private String displayNumber;

    /**
     * 号码状态 启用-1, 禁用-0
     */
    private Integer status;

    /**
     * 企业id
     */
    @TableField("tenant_id")
    @JsonProperty("tenantId")
    private String companyCode;

    /**
     * ivr服务id/人工服务id
     *
     * @since 7.0.0
     */
    private String serviceId;

    /**
     * 服务方式 1-IVR服务、2-人工服务
     *
     * @since 7.0.0
     */
    private Integer serviceWay;
}
