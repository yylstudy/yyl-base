package com.cqt.model.agent.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * @author Xienx
 * @date 2023-07-10 17:48:17:48
 */
@Data
@TableName("cloudcc_agent_skill_package")
public class AgentSkillPackage implements Serializable {

    private static final long serialVersionUID = 5048048876373410128L;

    /**
     * 主键id
     */
    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    /**
     * 技能包id
     */
    private String skillPackId;

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

}
