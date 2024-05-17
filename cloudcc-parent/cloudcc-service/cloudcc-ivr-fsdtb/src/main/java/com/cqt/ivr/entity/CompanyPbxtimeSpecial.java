package com.cqt.ivr.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author ld
 * @since 2023-07-24
 */
@Getter
@Setter
@TableName("tx_company_pbxtime_special")
@NoArgsConstructor
public class CompanyPbxtimeSpecial implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;

    @ApiModelProperty("日程ID")
    private String timeId;

    @ApiModelProperty("起始日期")
    private String startDate;

    @ApiModelProperty("结束日期")
    private String endDate;

    @ApiModelProperty("开始时间")
    private String startTime;

    @ApiModelProperty("结束时间")
    private String endTime;

    @ApiModelProperty("类型1：白名单2：黑名单")
    private String type;

    private String createBy;

    private String updateBy;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    @ApiModelProperty("备注")
    private String remark;

}
