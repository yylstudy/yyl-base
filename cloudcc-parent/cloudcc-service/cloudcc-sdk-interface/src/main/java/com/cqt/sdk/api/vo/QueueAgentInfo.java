package com.cqt.sdk.api.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 监控实体类
 * @author 86180
 */
@Data
public class QueueAgentInfo implements Serializable {

    private static final long serialVersionUID = -1L;

    /**
     * 坐席名称
     */
    private String agentName;

    /**
     * 坐席状态
     */
    private String agentStatus;

}
