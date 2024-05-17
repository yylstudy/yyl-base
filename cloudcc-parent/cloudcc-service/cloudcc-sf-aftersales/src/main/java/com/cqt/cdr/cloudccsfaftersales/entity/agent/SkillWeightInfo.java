package com.cqt.cdr.cloudccsfaftersales.entity.agent;

import com.cqt.model.agent.entity.AgentSkill;
import lombok.Data;

import java.io.Serializable;

/**
 * @author Xienx
 * @date 2023-07-11 16:37:16:37
 */
@Data
public class SkillWeightInfo implements Serializable {

    private static final long serialVersionUID = 7500359358742081584L;

    /**
     * 技能id
     */
    private String skillId;

    /**
     * 技能名称
     */
    private String skillName;

    /**
     * 技能包名称
     */
    private String skillPackName;

    /**
     * 技能包下的技能名称字符串数组
     */
    private String skillNames;

    /**
     * 技能包id
     */
    private String skillPackId;

    /**
     * 坐席权值
     */
    private Integer agentWeight;

    /**
     * 技能权值
     */
    private Integer skillWeight;

    public AgentSkill toAgentSkill(String sysAgentId) {
        AgentSkill agentSkill = new AgentSkill();
        agentSkill.setSysAgentId(sysAgentId);
        agentSkill.setSkillId(skillId);
        agentSkill.setSkillWeight(skillWeight);
        agentSkill.setAgentWeight(agentWeight);
        return agentSkill;
    }
}
