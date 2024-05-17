package com.cqt.model.agent.dto;

import com.cqt.base.enums.CallRoleEnum;
import com.cqt.model.client.vo.ClientCallInVO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author linshiqiang
 * date:  2023-07-04 13:36
 * 坐席状态迁移
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AgentStatusTransferDTO implements Serializable {

    private static final long serialVersionUID = -173586938144006935L;

    /**
     * 当前通话的uuid
     */
    private String uuid;

    /**
     * 企业id
     */
    private String companyCode;

    /**
     * 坐席id
     */
    private String agentId;

    /**
     * 当前关联的分机id
     */
    private String extId;

    /**
     * 终端
     */
    private String os;

    /**
     * 源 坐席状态
     */
    private String sourceStatus;

    /**
     * 源 坐席子状态
     */
    private String sourceSubStatus;

    /**
     * 源 状态持续时间s
     */
    private Integer sourceDuration;

    /**
     * 源 坐席状态时间戳
     */
    private Long sourceTimestamp;

    /**
     * 坐席状态迁移动作类型
     */
    private String transferAction;

    /**
     * 目标 坐席状态
     */
    private String targetStatus;

    /**
     * 目标 坐席子状态
     */
    private String targetSubStatus;

    /**
     * 目标 状态持续时间s
     */
    private Integer targetDuration;

    /**
     * 目标 坐席状态时间戳
     */
    private Long targetTimestamp;

    /**
     * 通话角色
     *
     * @see CallRoleEnum
     */
    private String callRole;

    /**
     * 结束通话后坐席进入什么状态
     *
     * @see com.cqt.base.enums.agent.AgentStatusEnum
     */
    private String afterCallStopAgentStatus;

    /**
     * 结束通话后坐席 迁移动作
     */
    private String afterCallStopAction;

    /**
     * 通话之前的状态(外呼或呼入之前的状态)
     * 未进行状态设置且关闭事后处理，结束通话后进入原先的状态
     *
     * @see com.cqt.base.enums.agent.AgentStatusEnum
     */
    private String beforeCallStartAgentStatus;

    /**
     * 在坐席挂断事件, 判断坐席是否有桥接事件-当userQueueUpDTO不为空时
     */
    private Boolean checkAgentBridgeStatus;

    /**
     * 是否有桥接事件
     */
    private Boolean bridgeFlag;

    /**
     * 呼入给前端的响应
     */
    private ClientCallInVO clientCallInVO;

    private String reason;

}
