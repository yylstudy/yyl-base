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
@TableName("tx_company_pbxtime_daily")
public class CompanyPbxtimeDaily implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;

    @ApiModelProperty("日程ID")
    private String timeId;

    @ApiModelProperty("起始时间")
    private String startTime;

    @ApiModelProperty("结束时间")
    private String endTime;

    @ApiModelProperty("1到7分别代表周一到周日")
    private String weekDay;

    private String createBy;

    private String updateBy;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
