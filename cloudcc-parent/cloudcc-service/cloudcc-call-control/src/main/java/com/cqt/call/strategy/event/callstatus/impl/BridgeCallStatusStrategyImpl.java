package com.cqt.call.strategy.event.callstatus.impl;

import com.cqt.base.enums.CallInChannelEnum;
import com.cqt.base.enums.CallStatusEventEnum;
import com.cqt.base.enums.CallTypeEnum;
import com.cqt.base.enums.CallbackActionEnum;
import com.cqt.base.enums.cdr.HangupCauseEnum;
import com.cqt.base.enums.ext.ExtStatusEnum;
import com.cqt.base.enums.ext.ExtStatusTransferActionEnum;
import com.cqt.call.service.DataStoreService;
import com.cqt.call.strategy.event.callstatus.CallStatusStrategy;
import com.cqt.cloudcc.manager.service.CommonDataOperateService;
import com.cqt.cloudcc.manager.service.FreeswitchRequestService;
import com.cqt.model.agent.vo.CallUuidContext;
import com.cqt.model.agent.vo.CallUuidRelationDTO;
import com.cqt.model.cdr.dto.CallCdrDTO;
import com.cqt.model.client.vo.ClientCallbackVO;
import com.cqt.model.freeswitch.dto.api.HangupDTO;
import com.cqt.model.freeswitch.dto.api.StopPlayDTO;
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
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BridgeCallStatusStrategyImpl extends AbstractCallStatus implements CallStatusStrategy {

    private final FreeswitchRequestService freeswitchRequestService;

    private final CommonDataOperateService commonDataOperateService;

    private final ObjectMapper objectMapper;

    private final ApplicationContext applicationContext;

    private final DataStoreService dataStoreService;

    @Override
    public void deal(CallStatusEventDTO callStatusEventDTO) throws Exception {
        String companyCode = callStatusEventDTO.getCompanyCode();
        String uuid = callStatusEventDTO.getUuid();
        CallUuidContext callUuidContext = commonDataOperateService.getCallUuidContext(companyCode, uuid);
        if (Objects.isNull(callUuidContext)) {
            log.info("[桥接事件] 企业id: {}, uuid: {}, 未查询到上下文信息", companyCode, uuid);
            return;
        }
        if (log.isDebugEnabled()) {
            log.debug("[桥接事件] 当前uuid上下文信息: {}", objectMapper.writeValueAsString(callUuidContext));
        }

        CallUuidRelationDTO current = callUuidContext.getCurrent();

        // 咨询中转接 通知坐席
        CallInChannelEnum callInChannel = callUuidContext.getCallInChannel();
        if (CallInChannelEnum.CONSULT_TO_TRANS.equals(callInChannel)) {
            dataStoreService.notifyClient(ClientCallbackVO.buildStart(callUuidContext,
                    CallbackActionEnum.CONSULT_TO_TRANS, "咨询中转接接通客户成功!"));
            log.info("[桥接事件] 企业: {}, 咨询中转接接通", companyCode);
        }

        // 挂断原坐席-转接
        if (Boolean.TRUE.equals(current.getTransHangup())) {
            log.info("[桥接事件] 转接 挂断原坐席: {}", uuid);
            HangupDTO hangupDTO = HangupDTO.build(companyCode, uuid, HangupCauseEnum.CANCEL_CONSULT_TRANS_AGENT_HANGUP);
            try {
                log.info("[桥接事件] 企业: {}, 挂断发起转接坐席: {}", companyCode, uuid);
                freeswitchRequestService.hangup(hangupDTO);
            } catch (Exception e) {
                log.error("[桥接事件] 挂断发起转接坐席失败: ", e);
            }
        }
        // 保存通话uuid上下文
        fillCallCdr(callStatusEventDTO, callUuidContext);

        // 客户没有桥接事件
        CallTypeEnum callTypeEnum = current.getCallTypeEnum();
        if (CallTypeEnum.CLIENT.equals(callTypeEnum)) {

            // 预览外呼-停止客户侧放音
            stopPlayback(callUuidContext);
            return;
        }

        // 分机状态
        extStatusTransfer(callStatusEventDTO, callUuidContext);
    }

    private void stopPlayback(CallUuidContext callUuidContext) {
        if (Objects.nonNull(callUuidContext.getClientOutboundCallTaskDTO())) {
            StopPlayDTO stopPlayDTO = StopPlayDTO.build(callUuidContext.getCompanyCode(), callUuidContext.getUUID());
            freeswitchRequestService.stopPlay(stopPlayDTO);
        }
    }

    private void fillCallCdr(CallStatusEventDTO callStatusEventDTO, CallUuidContext callUuidContext) {
        CallCdrDTO callCdrDTO = callUuidContext.getCallCdrDTO();
        callCdrDTO.setBridgeFlag(true);
        callCdrDTO.setBridgeTimestamp(callStatusEventDTO.getTimestamp());
        commonDataOperateService.saveCallUuidContext(callUuidContext);
    }

    @Override
    public CallStatusEventEnum getCallStatus() {
        return CallStatusEventEnum.BRIDGE;
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
        return ExtStatusTransferActionEnum.BRIDGE;
    }

    @Override
    public ExtStatusEnum getExtStatusEnum() {
        return ExtStatusEnum.CALLING;
    }
}
