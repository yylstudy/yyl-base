package com.cqt.model.agent.dto;

import com.cqt.base.enums.XferActionEnum;
import com.cqt.base.enums.agent.AgentStatusTransferActionEnum;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.io.Serializable;

/**
 * @author linshiqiang
 * date:  2023-07-04 9:23
 * 坐席实时状态
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AgentStatusDTO implements Serializable {

    private static final long serialVersionUID = -3055750275227449614L;

    /**
     * 企业id
     */
    private String companyCode;

    /**
     * 坐席id
     */
    private String agentId;

    /**
     * 坐席ip
     */
    private String agentIp;

    /**
     * 源 坐席状态
     */
    private String sourceStatus;

    /**
     * 源 坐席状子态
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
     *
     * @see AgentStatusTransferActionEnum
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
     * 坐席切换原因
     */
    private String reason;

    /**
     * 当前通话的uuid
     */
    private String uuid;

    /**
     * 当前关联的分机id
     */
    private String extId;

    /**
     * 当前关联的分机ip
     */
    private String extIp;

    /**
     * 最近迁入时间
     */
    private Long checkinTime;

    /**
     * 最近迁出时间
     */
    private Long checkoutTime;

    /**
     * 通话开始时间
     */
    private Long callStartTime;

    /**
     * 终端
     */
    private String os;

    /**
     * 否(小休时长单位：分) 小休时间
     */
    private Integer restMin;

    /**
     * xfer动作枚举
     * 【consult:咨询，trans:转接，three_way:三方通话，whisper:耳语，eavesdrop:监听】
     */
    private XferActionEnum xferActionEnum;

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
     * 通话结束后小休时长
     */
    private Integer afterCallStopRestMin;

    /**
     * 通话之前的状态(外呼或呼入之前的状态)
     * 未进行状态设置且关闭事后处理，结束通话后进入原先的状态
     *
     * @see com.cqt.base.enums.agent.AgentStatusEnum
     */
    private String beforeCallStartAgentStatus;

    private Integer serviceMode;

    /**
     * 构建对象
     */
    public AgentStatusDTO build(AgentStatusTransferDTO agentStatusTransferDTO) {
        this.setSourceStatus(agentStatusTransferDTO.getSourceStatus());
        this.setSourceSubStatus(agentStatusTransferDTO.getSourceSubStatus());
        this.setSourceTimestamp(agentStatusTransferDTO.getSourceTimestamp());
        this.setSourceDuration(agentStatusTransferDTO.getSourceDuration());
        this.setTransferAction(agentStatusTransferDTO.getTransferAction());
        this.setTargetStatus(agentStatusTransferDTO.getTargetStatus());
        this.setTargetSubStatus(agentStatusTransferDTO.getTargetSubStatus());
        this.setTargetTimestamp(agentStatusTransferDTO.getTargetTimestamp());
        this.setTargetDuration(agentStatusTransferDTO.getTargetDuration());
        this.setReason(agentStatusTransferDTO.getReason());
        return this;
    }

    /**
     * 一些字段设置为null
     */
    public void setNull() {
        setUuid(null);
        setXferActionEnum(null);
        setAfterCallStopAction(null);
        setAfterCallStopAction(null);
        setBeforeCallStartAgentStatus(null);
        setTargetSubStatus(null);
        setReason(null);
        setRestMin(null);
    }

    /**
     * null
     */
    public void hangupSetNull() {
        setAfterCallStopAction(null);
        setAfterCallStopAgentStatus(null);
        setBeforeCallStartAgentStatus(null);
        setAfterCallStopRestMin(null);
        setUuid(null);
    }

    /**
     * 移除无用字段
     */
    public void removeUselessField() {
        setRestMin(null);
        setXferActionEnum(null);
        setAfterCallStopAction(null);
        setAfterCallStopAgentStatus(null);
        setBeforeCallStartAgentStatus(null);
        setAfterCallStopRestMin(null);
        setReason(null);
        setCompanyCode(null);
        setTransferAction(null);
    }

    public static AgentStatusDTO copy(AgentStatusDTO agentStatusDTO) {
        if (agentStatusDTO == null) {
            return null;
        }

        AgentStatusDTO agentStatusDTO1 = new AgentStatusDTO();

        agentStatusDTO1.setCompanyCode(agentStatusDTO.getCompanyCode());
        agentStatusDTO1.setAgentId(agentStatusDTO.getAgentId());
        agentStatusDTO1.setAgentIp(agentStatusDTO.getAgentIp());
        agentStatusDTO1.setSourceStatus(agentStatusDTO.getSourceStatus());
        agentStatusDTO1.setSourceSubStatus(agentStatusDTO.getSourceSubStatus());
        agentStatusDTO1.setSourceDuration(agentStatusDTO.getSourceDuration());
        agentStatusDTO1.setSourceTimestamp(agentStatusDTO.getSourceTimestamp());
        agentStatusDTO1.setTransferAction(agentStatusDTO.getTransferAction());
        agentStatusDTO1.setTargetStatus(agentStatusDTO.getTargetStatus());
        agentStatusDTO1.setTargetSubStatus(agentStatusDTO.getTargetSubStatus());
        agentStatusDTO1.setTargetDuration(agentStatusDTO.getTargetDuration());
        agentStatusDTO1.setTargetTimestamp(agentStatusDTO.getTargetTimestamp());
        agentStatusDTO1.setReason(agentStatusDTO.getReason());
        agentStatusDTO1.setUuid(agentStatusDTO.getUuid());
        agentStatusDTO1.setExtId(agentStatusDTO.getExtId());
        agentStatusDTO1.setExtIp(agentStatusDTO.getExtIp());
        agentStatusDTO1.setCheckinTime(agentStatusDTO.getCheckinTime());
        agentStatusDTO1.setCheckoutTime(agentStatusDTO.getCheckoutTime());
        agentStatusDTO1.setCallStartTime(agentStatusDTO.getCallStartTime());
        agentStatusDTO1.setOs(agentStatusDTO.getOs());
        agentStatusDTO1.setRestMin(agentStatusDTO.getRestMin());
        agentStatusDTO1.setXferActionEnum(agentStatusDTO.getXferActionEnum());
        agentStatusDTO1.setAfterCallStopAgentStatus(agentStatusDTO.getAfterCallStopAgentStatus());
        agentStatusDTO1.setAfterCallStopAction(agentStatusDTO.getAfterCallStopAction());
        agentStatusDTO1.setAfterCallStopRestMin(agentStatusDTO.getAfterCallStopRestMin());
        agentStatusDTO1.setBeforeCallStartAgentStatus(agentStatusDTO.getBeforeCallStartAgentStatus());

        return agentStatusDTO1;
    }
}
