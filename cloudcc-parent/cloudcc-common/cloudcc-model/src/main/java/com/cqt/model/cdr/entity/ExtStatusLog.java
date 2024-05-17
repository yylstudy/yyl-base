package com.cqt.model.cdr.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 话机状态变迁记录(CcExtStatusLog)表实体类
 *
 * @author linshiqiang
 * @since 2023-07-04 11:25:26
 */
@Data
@TableName(value = "cloudcc_ext_status_log")
public class ExtStatusLog implements Serializable {

    private static final long serialVersionUID = -6952884878306698799L;

    /**
     * 变更主键id
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long logId;

    /**
     * uuid
     */
    @TableField("uuid")
    private String uuid;

    /**
     * 企业id
     */
    @TableField("company_code")
    private String companyCode;

    /**
     * 分机id
     */
    @TableField("ext_id")
    private String extId;

    /**
     * 分机ip
     */
    @TableField("ext_ip")
    private String extIp;

    /**
     * 源 分机状态
     */
    @TableField("source_status")
    private String sourceStatus;

    /**
     * 源 分机状态时间戳
     */
    @TableField("source_timestamp")
    private Long sourceTimestamp;

    /**
     * 状态迁移动作类型
     */
    @TableField("transfer_action")
    private String transferAction;

    /**
     * 目标 分机状态
     */
    @TableField("target_status")
    private String targetStatus;

    /**
     * 目标 分机状态时间戳
     */
    @TableField("target_timestamp")
    private Long targetTimestamp;

    /**
     * 状态变更原因
     */
    @TableField("reason")
    private String reason;

}

