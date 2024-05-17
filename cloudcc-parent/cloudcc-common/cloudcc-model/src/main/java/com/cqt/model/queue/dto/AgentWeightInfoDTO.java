package com.cqt.model.queue.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.Map;

/**
 * @author linshiqiang
 * date:  2023-07-19 10:10
 * 坐席的权值信息
 * <p>
 * {
 * agent: [
 * {
 * skillId: weight
 * }
 * ]
 * skill: [
 * {
 * skillId: weight
 * }
 * ]
 * }
 */
@Data
public class AgentWeightInfoDTO implements Serializable {

    private static final long serialVersionUID = 3310492878508453638L;

    /**
     * 坐席的技能权值
     * key: skillId
     * value: weight
     */
    private Map<String, Integer> agent;

    /**
     * 坐席的技能权值
     * key: skillId
     * value: weight
     */
    private Map<String, Integer> skill;
}
