package com.cqt.model.hmbc.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * GT码表
 *
 * @author jeecg-boot
 * @date 2022-05-25
 * V1.0
 */
@Data
@TableName("vlr_gt_info")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@ApiModel(value = "vlr_gt_info对象", description = "GT码表")
public class VlrGtInfo implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(type = IdType.ASSIGN_ID)
    @ApiModelProperty(value = "主键")
    private String id;

    /**
     * 创建人
     */
    @ApiModelProperty(value = "创建人")
    private String createBy;

    /**
     * 创建日期
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "创建日期")
    private Date createTime;

    /**
     * 更新人
     */
    @ApiModelProperty(value = "更新人")
    private String updateBy;

    /**
     * 更新日期
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "更新日期")
    private Date updateTime;

    /**
     * 环境
     */
    @ApiModelProperty(value = "环境")
    private String sourceName;

    /**
     * GT码
     */
    @ApiModelProperty(value = "GT码")
    private String gtCode;
    /**
     * 备注
     */
    @ApiModelProperty(value = "备注")
    private String remark;
    /**
     * MapUrl
     */
    @ApiModelProperty(value = "MapUrl")
    private String mapUrl;

}
