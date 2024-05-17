package com.cqt.monitor.web.callevent.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Data
@ApiModel
public class PriavteWarnVO implements Serializable {

    private static final long serialVersionUID = -4752422163092102373L;


    @ApiModelProperty(value = "id")
    private String id;

    @ApiModelProperty(value = "VCCID")
    private String vccId;

    @ApiModelProperty(value = "企业名称")
    private String vccName;

    @ApiModelProperty(value = "告警名称")
    private String warnName;

    /**
     *  nj:南京
     *  yz：扬州
     */
    @ApiModelProperty(value = "所属平台")
    private String platForm;

    /**
     * 0:企业
     * 1：企业号码
     */
    @ApiModelProperty(value = "生效颗粒度")
    private Integer granularity;

    @ApiModelProperty(value = "生效城市组")
    private String cityGroup;

    @ApiModelProperty(value = "生效开始时间")
    private String startTime;

    @ApiModelProperty(value = "生效结束时间")
    private String endTime;

    /**
     * 0:钉钉告警
     * 1：邮件告警
     */
    @ApiModelProperty(value = "告警方式")
    private String warningWay;

    @ApiModelProperty(value = "邮件收件人")
    private String emailReceiver;

    @ApiModelProperty(value = "告警内容")
    private String warningIncotent;

    private List<PrivateWarningRule> warnRuleList;

    /**
     * 0：是
     * 1：否
     */
    @ApiModelProperty(value = "是否进行应急处理")
    private Integer isHandle;

    /**
     * 0：所有绑定关系请求切换异地机房  1：所有话务切换异地机房
     */
    @ApiModelProperty(value = "应急处理")
    private Integer handleWay;

    @ApiModelProperty(value = "创建人")
    private String createBy;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    @ApiModelProperty(value = "更新时间")
    private Date updateTime;

    @ApiModelProperty(value = "更新人")
    private String updateBy;
}
