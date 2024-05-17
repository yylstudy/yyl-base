package com.cqt.model.hmbc.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * 定时拨测任务详单
 *
 * @author jeecg-boot
 * @date 2022-07-07
 * @since V2.1.0
 */
@Data
@TableName("private_dial_test_task_record_details")
@Accessors(chain = true)
@ApiModel(value = "定时拨测任务详单", description = "定时拨测任务详单")
public class PrivateDialTestTaskRecordDetails implements Serializable {
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
     * 任务记录ID（private_dial_test_task_record表的主键）
     */
    @ApiModelProperty(value = "任务记录ID（private_dial_test_task_record表的主键）")
    private String recordId;

    /**
     * 拨测的号码
     */
    @ApiModelProperty(value = "拨测的号码")
    private String number;

    /**
     * 拨测的IMSI码
     */
    @ApiModelProperty(value = "拨测的IMSI码")
    private String imsi;

    /**
     * 拨测所在的GT码（仅位置更新时有该字段，方便后续排查）
     */
    @ApiModelProperty(value = "拨测所在的GT码（仅位置更新时有该字段，方便后续排查）")
    private String gtCode;

    /**
     * 拨测状态
     */
    @ApiModelProperty(value = "拨测状态")
    private Integer state;

    /**
     * 拨测执行时间
     */
    @ApiModelProperty(value = "拨测执行时间")
    private Date executionTime;

    /**
     * 失败原因
     */
    @ApiModelProperty(value = "失败原因")
    private String failCause;

    /**
     * 创建人
     */
    @TableField(fill = FieldFill.INSERT)
    @ApiModelProperty(value = "创建人")
    private String createBy;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    /**
     * 备注，保留字段
     */
    @ApiModelProperty(value = "备注，保留字段")
    private String remark;
}
