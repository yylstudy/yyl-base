package com.cqt.model.client.vo;

import cn.hutool.core.util.IdUtil;
import com.cqt.base.enums.MsgTypeEnum;
import com.cqt.model.agent.dto.AgentStatusDTO;
import com.cqt.model.client.base.ClientResponseBaseVO;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Set;

/**
 * @author linshiqiang
 * date:  2023-06-29 9:42
 * SDK 坐席状态变化 响应
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ClientAgentStatusChangeVO extends ClientResponseBaseVO implements Serializable {

    private static final long serialVersionUID = 3113424155698909917L;

    /**
     * 坐席id
     */
    @JsonProperty("agent_id")
    private String agentId;

    /**
     * 分机id
     */
    @JsonProperty("ext_id")
    private String extId;

    /**
     * 切换前的状态
     */
    @JsonProperty("last_agent_status")
    private String lastAgentStatus;

    /**
     * 是 切换后的状态
     */
    @JsonProperty("agent_status")
    private String agentStatus;

    /**
     * 切换后坐席子状态
     */
    @JsonProperty("sub_agent_status")
    private String subAgentStatus;

    /**
     * 状态切换原因
     */
    private String reason;

    /**
     * 终端
     */
    private String os;

    /**
     * 通话uuid
     */
    private String uuid;

    /**
     * 与坐席正在一起通话的号码列表
     */
    @JsonProperty("in_call_numbers")
    private Set<String> inCallNumbers;

    /**
     * 构建对象
     */
    public static ClientAgentStatusChangeVO build(AgentStatusDTO agentStatusDTO) {
        ClientAgentStatusChangeVO changeVO = new ClientAgentStatusChangeVO();
        changeVO.setCode("0");
        changeVO.setMsg("success");
        changeVO.setReqId(IdUtil.fastUUID());
        changeVO.setMsgType(MsgTypeEnum.change_status.name());
        changeVO.setCompanyCode(agentStatusDTO.getCompanyCode());
        changeVO.setAgentId(agentStatusDTO.getAgentId());
        changeVO.setExtId(agentStatusDTO.getExtId());
        changeVO.setAgentStatus(agentStatusDTO.getTargetStatus());
        changeVO.setSubAgentStatus(agentStatusDTO.getTargetSubStatus());
        changeVO.setLastAgentStatus(agentStatusDTO.getSourceStatus());
        changeVO.setOs(agentStatusDTO.getOs());
        changeVO.setReason(agentStatusDTO.getReason());
        changeVO.setUuid(agentStatusDTO.getUuid());
        return changeVO;
    }
}
