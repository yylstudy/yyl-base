package com.cqt.monitor.web.distributor.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * @author linshiqiang
 * @since 2022/7/8 13:50
 * freeswitch 轮训组配置信息 distributor.conf.xml
 */
@ApiModel(value = "freeswitch 轮训组配置信息 distributor.conf.xml")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "private_number_ms.private_sip_distributor_info")
public class PrivateSipDistributorInfo implements Serializable {
    /**
     * 主键diaplan id
     */
    @TableId(value = "distributor_id", type = IdType.ASSIGN_ID)
    @ApiModelProperty(value = "主键distributor_id")
    private String distributorId;

    /**
     * diaplan名称
     */
    @TableField(value = "distributor_name")
    @ApiModelProperty(value = "diaplan名称")
    private String distributorName;

    /**
     * 当前状态 1已加载, 0未加载
     */
    @TableField(value = "distributor_status")
    @ApiModelProperty(value = "当前状态 1已加载, 0未加载")
    private Integer distributorStatus;

    /**
     * 机房名称
     */
    @TableField(value = "room_name")
    @ApiModelProperty(value = "机房名称")
    private String roomName;

    /**
     * 机房id
     */
    @TableField(value = "room_id")
    @ApiModelProperty(value = "机房id")
    private String roomId;

    /**
     * 设备id
     */
    @TableField(value = "device_id")
    @ApiModelProperty(value = "设备id")
    private String deviceId;

    /**
     * 设备IP
     */
    @TableField(value = "device_ip")
    @ApiModelProperty(value = "设备IP")
    private String deviceIp;

    /**
     * 设备名称
     */
    @TableField(value = "device_name")
    @ApiModelProperty(value = "设备名称")
    private String deviceName;

    /**
     * 设备名称
     */
    @TableField(value = "device_type")
    @ApiModelProperty(value = "设备名称")
    private String deviceType;


    /**
     * 文件在freeswitch的目录
     */
    @TableField(value = "file_path")
    @ApiModelProperty(value = "文件在freeswitch的目录")
    private String filePath;

    /**
     * md5
     */
    @TableField(value = "md5")
    @ApiModelProperty(value = "md5")
    private String md5;

    /**
     * distributor xml文件内容
     */
    @TableField(value = "distributor_content")
    @ApiModelProperty(value = "distributor xml文件内容")
    private String distributorContent;

    /**
     * 创建人
     */
    @TableField(value = "create_by")
    @ApiModelProperty(value = "创建人")
    private String createBy;

    /**
     * 创建日期
     */
    @TableField(value = "create_time")
    @ApiModelProperty(value = "创建日期")
    private Date createTime;

    /**
     * 更新人
     */
    @TableField(value = "update_by")
    @ApiModelProperty(value = "更新人")
    private String updateBy;

    /**
     * 更新日期
     */
    @TableField(value = "update_time")
    @ApiModelProperty(value = "更新日期")
    private Date updateTime;

    /**
     * 逻辑删除  默认0  删除 1
     */
    @TableField(value = "delete_flag")
    private Integer deleteFlag;

    private static final long serialVersionUID = 1L;
}
