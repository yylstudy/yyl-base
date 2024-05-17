package com.cqt.model.hmbc.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * 定时拨测任务配置
 *
 * @author jeecg-boot
 * @date 2022-07-07
 * @since V2.1.0
 */
@Data
@TableName("private_dial_test_timing_conf")
@Accessors(chain = true)
@ApiModel(value = "定时拨测任务配置", description = "定时拨测任务配置")
public class PrivateDialTestTimingConf implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 分布式主键id
     */
    @TableId(type = IdType.ASSIGN_ID)
    @ApiModelProperty(value = "分布式主键id")
    private String id;

    /**
     * 企业vccId
     */
    @ApiModelProperty(value = "企业vccId")
    private String vccId;

    /**
     * XXL-JOB返回的任务ID
     */
    @ApiModelProperty(value = "XXL-JOB返回的任务ID")
    private Integer jobId;

    /**
     * 拨测类型（1：隐私号拨测；2：位置更新）
     */
    @ApiModelProperty(value = "拨测类型（1：隐私号拨测；2：位置更新）")
    private Integer type;

    /**
     * 定时执行时间配置（CRON表达式）
     */
    @ApiModelProperty(value = "定时执行时间配置（CRON表达式）")
    private String cronConf;

    /**
     * 拨测的号码范围（0：全部号码, 1:指定号码）
     */
    @ApiModelProperty(value = "拨测的号码范围（0：全部号码, 1:指定号码）")
    private Integer numberRange;

    /**
     * 任务状态（0：暂停，1：正常）
     */
    @ApiModelProperty(value = "任务状态（0：暂停，1：正常）")
    private Integer state;

    /**
     * 企业拨测结果推送URL
     */
    @ApiModelProperty(value = "企业拨测结果推送URL")
    private String pushUrl;

    /**
     * 企业拨测结果推送类型（-1：不推送, 0：仅推送异常结果，1：推送全部结果）
     */
    @ApiModelProperty(value = "-1：不推送, 企业拨测结果推送类型（0：仅推送异常结果，1：推送全部结果）")
    private Integer pushType;

    /**
     * 创建人
     */
    @ApiModelProperty(value = "创建人")
    private String createBy;

    /**
     * 创建时间
     */
    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    /**
     * 更新人
     */
    @ApiModelProperty(value = "更新人")
    private String updateBy;

    /**
     * 添加时间
     */
    @ApiModelProperty(value = "添加时间")
    private Date updateTime;

    /**
     * 备注，保留字段
     */
    @ApiModelProperty(value = "备注，保留字段")
    private String remark;
}
