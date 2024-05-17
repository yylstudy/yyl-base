package com.cqt.ivr.entity.dto;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;
import java.io.Serializable;

/**
 *
 * @author ld
 * @since 2023-07-24
 */
@Getter
@Setter
@TableName("tx_company_pbxtime_daily")
public class CompanyPbxtimeDailyDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("日程ID")
    @NotEmpty(message = "timeId不为空")
    private String timeId;

    @ApiModelProperty("日期")
    @NotEmpty(message = "date不为空")
    @Pattern(regexp = "^[0-9]{4}-(((0[13578]|(10|12))-(0[1-9]|[1-2][0-9]|3[0-1]))|(02-(0[1-9]|[1-2][0-9]))|((0[469]|11)-(0[1-9]|[1-2][0-9]|30)))$",message = "date时间格式为yyyy-MM-dd")
    private String date;

    @ApiModelProperty("时间")
    @NotEmpty(message = "time不为空")
    @Pattern(regexp = "^(([0-1]?[0-9])|([2][0-3])):([0-5]?[0-9])(:([0-5]?[0-9]))$",message = "time时间格式为HH:mm:ss")
    private String time;

}
