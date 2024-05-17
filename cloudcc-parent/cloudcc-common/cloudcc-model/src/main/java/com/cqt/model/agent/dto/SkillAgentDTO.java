package com.cqt.model.agent.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @author linshiqiang
 * date:  2023-11-23 10:08
 */
@Data
public class SkillAgentDTO implements Serializable {

    private static final long serialVersionUID = -3817598277891006722L;

    @JsonProperty("company_code")
    private String companyCode;

    @JsonProperty("agent_id")
    private String agentId;

    @JsonProperty("skill_id")
    private String skillId;

    private String keyword;

    @JsonProperty("page_no")
    private Integer pageNo;

    @JsonProperty("page_size")
    private Integer pageSize;
}
