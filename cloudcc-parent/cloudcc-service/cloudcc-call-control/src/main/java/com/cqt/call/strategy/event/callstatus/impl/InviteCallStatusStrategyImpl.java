package com.cqt.call.strategy.event.callstatus.impl;

import com.cqt.base.enums.CallInChannelEnum;
import com.cqt.base.enums.CallRoleEnum;
import com.cqt.base.enums.CallStatusEventEnum;
import com.cqt.base.enums.CallTypeEnum;
import com.cqt.base.enums.agent.AgentStatusTransferActionEnum;
import com.cqt.base.enums.ext.ExtStatusEnum;
import com.cqt.base.enums.ext.ExtStatusTransferActionEnum;
import com.cqt.call.service.DataQueryService;
import com.cqt.call.service.DataStoreService;
import com.cqt.call.strategy.event.callstatus.CallStatusStrategy;
import com.cqt.cloudcc.manager.service.CommonDataOperateService;
import com.cqt.model.agent.bo.AgentStatusTransferBO;
import com.cqt.model.agent.vo.CallUuidContext;
import com.cqt.model.agent.vo.CallUuidRelationDTO;
import com.cqt.model.cdr.dto.CallCdrDTO;
import com.cqt.model.client.dto.ClientOutboundCallTaskDTO;
import com.cqt.model.client.vo.ClientCallInVO;
import com.cqt.model.ext.dto.ExtStatusDTO;
import com.cqt.model.freeswitch.dto.event.CallStatusEventDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * @author linshiqiang
 * date:  2023-07-03 13:37
 * 外呼事件
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InviteCallStatusStrategyImpl extends AbstractCallStatus implements CallStatusStrategy {

    private final CommonDataOperateService commonDataOperateService;

    private final DataQueryService dataQueryService;

    private final DataStoreService dataStoreService;

    private final ObjectMapper objectMapper;

    private final ApplicationContext applicationContext;

    @Override
    public void deal(CallStatusEventDTO callStatusEventDTO) throws Exception {
        String companyCode = callStatusEventDTO.getCompanyCode();
        String uuid = callStatusEventDTO.getUuid();
        CallUuidContext callUuidContext = dataQueryService.getCallUuidContext(companyCode, uuid);
        if (Objects.isNull(callUuidContext)) {
            log.info("[外呼事件] 企业id: {}, uuid: {}, 未查询到上下文信息", companyCode, uuid);
            return;
        }
        if (log.isDebugEnabled()) {
            log.debug("[外呼事件] 当前uuid上下文信息: {}", objectMapper.writeValueAsString(callUuidContext));
        }
        // 开始录制处理
        startRecord(getCallStatus(), callUuidContext);

        // 保存通话uuid上下文
        saveContext(callStatusEventDTO, callUuidContext);

        CallUuidRelationDTO current = callUuidContext.getCurrent();
        String extId = current.getExtId();
        String agentId = current.getAgentId();
        current.setServerId(callStatusEventDTO.getServerId());

        if (CallRoleEnum.CALLIN_TRANSFER_AGENT.equals(current.getCallRoleEnum())) {
            String mainCallId = callUuidContext.getMainCallId();
            dataStoreService.saveCdrLink(companyCode, mainCallId, callUuidContext.getCurrent().getRelationUuid(), uuid);
        }

        // 客户接通, 只需更新当前事件的时间callUuidRelationDTO
        CallTypeEnum callTypeEnum = current.getCallTypeEnum();
        if (CallTypeEnum.CLIENT.equals(callTypeEnum)) {
            return;
        }

        // 通知SDK 来电呼入坐席
        if (Boolean.TRUE.equals(callUuidContext.getCurrent().getCallInFlag())) {
            ClientCallInVO clientCallInVO = ClientCallInVO.buildClientCallInVO(callStatusEventDTO, callUuidContext);
            dataStoreService.notifyClient(clientCallInVO);
            if (log.isInfoEnabled()) {
                log.info("[外呼事件] 来电, 呼入给坐席, 通知SDK: {}", objectMapper.writeValueAsString(clientCallInVO));
            }
        }
        if (CallInChannelEnum.PREDICT_OUT_CALL.equals(callUuidContext.getCallInChannel())) {
            ClientCallInVO clientCallInVO = ClientCallInVO.buildClientPredictCallInVO(callStatusEventDTO, callUuidContext);
            dataStoreService.notifyClient(clientCallInVO);
            if (log.isInfoEnabled()) {
                log.info("[外呼事件] 预测外呼, 呼入给坐席, 通知SDK: {}", objectMapper.writeValueAsString(clientCallInVO));
            }
        }

        if (Boolean.TRUE.equals(callUuidContext.getCurrent().getOutCallTaskFlag())) {
            ClientOutboundCallTaskDTO callTaskDTO = callUuidContext.getClientOutboundCallTaskDTO();
            dataStoreService.setCallTimestamp(callTaskDTO.getTaskId(), callTaskDTO.getMember());
        }

        /*
         * 调用SDK-Interface
         *   1. 坐席状态迁移 空闲(上一次状态) -> 外呼中
         *   2. 保存坐席实时状态
         *   3. 通知前端SDK
         */
        AgentStatusTransferBO agentStatusTransferBO = AgentStatusTransferBO.builder()
                .transferAction(AgentStatusTransferActionEnum.CALLOUT)
                .agentId(agentId)
                .companyCode(companyCode)
                .callRoleEnum(current.getCallRoleEnum())
                .extId(extId)
                .os(callUuidContext.getCurrent().getOs())
                .uuid(uuid)
                .eventTimestamp(callStatusEventDTO.getTimestamp())
                .build();
        // 坐席状态逻辑由SDK-Interface实现
        boolean transfer = dataStoreService.agentStatusChangeTransfer(agentStatusTransferBO);
        log.info("[外呼事件] 调用SDK-Interface 坐席id: {}, 坐席状态迁移结果: {}", agentId, transfer);

        // 查分机状态
        ExtStatusDTO extStatusDTO = dataQueryService.getActualExtStatus(companyCode, extId);
        String targetStatus = extStatusDTO.getTargetStatus();
        // 当前是振铃, 如果target是通话中, 丢弃修改
        boolean passTransfer = ExtStatusEnum.CALLING.name().equals(targetStatus)
                || ExtStatusEnum.RINGING.name().equals(targetStatus);
        if (passTransfer) {
            log.info("[外呼事件] 分机当前状态: {}, 是通话中或振铃中, 已跳过外呼状态, 状态迁移不处理!!!", targetStatus);
            return;
        }

        // 分机状态
        extStatusTransfer(callStatusEventDTO, callUuidContext);
    }

    private void saveContext(CallStatusEventDTO callStatusEventDTO,
                             CallUuidContext callUuidContext) {
        CallCdrDTO callCdrDTO = callUuidContext.getCurrent().getCallCdrDTO();
        callCdrDTO.setInviteFlag(true);
        callCdrDTO.setInviteTimestamp(callStatusEventDTO.getTimestamp());
        dataStoreService.saveCallUuidContext(callUuidContext);
    }

    @Override
    public CallStatusEventEnum getCallStatus() {
        return CallStatusEventEnum.INVITE;
    }

    @Override
    public ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    @Override
    public CommonDataOperateService getCommonDataOperateService() {
        return commonDataOperateService;
    }

    @Override
    public ExtStatusTransferActionEnum getExtStatusTransferActionEnum() {
        return ExtStatusTransferActionEnum.INVITE;
    }

    @Override
    public ExtStatusEnum getExtStatusEnum() {
        return ExtStatusEnum.INVITING;
    }
}
