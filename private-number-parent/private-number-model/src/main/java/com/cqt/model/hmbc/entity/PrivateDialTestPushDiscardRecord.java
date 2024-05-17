package com.cqt.model.hmbc.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * 定时拨测任务结果推送失败记录
 *
 * @author jeecg-boot
 * @date 2022-07-07
 * @since V2.1.0
 */
@Data
@TableName("private_dial_test_push_discard_record")
@Accessors(chain = true)
@ApiModel(value = "定时拨测任务结果推送失败记录", description = "定时拨测任务结果推送失败记录")
public class PrivateDialTestPushDiscardRecord implements Serializable {

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
     * 号码
     */
    @ApiModelProperty(value = "号码")
    private String number;

    /**
     * 拨测类型（1：隐私号拨测；2：位置更新）
     */
    @ApiModelProperty(value = "拨测类型（1：隐私号拨测；2：位置更新）")
    private Integer type;

    /**
     * 企业拨测结果推送接口地址
     */
    @ApiModelProperty(value = "企业拨测结果推送接口地址")
    private String pushUrl;

    /**
     * 企业拨测推送JSON
     */
    @ApiModelProperty(value = "企业拨测推送JSON")
    private String pushJson;

    /**
     * 拨测结果推送失败原因
     */
    @ApiModelProperty(value = "拨测结果推送失败原因")
    private String reason;

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
