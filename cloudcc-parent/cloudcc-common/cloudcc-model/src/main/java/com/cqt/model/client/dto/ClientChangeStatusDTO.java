package com.cqt.model.client.dto;

import com.cqt.model.client.base.ClientRequestBaseDTO;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotEmpty;
import java.io.Serializable;

/**
 * @author linshiqiang
 * date:  2023-07-05 14:28
 * SDK 切换状态（包括强制）参数
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ClientChangeStatusDTO extends ClientRequestBaseDTO implements Serializable {

    /**
     * 状态切换动作
     *
     * @see com.cqt.base.enums.agent.AgentStatusTransferActionEnum
     */
    @NotEmpty(message = "[action]不能为空!")
    private String action;

    /**
     * 否(小休时长单位：分) 小休时间
     */
    @JsonProperty("rest_min")
    private Integer restMin;

    /**
     * 是 被操作的分机ID
     */
    @JsonProperty("operated_ext_id")
    private String operatedExtId;

    /**
     * 是 被操作的分机ID
     */
    @JsonProperty("operated_agent_id")
    private String operatedAgentId;

}
