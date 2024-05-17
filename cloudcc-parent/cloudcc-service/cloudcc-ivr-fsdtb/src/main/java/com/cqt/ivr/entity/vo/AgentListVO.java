package com.cqt.ivr.entity.vo;

import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.io.Serializable;

/**
 * @author 86180
 */
@ApiModel(value = "坐席列表返回VO")
@Data
public class AgentListVO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String sysAgentId;

    private String agentStatus;

    private String agentName;
}
