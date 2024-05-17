package com.cqt.model.client.vo;

import com.cqt.model.agent.dto.AgentStatusDTO;
import com.cqt.model.client.base.ClientResponseBaseVO;
import com.cqt.model.client.dto.ClientChangeStatusDTO;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * @author linshiqiang
 * date:  2023-06-29 9:42
 * SDK 切换状态（包括强制）响应
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ClientChangeStatusVO extends ClientResponseBaseVO implements Serializable {

    private static final long serialVersionUID = 3113424155698909917L;

    /**
     * 状态切换动作
     */
    private String action;

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

    private String os;

    /**
     * 响应reply
     */
    public static ClientChangeStatusVO response(ClientChangeStatusDTO clientChangeStatusDTO,
                                                String code,
                                                String msg,
                                                Boolean reply) {
        ClientChangeStatusVO clientChangeStatusVO = new ClientChangeStatusVO();
        clientChangeStatusVO.setMsg(msg);
        clientChangeStatusVO.setCode(code);
        clientChangeStatusVO.setReply(reply);
        clientChangeStatusVO.setAction(clientChangeStatusDTO.getAction());
        clientChangeStatusVO.setMsgType(clientChangeStatusDTO.getMsgType());
        clientChangeStatusVO.setCompanyCode(clientChangeStatusDTO.getCompanyCode());
        clientChangeStatusVO.setReqId(clientChangeStatusDTO.getReqId());
        clientChangeStatusVO.setOs(clientChangeStatusVO.getOs());
        return clientChangeStatusVO;
    }

    /**
     * 响应 默认reply
     */
    public static ClientChangeStatusVO response(ClientChangeStatusDTO clientChangeStatusDTO,
                                                String code,
                                                String msg) {
        ClientChangeStatusVO clientChangeStatusVO = new ClientChangeStatusVO();
        clientChangeStatusVO.setMsg(msg);
        clientChangeStatusVO.setCode(code);
        clientChangeStatusVO.setReply(true);
        clientChangeStatusVO.setAction(clientChangeStatusDTO.getAction());
        clientChangeStatusVO.setMsgType(clientChangeStatusDTO.getMsgType());
        clientChangeStatusVO.setCompanyCode(clientChangeStatusDTO.getCompanyCode());
        clientChangeStatusVO.setReqId(clientChangeStatusDTO.getReqId());
        clientChangeStatusVO.setOs(clientChangeStatusVO.getOs());
        return clientChangeStatusVO;
    }

    /**
     * 响应 默认reply
     */
    public static ClientChangeStatusVO response(ClientChangeStatusDTO clientChangeStatusDTO,
                                                AgentStatusDTO agentStatusDTO,
                                                String code,
                                                String msg) {
        ClientChangeStatusVO clientChangeStatusVO = new ClientChangeStatusVO();
        clientChangeStatusVO.setMsg(msg);
        clientChangeStatusVO.setCode(code);
        clientChangeStatusVO.setReply(true);
        clientChangeStatusVO.setAction(clientChangeStatusDTO.getAction());
        clientChangeStatusVO.setMsgType(clientChangeStatusDTO.getMsgType());
        clientChangeStatusVO.setCompanyCode(clientChangeStatusDTO.getCompanyCode());
        clientChangeStatusVO.setReqId(clientChangeStatusDTO.getReqId());
        clientChangeStatusVO.setLastAgentStatus(agentStatusDTO.getSourceStatus());
        clientChangeStatusVO.setAgentStatus(agentStatusDTO.getTargetStatus());
        clientChangeStatusVO.setOs(agentStatusDTO.getOs());
        return clientChangeStatusVO;
    }
}
