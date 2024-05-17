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
 * 定时拨测任务号码配置
 *
 * @author jeecg-boot
 * @date 2022-07-07
 * @since V2.1.0
 */
@Data
@TableName("private_dial_test_number_conf")
@Accessors(chain = true)
@ApiModel(value = "定时拨测任务号码配置", description = "定时拨测任务号码配置")
public class PrivateDialTestNumberConf implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 分布式主键ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    @ApiModelProperty(value = "分布式主键ID")
    private String id;

    /**
     * 企业定时拨测的ID
     */
    @ApiModelProperty(value = "企业定时拨测的ID")
    private String timingConfId;

    /**
     * X号码
     */
    @ApiModelProperty(value = "X号码")
    private String number;

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
     * 备注，保留字段
     */
    @ApiModelProperty(value = "备注，保留字段")
    private String remark;
}
