package com.cqt.model.cdr.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * 坐席状态变更日志表(CcAgentStatusLog100000202307)表实体类
 *
 * @author linshiqiang
 * @since 2023-07-07 18:24:51
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName("cloudcc_agent_status_log")
public class AgentStatusLog {

    /**
     * 变更主键id
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long logId;

    /**
     * 主叫侧uuid
     */
    private String uuid;

    /**
     * 企业id
     */
    @TableField("company_code")
    private String companyCode;

    /**
     * 坐席工号
     */
    @TableField("agent_id")
    private String agentId;

    /**
     * 分机id
     */
    @TableField("ext_id")
    private String extId;

    /**
     * 上一次状态
     */
    @TableField("source_status")
    private String sourceStatus;

    /**
     * 上一次状态的时间戳
     */
    @TableField("source_timestamp")
    private Long sourceTimestamp;

    /**
     * 上一次子状态
     */
    @TableField("source_sub_status")
    private String sourceSubStatus;

    /**
     * 上一次状态持续时间
     */
    @TableField("source_duration")
    private Integer sourceDuration;

    /**
     * 状态变更操作类型
     */
    @TableField("transfer_action")
    private String transferAction;

    /**
     * 变更后的当前状态
     */
    @TableField("target_status")
    private String targetStatus;

    /**
     * 变更后的当前子状态
     */
    @TableField("target_sub_status")
    private String targetSubStatus;

    /**
     * 变更后的当前状态设置持续时间
     */
    @TableField("target_duration")
    private Integer targetDuration;

    /**
     * 当前状态时间戳
     */
    @TableField("target_timestamp")
    private Long targetTimestamp;

    /**
     * 状态变更原因
     */
    private String reason;

}

