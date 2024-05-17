package com.cqt.ivr.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 *
 * @author ld
 * @since 2023-07-24
 */
@Getter
@Setter
@TableName("tx_company_pbxtimes")
public class CompanyPbxtimes implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;

    @ApiModelProperty("日程名称")
    private String name;

    @ApiModelProperty("起始时间")
    private String startDate;

    @ApiModelProperty("结束时间")
    private String endDate;

    @ApiModelProperty("所属企业")
    private String tenantId;

    private String createBy;

    private String updateBy;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
