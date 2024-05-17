package com.cqt.model.agent.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * @author Xienx
 * date 2023-07-10 17:41:17:41
 */
@Data
@TableName("cloudcc_agent_skill")
public class AgentSkill implements Serializable {

    private static final long serialVersionUID = 7076629337117038175L;

    /**
     * 主键id
     */
    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    /**
     * 技能id
     */
    private String skillId;

    /**
     * 系统坐席id
     */
    private String sysAgentId;

    /**
     * 坐席权值
     */
    private Integer agentWeight;

    /**
     * 技能权值
     */
    private Integer skillWeight;

    /**
     * 逻辑字段 坐席工号
     */
    @TableField(exist = false)
    private String agentId;
}
