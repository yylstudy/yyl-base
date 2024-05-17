package com.cqt.ivr.entity.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 获取埋点信息请求实体类
 *
 */
@Data
@ApiModel("触发埋点信息请求参数")
public class BuryingPointReq implements Serializable {

    private static final long serialVersionUID = -1L;

    @ApiModelProperty(value = "业务类型")
    private String ivrType;

    @ApiModelProperty(value = "报表字段（ivr小类）")
    private String subIvrType;

    @ApiModelProperty(value = "人工还是自助  1人工  2 自助")
    private String callType;

    @ApiModelProperty(value = "话单uuid", required = true)
    private String uuid;

    @ApiModelProperty(value = "触发时间   非传参参数系统自生成")
    private String triggerTime;

    @ApiModelProperty(value = "年月   非传参参数系统自生成")
    private String month;

    @ApiModelProperty(value = "企业标识", required = true)
    private String company_code;

    @ApiModelProperty(value = "语言  1-粤语  2-英语  3-中文")
    private String language;

    @ApiModelProperty(value = "是否已发短信  0否 1是， 默认0")
    private String submissionCode;

    @ApiModelProperty(value = "报表字段业务明细类型")
    private String ivrDetailType;

}