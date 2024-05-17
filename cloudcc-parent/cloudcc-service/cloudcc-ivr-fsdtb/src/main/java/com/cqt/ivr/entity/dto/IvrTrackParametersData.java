package com.cqt.ivr.entity.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 添加ivr随路数据供接口调用请求信息
 *
 */
@Data
@ApiModel("添加ivr随路数据供接口调用请求信息")
public class IvrTrackParametersData implements Serializable {

    private static final long serialVersionUID = -1L;

    @ApiModelProperty(value = "等待次数")
    private String busy_no;

    @ApiModelProperty(value = "主叫号码", example = "89555555588", required = true)
    private String real_caller;

    @ApiModelProperty(value = "被叫号码")
    private String cr_destination;

    @ApiModelProperty(value = "音库 1-粤语  2-英语  3-中文   默认普通话", example = "0")
    private String property;

    @ApiModelProperty(value = "主菜单按键", example = "0")
    private String mainMenuPushKey;

    @ApiModelProperty(value = "业务类型")
    private String ivrType;

    @ApiModelProperty(value = "客户类型  0:黑名单客户、1:正常客户、2:VIP客户、3:项目客户、4:重点客户(高贡献)、5:特殊客户(高敏感)、6:特殊客户(高风险)")
    private String customerType;

    @ApiModelProperty(value = "运单号")
    private String orderCode;

    @ApiModelProperty(value = "地区码")
    private String areaCode;

    @ApiModelProperty(value = "来电时间")
    private String in_ivr_time;

    @ApiModelProperty(value = "二级菜单按键", example = "0")
    private String secMenuPushKey;

    @ApiModelProperty(value = "新老客户  1:新客户    2：老客户")
    private String newCustomer;

    @ApiModelProperty(value = "通话唯一标识")
    private String uuid;

    @ApiModelProperty(value = "技能组id")
    private String sysQueueid;

    @ApiModelProperty(value = "技能组名称")
    private String queueName;

    @ApiModelProperty(value = "企业标识")
    private String company_code;

    @ApiModelProperty(value = "顺丰响应客户类型值")
    private String customerTypeResult;

    @ApiModelProperty(value = "是否澳门   香港0  澳门1")
    private String isMACO;
}