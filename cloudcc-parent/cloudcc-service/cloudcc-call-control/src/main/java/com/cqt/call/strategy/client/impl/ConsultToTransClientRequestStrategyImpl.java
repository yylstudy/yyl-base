package com.cqt.call.strategy.client.impl;

import com.cqt.base.enums.*;
import com.cqt.base.enums.agent.AgentStatusEnum;
import com.cqt.base.enums.cdr.HangupCauseEnum;
import com.cqt.call.strategy.client.ClientRequestStrategy;
import com.cqt.cloudcc.manager.service.CommonDataOperateService;
import com.cqt.cloudcc.manager.service.FreeswitchRequestService;
import com.cqt.model.agent.dto.AgentStatusDTO;
import com.cqt.model.agent.vo.CallUuidContext;
import com.cqt.model.cdr.dto.CallCdrDTO;
import com.cqt.model.client.base.ClientResponseBaseVO;
import com.cqt.model.client.dto.ClientConsultToTransDTO;
import com.cqt.model.freeswitch.dto.api.HangupDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;

/**
 * @author linshiqiang
 * date:  2023-07-03 14:42
 * 咨询过程中, 被咨询方未接, 直接转接给被咨询方
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ConsultToTransClientRequestStrategyImpl extends AbstractClientChecker implements ClientRequestStrategy {

    private final CommonDataOperateService commonDataOperateService;

    private final FreeswitchRequestService freeswitchRequestService;

    @Override
    public MsgTypeEnum getMsgType() {
        return MsgTypeEnum.consult_to_trans;
    }

    @Override
    public ClientResponseBaseVO deal(String requestBody) throws Exception {
        ClientConsultToTransDTO consultToTransDTO = convert(requestBody, ClientConsultToTransDTO.class);
        String companyCode = consultToTransDTO.getCompanyCode();
        String consultUuid = consultToTransDTO.getConsultUuid();
        String agentId = consultToTransDTO.getAgentId();
        CallUuidContext callUuidContext = commonDataOperateService.getCallUuidContext(companyCode, consultUuid);
        if (Objects.isNull(callUuidContext)) {
            return ClientResponseBaseVO.response(consultToTransDTO, "1", "被咨询方uuid不存在!");
        }
        Optional<AgentStatusDTO> agentStatusOptional = commonDataOperateService.getActualAgentStatus(companyCode, agentId);
        if (!agentStatusOptional.isPresent()) {
            return ClientResponseBaseVO.response(consultToTransDTO, "1", "坐席id不存在!");
        }
        AgentStatusDTO agentStatusDTO = agentStatusOptional.get();
        if (!isCalling(agentStatusDTO)) {
            return ClientResponseBaseVO.response(consultToTransDTO, "1", "坐席非通话状态!");
        }
        // 是否已接通
        boolean isAnswer = isAnswer(callUuidContext);
        if (isAnswer) {
            // 桥接
            boolean bridge = toTransfer(callUuidContext, agentStatusDTO);
            if (bridge) {
                return ClientResponseBaseVO.response(consultToTransDTO, "0", "咨询中转接成功!");
            }
            return ClientResponseBaseVO.response(consultToTransDTO, "0", "咨询中转接失败!");
        }

        boolean updateContext = updateContext(callUuidContext, agentStatusDTO);
        if (updateContext) {
            return ClientResponseBaseVO.response(consultToTransDTO, "0", "咨询中转接成功!");
        }
        return ClientResponseBaseVO.response(consultToTransDTO, "1", "咨询已执行, 转接失败!");
    }

    private boolean isCalling(AgentStatusDTO agentStatusDTO) {
        return AgentStatusEnum.CALLING.name().equals(agentStatusDTO.getTargetStatus());
    }

    private boolean toTransfer(CallUuidContext consultContext, AgentStatusDTO agentStatusDTO) {
        String consultUuid = consultContext.getUUID();
        String companyCode = agentStatusDTO.getCompanyCode();
        String uuid = agentStatusDTO.getUuid();
        CallUuidContext agentContext = commonDataOperateService.getCallUuidContext(companyCode, uuid);
        String clientUuid = agentContext.findRelationUUID();
        CallUuidContext clientContext = commonDataOperateService.getCallUuidContext(companyCode, clientUuid);
        clientContext.getCurrent().setRelationUuid(consultUuid);
        commonDataOperateService.saveCallUuidContext(clientContext);

        consultContext.getCurrent().setRelationUuid(clientUuid);
        // consultContext.getCurrent().setXferActionEnum(XferActionEnum.TRANS);
        CallTypeEnum callTypeEnum = consultContext.getCallTypeEnum();
        if (CallTypeEnum.CLIENT.equals(callTypeEnum)) {
            consultContext.getCurrent().setCallRoleEnum(CallRoleEnum.CONSULT_TO_TRANS_CLIENT);
        } else {
            consultContext.getCurrent().setCallRoleEnum(CallRoleEnum.CONSULT_TO_TRANS_AGENT);
        }
        consultContext.getCurrent().setCallInChannel(CallInChannelEnum.CONSULT_TO_TRANS);
        consultContext.getCurrent().setHangupAll(null);
        consultContext.fillRelationUuidSet(clientUuid);
        commonDataOperateService.saveCallUuidContext(consultContext);

        // 挂断坐席
        agentContext.getCurrent().setHangupAll(false);
        commonDataOperateService.saveCallUuidContext(agentContext);

        HangupDTO hangupDTO = HangupDTO.build(companyCode, uuid, HangupCauseEnum.CONSULT_TO_TRANS);
        freeswitchRequestService.hangup(hangupDTO);

        return true;
    }

    private boolean updateContext(CallUuidContext consultContext, AgentStatusDTO agentStatusDTO) {
        Boolean flag = commonDataOperateService.saveConsultFlag(consultContext.getUUID());
        if (!flag) {
            return false;
        }
        // lock  在xfer时 和此时, 看谁先lock
        consultContext.getCurrent().setXferActionEnum(XferActionEnum.TRANS);
        CallTypeEnum callTypeEnum = consultContext.getCallTypeEnum();
        if (CallTypeEnum.CLIENT.equals(callTypeEnum)) {
            consultContext.getCurrent().setCallRoleEnum(CallRoleEnum.TRANS_CLIENT);
        } else {
            consultContext.getCurrent().setCallRoleEnum(CallRoleEnum.TRANS_AGENT);
        }
        consultContext.getCurrent().setCallInChannel(CallInChannelEnum.TRANS);

        // 发起操作坐席
        String companyCode = agentStatusDTO.getCompanyCode();
        String uuid = agentStatusDTO.getUuid();
        CallUuidContext agentContext = commonDataOperateService.getCallUuidContext(companyCode, uuid);
        if (Objects.nonNull(agentContext)) {
            agentContext.getCurrent().setTransHangup(true);
            commonDataOperateService.saveCallUuidContext(agentContext);

            String clientUuid = agentContext.findRelationUUID();
            consultContext.getCurrent().setRelationUuid(clientUuid);

            CallUuidContext clientContext = commonDataOperateService.getCallUuidContext(companyCode, clientUuid);
            clientContext.getCurrent().setRelationUuid(consultContext.getUUID());
            commonDataOperateService.saveCallUuidContext(clientContext);
        }

        commonDataOperateService.saveCallUuidContext(consultContext);
        return true;
    }

    private boolean isAnswer(CallUuidContext callUuidContext) {
        CallCdrDTO callCdrDTO = callUuidContext.getCallCdrDTO();
        return Boolean.TRUE.equals(callCdrDTO.getAnswerFlag());
    }
}
