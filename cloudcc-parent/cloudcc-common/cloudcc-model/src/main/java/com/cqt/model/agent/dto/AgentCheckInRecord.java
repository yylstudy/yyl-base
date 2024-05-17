package com.cqt.model.agent.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @author Xienx
 * @date 2023-07-24 11:29:11:29
 */
@Data
public class AgentCheckInRecord implements Serializable {

    private static final long serialVersionUID = 5881585123525633729L;

    /**
     * 坐席id
     */
    private String agentId;

    /**
     * 用户设备ip
     */
    private List<String> clientIps;

    /**
     * 用户设备类型
     */
    private String clientType;

    /**
     * 签入时间戳
     */
    private Date ts;
}
