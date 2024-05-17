package com.cqt.model.agent.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author 86180
 */
@ApiModel(value = "技能下坐席列表返回VO")
@Data
public class SkillAgentVO implements Serializable {

    private static final long serialVersionUID = -2656299650799848598L;

    @ApiModelProperty(value = "技能名称")
    @JsonProperty("skill_name")
    private String skillName;

    @ApiModelProperty(value = "技能id")
    @JsonProperty("skill_id")
    private String skillId;

    @ApiModelProperty(value = "坐席列表")
    @JsonProperty("agents")
    private AgentData agents;

    @Data
    public static class AgentData implements Serializable {

        private static final long serialVersionUID = 7810639363294654216L;

        private List<AgentListVO> list;

        @JsonProperty("total_count")
        private Long totalCount;

        @JsonProperty("total_page")
        private Long totalPage;

        @JsonProperty("current_page")
        private Integer currentPage;

    }

    @Data
    public static class AgentListVO implements Serializable {

        private static final long serialVersionUID = -6818502868187629601L;

        @JsonProperty("agent_id")
        private String sysAgentId;

        @JsonProperty("agent_status")
        private String agentStatus;

        @JsonProperty("agent_name")
        private String agentName;

    }
}
