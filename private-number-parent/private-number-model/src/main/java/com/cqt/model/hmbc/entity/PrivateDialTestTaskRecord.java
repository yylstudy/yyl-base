package com.cqt.model.hmbc.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * 定时拨测任务记录
 *
 * @author jeecg-boot
 * @date 2022-07-07
 * @since V2.1.0
 */
@Data
@TableName("private_dial_test_task_record")
@Accessors(chain = true)
@ApiModel(value = "定时拨测任务记录", description = "定时拨测任务记录")
public class PrivateDialTestTaskRecord implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 分布式主键ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    @ApiModelProperty(value = "分布式主键ID")
    private String id;

    /**
     * 企业vccId
     */
    @ApiModelProperty(value = "企业vccId")
    private String vccId;

    /**
     * xxl-job任务ID
     */
    @ApiModelProperty(value = "xxl-job任务ID")
    private Integer jobId;

    /**
     * 拨测类型（1：隐私号拨测；2：位置更新）
     */
    @ApiModelProperty(value = "拨测类型（1：隐私号拨测；2：位置更新）")
    private Integer type;

    /**
     * 执行拨测任务的服务器IP（方便后续排查）
     */
    @ApiModelProperty(value = "执行拨测任务的服务器IP（方便后续排查）")
    private String serverIp;

    /**
     * 任务执行状态（0：进行中，1：已完成）
     */
    @ApiModelProperty(value = "任务执行状态（0：进行中，1：已完成）")
    private Integer state;

    /**
     * 要更新的号码数量
     */
    @ApiModelProperty(value = "要更新的号码数量")
    private Integer totalCount;

    /**
     * 更新成功的号码数量
     */
    @ApiModelProperty(value = "更新成功的号码数量")
    private Integer sucCount;

    /**
     * 更新失败号码数量
     */
    @ApiModelProperty(value = "更新失败号码数量")
    private Integer failCount;

    /**
     * 创建人
     */
    @TableField(fill = FieldFill.INSERT)
    @ApiModelProperty(value = "创建人")
    private String createBy;

    /**
     * 更新人
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    @ApiModelProperty(value = "更新人")
    private String updateBy;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    /**
     * 添加时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    @ApiModelProperty(value = "添加时间")
    private Date updateTime;

    /**
     * 备注，保留字段
     */
    @ApiModelProperty(value = "备注，保留字段")
    private String remark;
}
