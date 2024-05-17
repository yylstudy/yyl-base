package com.cqt.model.agent.bo;

import cn.hutool.core.util.StrUtil;
import com.cqt.base.enums.CallRoleEnum;
import com.cqt.base.enums.agent.AgentCallingSubStatusEnum;
import com.cqt.base.enums.agent.AgentStatusEnum;
import com.cqt.base.enums.agent.AgentStatusTransferActionEnum;
import com.cqt.model.agent.dto.AgentStatusDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author linshiqiang
 * date:  2023-07-26 16:13
 * 坐席状态迁移业务实体
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AgentStatusTransferBO implements Serializable {

    private static final long serialVersionUID = 7568657163283427669L;

    /**
     * 企业id
     */
    private String companyCode;

    /**
     * 坐席id
     */
    private String agentId;

    /**
     * 分机id
     */
    private String extId;

    /**
     * 终端
     */
    private String os;

    /**
     * 通话时的uuid
     */
    private String uuid;

    /**
     * 事件发生时间戳
     */
    private Long eventTimestamp;

    /**
     * 坐席呼叫时的角色
     */
    private CallRoleEnum callRoleEnum;

    /**
     * 迁移动作
     */
    private AgentStatusTransferActionEnum transferAction;

    /**
     * 目标状态
     */
    private AgentStatusEnum targetStatus;

    /**
     * 目标子状态
     */
    private AgentCallingSubStatusEnum targetSubStatus;

    /**
     * 桥接事件
     */
    @Deprecated
    private Boolean bridgeFlag;

    /**
     * 呼入坐席未接, 转忙碌
     */
    private Boolean agentNoAnswerMakerBusy;

    /**
     * 呼入转坐席响铃, 客户先挂断
     */
    private Boolean callInAgentClientHangUpFirst;

    /**
     * 坐席外呼失败,
     */
    private Boolean agentCallOutFail;

    /**
     * 在坐席挂断事件, 判断坐席是否有桥接事件-当userQueueUpDTO不为空时
     */
    private Boolean checkAgentBridgeStatus;

    private String reason;

    /**
     * 事后处理对象
     */
    public static AgentStatusTransferBO newArrangeBuild(AgentStatusTransferBO agentStatusTransferBO,
                                                        AgentStatusDTO agentStatusDTO) {
        AgentStatusTransferBO transferBO = new AgentStatusTransferBO();
        transferBO.setTransferAction(AgentStatusTransferActionEnum.RECOVER);
        transferBO.setAgentId(agentStatusTransferBO.getAgentId());
        transferBO.setEventTimestamp(System.currentTimeMillis());
        transferBO.setCompanyCode(agentStatusTransferBO.getCompanyCode());
        if (StrUtil.isEmpty(agentStatusDTO.getBeforeCallStartAgentStatus())) {
            transferBO.setTargetStatus(AgentStatusEnum.BUSY);
        } else {
            transferBO.setTargetStatus(AgentStatusEnum.valueOf(agentStatusDTO.getBeforeCallStartAgentStatus()));
        }
        transferBO.setCallRoleEnum(agentStatusTransferBO.getCallRoleEnum());
        return transferBO;
    }
}
