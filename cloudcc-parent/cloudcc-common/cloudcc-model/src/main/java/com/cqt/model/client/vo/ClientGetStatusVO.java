package com.cqt.model.client.vo;

import com.cqt.model.agent.dto.AgentStatusDTO;
import com.cqt.model.client.base.ClientResponseBaseVO;
import com.cqt.model.client.dto.ClientGetStatusDTO;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Set;

/**
 * @author linshiqiang
 * date:  2023-06-29 9:42
 * SDK 获取状态 响应
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ClientGetStatusVO extends ClientResponseBaseVO implements Serializable {

    private static final long serialVersionUID = 3113424155698909917L;

    /**
     * 坐席状态
     */
    private String status;

    /**
     * 坐席子状态
     */
    @JsonProperty("sub_status")
    private String subStatus;

    /**
     * 与坐席正在一起通话的号码列表
     */
    @JsonProperty("in_call_numbers")
    private Set<String> inCallNumbers;

    /**
     * 构建
     */
    public static ClientGetStatusVO build(ClientGetStatusDTO getStatusDTO,
                                          AgentStatusDTO agentStatusDTO,
                                          Set<String> inCallNumbers) {
        ClientGetStatusVO clientGetStatusVO = new ClientGetStatusVO();
        clientGetStatusVO.setReqId(getStatusDTO.getReqId());
        clientGetStatusVO.setMsgType(getStatusDTO.getMsgType());
        clientGetStatusVO.setCode("0");
        clientGetStatusVO.setMsg("获取成功!");
        clientGetStatusVO.setStatus(agentStatusDTO.getTargetStatus());
        clientGetStatusVO.setSubStatus(agentStatusDTO.getTargetSubStatus());
        clientGetStatusVO.setUuid(agentStatusDTO.getUuid());
        clientGetStatusVO.setCompanyCode(getStatusDTO.getCompanyCode());
        clientGetStatusVO.setInCallNumbers(inCallNumbers);
        return clientGetStatusVO;
    }
}
