package com.cqt.call.strategy.event.callstatus.impl;

import cn.hutool.core.util.StrUtil;
import com.cqt.base.contants.CommonConstant;
import com.cqt.base.enums.*;
import com.cqt.base.enums.agent.AgentCallingSubStatusEnum;
import com.cqt.base.enums.agent.AgentStatusEnum;
import com.cqt.base.enums.agent.AgentStatusTransferActionEnum;
import com.cqt.base.enums.ext.ExtStatusEnum;
import com.cqt.base.enums.ext.ExtStatusTransferActionEnum;
import com.cqt.call.event.incalls.InCallsNumberEvent;
import com.cqt.call.event.stats.CallStatsEvent;
import com.cqt.call.service.DataStoreService;
import com.cqt.call.strategy.event.answeraction.AfterAnswerActionStrategyFactory;
import com.cqt.call.strategy.event.callstatus.CallStatusStrategy;
import com.cqt.cloudcc.manager.service.CommonDataOperateService;
import com.cqt.cloudcc.manager.service.FreeswitchRequestService;
import com.cqt.model.agent.bo.AgentStatusTransferBO;
import com.cqt.model.agent.vo.CallUuidContext;
import com.cqt.model.client.vo.ClientCallbackVO;
import com.cqt.model.company.entity.CompanyInfo;
import com.cqt.model.freeswitch.dto.api.PlaybackDTO;
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
 * 接通事件
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AnswerCallStatusStrategyImpl extends AbstractCallStatus implements CallStatusStrategy {

    private final CommonDataOperateService commonDataOperateService;

    private final DataStoreService dataStoreService;

    private final ObjectMapper objectMapper;

    private final ApplicationContext applicationContext;

    private final FreeswitchRequestService freeswitchRequestService;

    private final AfterAnswerActionStrategyFactory afterAnswerActionStrategyFactory;

    @Override
    public void deal(CallStatusEventDTO callStatusEventDTO) throws Exception {
        String companyCode = callStatusEventDTO.getCompanyCode();
        String uuid = callStatusEventDTO.getUuid();
        CallUuidContext callUuidContext = commonDataOperateService.getCallUuidContext(companyCode, uuid);
        if (Objects.isNull(callUuidContext)) {
            log.info("[接通事件] 企业id: {}, uuid: {}, 未查询到上下文信息", companyCode, uuid);
            return;
        }
        if (log.isDebugEnabled()) {
            log.debug("[接通事件] 当前uuid上下文信息: {}", objectMapper.writeValueAsString(callUuidContext));
        }

        // 开始录制处理
        startRecord(getCallStatus(), callUuidContext);

        // 通话中的号码-新增
        addInCallsNumber(callUuidContext);

        // 接通回调
        answerCallback(callUuidContext);

        // 来电转坐席
        if (CallRoleEnum.CALLIN_TRANSFER_AGENT.equals(callUuidContext.getCallRoleEnum())
                || CallRoleEnum.CALLIN_TRANSFER_OFFLINE_AGENT_PHONE.equals(callUuidContext.getCallRoleEnum())) {
            callinTransferAgentAnswer(callStatusEventDTO, callUuidContext);
        }

        // 客户接通, 只需更新当前事件的时间callUuidRelationDTO
        CallTypeEnum callTypeEnum = callUuidContext.getCallTypeEnum();
        saveContext(callStatusEventDTO, callUuidContext);
        if (CallTypeEnum.CLIENT.equals(callTypeEnum)) {
            // 咨询、转接、三方通话、耳语、监听 xfer等接通事件
            afterAnswerActionStrategyFactory.execute(callStatusEventDTO, callUuidContext);
            return;
        }

        String extId = callUuidContext.getExtId();
        String agentId = callUuidContext.getAgentId();

        // 坐席通话次数累加
        publishCallStatsEvent(callStatusEventDTO, callUuidContext);

        // 分机状态
        extStatusTransfer(callStatusEventDTO, callUuidContext);

        // 子状态
        AgentCallingSubStatusEnum callingSubStatusEnum = dealCallingSubStatus(callUuidContext);

        // 调用SDK-Interface
        //  1. 坐席状态迁移 振铃中->通话中
        //  2. 保存坐席实时状态
        //  3. 通知前端SDK
        AgentStatusTransferBO agentStatusTransferBO = AgentStatusTransferBO.builder()
                .transferAction(AgentStatusTransferActionEnum.ANSWER)
                .targetStatus(AgentStatusEnum.CALLING)
                .targetSubStatus(callingSubStatusEnum)
                .agentId(agentId)
                .companyCode(companyCode)
                .callRoleEnum(callUuidContext.getCallRoleEnum())
                .extId(extId)
                .os(callUuidContext.getOs())
                .uuid(uuid)
                .eventTimestamp(callStatusEventDTO.getTimestamp())
                .reason(Objects.nonNull(callUuidContext.getXferAction()) ? callUuidContext.getXferAction().name() : "")
                .build();
        // 坐席状态逻辑由SDK-Interface实现
        boolean transfer = dataStoreService.agentStatusChangeTransfer(agentStatusTransferBO);
        log.info("[接通事件] 调用SDK-Interface 坐席id: {}, 坐席状态迁移结果: {}", agentId, transfer);

        afterAnswerActionStrategyFactory.execute(callStatusEventDTO, callUuidContext);
    }

    /**
     * 坐席通话次数累加
     *
     * @param callStatusEventDTO 通话状态事件
     * @param callUuidContext    uuid上下文
     */
    private void publishCallStatsEvent(CallStatusEventDTO callStatusEventDTO, CallUuidContext callUuidContext) {
        String agentId = callUuidContext.getAgentId();
        applicationContext.publishEvent(new CallStatsEvent(this, agentId, callStatusEventDTO, callUuidContext));
    }

    /**
     * 通话中的号码-新增
     *
     * @param callUuidContext uuid上下文
     */
    private void addInCallsNumber(CallUuidContext callUuidContext) {
        applicationContext.publishEvent(new InCallsNumberEvent(callUuidContext.getCompanyCode(),
                callUuidContext.getMainCallId(),
                callUuidContext.getUUID(),
                callUuidContext.getNumber(),
                OperateTypeEnum.INSERT));
    }

    /**
     * ivr呼入 分配坐席, 坐席接通后处理
     *
     * @param callStatusEventDTO 事件消息
     * @param callUuidContext    转接坐席uuid上下文
     */
    private void callinTransferAgentAnswer(CallStatusEventDTO callStatusEventDTO, CallUuidContext callUuidContext) {
        String companyCode = callStatusEventDTO.getCompanyCode();
        String clientUUID = callUuidContext.getClientUUID();
        CallUuidContext clientContext = commonDataOperateService.getCallUuidContext(companyCode, clientUUID);
        if (Objects.nonNull(callUuidContext.getUserQueueUpDTO())) {
            clientContext.getUserQueueUpDTO().setSuccessTimestamp(callStatusEventDTO.getTimestamp());
        }
        StopPlayDTO stopPlayDTO = StopPlayDTO.build(companyCode, clientUUID);
        freeswitchRequestService.stopPlay(stopPlayDTO);
        commonDataOperateService.saveCallUuidContext(clientContext);
        log.info("[接通事件] 企业: {}, 来电排队成功, 坐席接通, 停止放等待音开始录音", companyCode);

        callInAgentAnswerPlayback(callStatusEventDTO);
    }

    /**
     * 坐席接收来电应答的提示音
     * 客户呼入-转坐席, 坐席接通,给坐席放音
     *
     * @since 7.0.0
     */
    public void callInAgentAnswerPlayback(CallStatusEventDTO callStatusEventDTO) {
        String companyCode = callStatusEventDTO.getCompanyCode();
        CompanyInfo companyInfo = commonDataOperateService.getCompanyInfoDTO(companyCode);
        if (companyInfo == null) {
            return;
        }
        Integer callInAgentAnswerPlaybackSwitch = companyInfo.getCallInAgentAnswerPlaybackSwitch();
        String uuid = callStatusEventDTO.getUuid();
        if (CommonConstant.ENABLE_Y.equals(callInAgentAnswerPlaybackSwitch)) {
            String defaultTone = commonDataOperateService.getDefaultTone(DefaultToneEnum.CALL_IN_AGENT_ANSWER_PLAYBACK);
            log.info("[接通事件] 企业: {}, 坐席接收来电应答的提示音: {}", companyCode, defaultTone);
            if (StrUtil.isEmpty(defaultTone)) {
                return;
            }
            PlaybackDTO playbackDTO = PlaybackDTO.build(companyCode, uuid, defaultTone, 1);
            freeswitchRequestService.playback(playbackDTO);
        }
    }

    /**
     * 处理通话子状态
     */
    private AgentCallingSubStatusEnum dealCallingSubStatus(CallUuidContext callUuidContext) {
        XferActionEnum xferActionEnum = callUuidContext.getXferAction();
        if (Objects.nonNull(xferActionEnum)) {
            // 三方通话, 咨询x, 转接
            // 耳语, 监听,
            try {
                switch (xferActionEnum) {
                    case WHISPER:
                    case EAVESDROP:
                    case CONSULT:
                    case THREE_WAY:
                    case FORCE_CALL:
                    case TRANS:
                        return AgentCallingSubStatusEnum.valueOf(xferActionEnum.name());
                    default:
                        return null;
                }
            } catch (IllegalArgumentException e) {
                log.error("[接通事件] xferActionEnum: {}, dealCallingSubStatus error:  ", xferActionEnum, e);
            }
        }
        if (OriginateAfterActionEnum.PLAY_RECORD.equals(callUuidContext.getOriginateAfterAction())) {
            return AgentCallingSubStatusEnum.PLAY_RECORD;
        }
        return null;
    }

    private void saveContext(CallStatusEventDTO callStatusEventDTO,
                             CallUuidContext callUuidContext) {
        callUuidContext.fillCallCdrDTO(getCallStatus(), callStatusEventDTO.getTimestamp());
        commonDataOperateService.saveCallUuidContext(callUuidContext);
    }

    /**
     * 接通回调通知
     *
     * @param callUuidContext uuid上下文
     */
    private void answerCallback(CallUuidContext callUuidContext) {
        String companyCode = callUuidContext.getCompanyCode();
        String xferUUID = callUuidContext.getXferUUID();
        CallRoleEnum callRoleEnum = callUuidContext.getCallRoleEnum();
        if (StrUtil.isNotEmpty(xferUUID)) {
            callOutPeerAnswerPlayback(companyCode, xferUUID);

            CallUuidContext xferContext = commonDataOperateService.getCallUuidContext(callUuidContext.getCompanyCode(), xferUUID);
            log.info("[接通事件] xfer 企业: {}, 坐席角色: {}, 对方已接通,回调通知SDK", companyCode, callRoleEnum);
            switch (callRoleEnum) {
                case TRANS_CLIENT:
                case TRANS_AGENT:
                    dataStoreService.notifyClient(ClientCallbackVO.buildStart(xferContext, CallbackActionEnum.TRANS));
                    return;
                case CONSULT_CLIENT:
                case CONSULT_AGENT:
                    dataStoreService.notifyClient(ClientCallbackVO.buildStart(xferContext, CallbackActionEnum.CONSULT));
                    return;
                case THREE_WAY_CLIENT:
                case THREE_WAY_AGENT:
                    dataStoreService.notifyClient(ClientCallbackVO.buildStart(xferContext, CallbackActionEnum.THREE_WAY));
                    return;
                case FORCE_CALL_AGENT:
                    dataStoreService.notifyClient(ClientCallbackVO.buildStart(xferContext, CallbackActionEnum.FORCE_CALL));
                    return;
                default:
            }
            return;
        }
        if (Objects.nonNull(callUuidContext.getCallInChannel())) {
            switch (callUuidContext.getCallInChannel()) {
                case CALL:
                    agentCallInNotify(callUuidContext, CallbackActionEnum.CALL);
                    return;
                case PREVIEW_OUT_CALL:
                    agentCallInNotify(callUuidContext, CallbackActionEnum.PREVIEW_OUT_CALL);
                    return;
                default:
                    log.debug("default callin channel");
            }
        }
    }

    /**
     * 坐席呼出对端应答后的提示音
     * 坐席呼出-客户接通, 给坐席放音
     *
     * @since 7.0.0
     */
    private void callOutPeerAnswerPlayback(String companyCode, String uuid) {
        CompanyInfo companyInfo = commonDataOperateService.getCompanyInfoDTO(companyCode);
        if (companyInfo == null) {
            return;
        }
        Integer callOutPeerAnswerPlaybackSwitch = companyInfo.getCallOutPeerAnswerPlaybackSwitch();
        if (CommonConstant.ENABLE_Y.equals(callOutPeerAnswerPlaybackSwitch)) {
            String defaultTone = commonDataOperateService.getDefaultTone(DefaultToneEnum.CALL_OUT_PEER_ANSWER_PLAYBACK);
            log.info("[接通事件] 坐席呼出对端应答后的提示音: {}", defaultTone);
            if (StrUtil.isEmpty(defaultTone)) {
                return;
            }
            PlaybackDTO playbackDTO = PlaybackDTO.build(companyCode, uuid, defaultTone, 1);
            freeswitchRequestService.playback(playbackDTO);
        }
    }

    private void agentCallInNotify(CallUuidContext callUuidContext, CallbackActionEnum callbackActionEnum) {
        String companyCode = callUuidContext.getCompanyCode();
        String bridgeUUID = callUuidContext.getBridgeUUID();

        callOutPeerAnswerPlayback(companyCode, callUuidContext.findRelationUUID());
        CallUuidContext bridgeContext = commonDataOperateService.getCallUuidContext(companyCode, bridgeUUID);
        ClientCallbackVO clientCallbackVO = ClientCallbackVO.buildStart(bridgeContext, callbackActionEnum);
        clientCallbackVO.setWorkOrderId(callUuidContext.getWorkOrderId());
        clientCallbackVO.setPeerUuid(callUuidContext.getUUID());
        clientCallbackVO.setDisplayNumber(callUuidContext.getDisplayNumber());
        dataStoreService.notifyClient(clientCallbackVO);
    }

    @Override
    public CallStatusEventEnum getCallStatus() {
        return CallStatusEventEnum.ANSWER;
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
        return ExtStatusTransferActionEnum.ANSWER;
    }

    @Override
    public ExtStatusEnum getExtStatusEnum() {
        return ExtStatusEnum.CALLING;
    }
}
