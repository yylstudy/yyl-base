package com.cqt.call.strategy.event.callstatus.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.cqt.base.contants.BaseErrorMsgConstant;
import com.cqt.base.enums.*;
import com.cqt.base.enums.agent.AgentStatusEnum;
import com.cqt.base.enums.agent.AgentStatusTransferActionEnum;
import com.cqt.base.enums.calltask.CallTaskEnum;
import com.cqt.base.enums.cdr.HangupCauseEnum;
import com.cqt.base.enums.ext.ExtStatusEnum;
import com.cqt.base.enums.ext.ExtStatusTransferActionEnum;
import com.cqt.call.config.ThreadPoolConfig;
import com.cqt.call.event.calltask.CallTaskCallbackEvent;
import com.cqt.call.event.cdr.CdrGenerateEvent;
import com.cqt.call.event.incalls.InCallsNumberEvent;
import com.cqt.call.event.stats.CallStatsEvent;
import com.cqt.call.service.DataStoreService;
import com.cqt.call.strategy.event.callstatus.CallStatusStrategy;
import com.cqt.cloudcc.manager.service.CommonDataOperateService;
import com.cqt.cloudcc.manager.service.FreeswitchRequestService;
import com.cqt.model.agent.bo.AgentStatusTransferBO;
import com.cqt.model.agent.dto.AgentStatusDTO;
import com.cqt.model.agent.vo.CallUuidContext;
import com.cqt.model.cdr.dto.CallCdrDTO;
import com.cqt.model.cdr.dto.CdrGenerateDTO;
import com.cqt.model.client.dto.ClientOutboundCallTaskDTO;
import com.cqt.model.client.dto.ClientPreviewOutCallDTO;
import com.cqt.model.client.vo.ClientCallbackVO;
import com.cqt.model.freeswitch.dto.api.HangupDTO;
import com.cqt.model.freeswitch.dto.api.PlaybackDTO;
import com.cqt.model.freeswitch.dto.event.CallStatusEventDTO;
import com.cqt.model.queue.dto.UserQueueUpDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Executor;

/**
 * @author linshiqiang
 * date:  2023-07-03 13:37
 * 挂断
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HangupCallStatusStrategyImpl extends AbstractCallStatus implements CallStatusStrategy {

    private final CommonDataOperateService commonDataOperateService;

    private final DataStoreService dataStoreService;

    private final ObjectMapper objectMapper;

    private final ApplicationContext applicationContext;

    private final FreeswitchRequestService freeswitchRequestService;

    @Resource(name = ThreadPoolConfig.BASE_POOL_NAME)
    private Executor baseReqExecutor;

    @Override
    public void deal(CallStatusEventDTO callStatusEventDTO) throws Exception {
        String companyCode = callStatusEventDTO.getCompanyCode();
        String uuid = callStatusEventDTO.getUuid();
        CallUuidContext callUuidContext = commonDataOperateService.getCallUuidContext(companyCode, uuid);
        if (Objects.isNull(callUuidContext)) {
            log.info("[挂断事件] 企业id: {}, uuid: {}, 未查询到上下文信息", companyCode, uuid);
            return;
        }
        if (log.isDebugEnabled()) {
            log.debug("[挂断事件] 当前uuid上下文信息: {}", objectMapper.writeValueAsString(callUuidContext));
        }
        // 保存通话uuid上下文
        saveContext(callStatusEventDTO, callUuidContext);

        // 通话中的号码-删除
        deleteInCallsNumber(callUuidContext);

        // 挂断callback通知
        hangupCallback(callUuidContext);

        // 外呼任务结果回调
        outboundCallTaskCallback(callUuidContext);

        // 判断是哪方
        CallTypeEnum callTypeEnum = callUuidContext.getCallTypeEnum();
        if (CallTypeEnum.CLIENT.equals(callTypeEnum)) {
            // 客户呼入ivr 主动挂断移除排队
            checkCallInIvr(callUuidContext);
            // 客户侧挂断
            cdrEvent(callStatusEventDTO, callUuidContext);
            hangupAll(callUuidContext);
            return;
        }
        String agentId = callUuidContext.getAgentId();

        // 坐席通话时间累加
        publishCallStatsEvent(callStatusEventDTO, callUuidContext);

        // 分机状态
        extStatusTransfer(callStatusEventDTO, callUuidContext);

        boolean transferStatus = true;
        CallRoleEnum callRoleEnum = callUuidContext.getCurrent().getCallRoleEnum();
        if (CallRoleEnum.CALLIN_TRANSFER_AGENT.equals(callRoleEnum)) {
            transferStatus = callinTransferAgent(callStatusEventDTO, callUuidContext);
        }

        // 判断是否挂断所有关联通话
        hangupAll(callUuidContext);

        // 生成话单
        cdrEvent(callStatusEventDTO, callUuidContext);

        // 调用SDK-Interface
        //  0. 判断坐席挂断后应该进入什么状态, 根据企业或坐席配置
        //  1. 坐席状态迁移 通话中->挂断
        //  2. 保存坐席实时状态
        //  3. 在客户呼入分配该坐席, 坐席未接, 将坐席状态设置为忙碌
        //  4. 通知前端SDK
        if (transferStatus && !callUuidContext.isCheckoutToOffline()) {
            AgentStatusTransferBO agentStatusTransferBO = getAgentStatusTransferBO(callStatusEventDTO, callUuidContext);
            // 呼出失败 未接通客户
            boolean agentCallOutFail = agentCallOutFail(callUuidContext);
            agentStatusTransferBO.setAgentCallOutFail(agentCallOutFail);

            boolean transfer = dataStoreService.agentStatusChangeTransfer(agentStatusTransferBO);
            log.info("[挂断事件] 调用SDK-Interface 坐席id: {}, 坐席状态迁移结果: {}", agentId, transfer);
        }

        Boolean unlocked = dataStoreService.unlockOriginate(companyCode, agentId);
        log.info("[挂断事件] 企业: {}, 坐席: {}, unlock: {}", companyCode, agentId, unlocked);
    }

    /**
     * 外呼任务结果回调
     *
     * @param callUuidContext    uuid上下文
     */
    private void outboundCallTaskCallback(CallUuidContext callUuidContext) {
        ClientOutboundCallTaskDTO callTaskDTO = callUuidContext.getClientOutboundCallTaskDTO();
        // client caller
        if (Objects.nonNull(callTaskDTO) && CallRoleEnum.CLIENT_CALLER.equals(callUuidContext.getCallRoleEnum())) {
            String taskType = callTaskDTO.getTaskType();
            // ivr, 预测 坐席
            String bridgeUUID = callUuidContext.getBridgeUUID();
            String companyCode = callUuidContext.getCompanyCode();
            CallUuidContext agentContext = commonDataOperateService.getCallUuidContext(companyCode, bridgeUUID);
            applicationContext.publishEvent(new CallTaskCallbackEvent(this, callUuidContext, agentContext,
                    CallTaskEnum.valueOf(taskType)));
            return;
        }

        // agent caller
        // 预览外呼
        ClientPreviewOutCallDTO previewOutCallDTO = callUuidContext.getClientPreviewOutCallDTO();
        if (Objects.nonNull(previewOutCallDTO)) {
            // 客户uuid
            String bridgeUUID = callUuidContext.getBridgeUUID();
            String companyCode = callUuidContext.getCompanyCode();
            CallUuidContext clientContext = commonDataOperateService.getCallUuidContext(companyCode, bridgeUUID);
            applicationContext.publishEvent(new CallTaskCallbackEvent(this, clientContext, callUuidContext,
                    CallTaskEnum.PREVIEW));
        }

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
     * 通话中的号码-删除
     *
     * @param callUuidContext uuid上下文
     */
    private void deleteInCallsNumber(CallUuidContext callUuidContext) {
        applicationContext.publishEvent(new InCallsNumberEvent(callUuidContext.getCompanyCode(),
                callUuidContext.getMainCallId(),
                callUuidContext.getUUID(),
                callUuidContext.getNumber(),
                OperateTypeEnum.DELETE));
    }

    /**
     * 呼入转坐席挂断处理
     *
     * @param callStatusEventDTO 事件
     * @param callUuidContext    uuid上下文
     * @return 是否状态迁移
     */
    private boolean callinTransferAgent(CallStatusEventDTO callStatusEventDTO, CallUuidContext callUuidContext) {
        if (callUuidContext.isCheckoutToOffline()) {
            return false;
        }
        // 呼入转坐席, 坐席未接 需要再次进入排队, 没有answer事件
        // 添加到排队队列
        UserQueueUpDTO userQueueUpDTO = callUuidContext.getUserQueueUpDTO();
        boolean transferStatus = true;
        if (isNoAnswer(callUuidContext) && Objects.nonNull(userQueueUpDTO)) {
            transferStatus = false;
            AgentStatusTransferBO agentStatusTransferBO = getAgentStatusTransferBO(callStatusEventDTO, callUuidContext);
            // 呼入转坐席响铃, 客户先挂断
            boolean callInAgentClientHangUpFirst = callInAgentClientHangUpFirst(callUuidContext);
            agentStatusTransferBO.setCallInAgentClientHangUpFirst(callInAgentClientHangUpFirst);

            boolean transfer = dataStoreService.agentStatusChangeTransfer(agentStatusTransferBO);
            log.info("[挂断事件] 调用SDK-Interface 坐席: {}, 调用坐席状态切换: {}", callUuidContext.getAgentId(), transfer);

            // 放音
            PlaybackDTO playbackDTO = PlaybackDTO.buildWaitPlayback(userQueueUpDTO);
            freeswitchRequestService.playback(playbackDTO);
            boolean add = dataStoreService.addUserLevelQueue(userQueueUpDTO);
            log.info("[挂断事件] 企业: {}, 坐席: {}, 来电号码: {}, 坐席未接, 重新排队: {}",
                    callUuidContext.getCompanyCode(), callUuidContext.getAgentId(), userQueueUpDTO.getCallerNumber(), add);
        }
        return transferStatus;
    }

    /**
     * 客户呼入ivr 主动挂断移除排队
     */
    private void checkCallInIvr(CallUuidContext callUuidContext) {
        baseReqExecutor.execute(() -> {
            String companyCode = callUuidContext.getCompanyCode();
            if (Boolean.TRUE.equals(callUuidContext.getCallinIVR())) {
                // 移除排队队列
                String callerNumber = callUuidContext.getCallerNumber();
                UserQueueUpDTO userQueueUpDTO = callUuidContext.getUserQueueUpDTO();
                if (Objects.nonNull(userQueueUpDTO)) {
                    boolean removed = dataStoreService.removeQueueUp(userQueueUpDTO);
                    log.info("[挂断事件] 呼入ivr, 企业: {}, 来电: {}, 客户挂断, 移除排队: {}", companyCode, callerNumber, removed);
                }
            }
            // 呼入转离线手机
            if (CallRoleEnum.CALLIN_TRANSFER_OFFLINE_AGENT_PHONE.equals(callUuidContext.getCallRoleEnum())) {
                String callinTransferAgentId = callUuidContext.getCurrent().getCallinTransferAgentId();
                Optional<AgentStatusDTO> agentStatusOptional = commonDataOperateService.getActualAgentStatus(companyCode,
                        callinTransferAgentId);
                if (agentStatusOptional.isPresent()) {
                    AgentStatusDTO agentStatusDTO = agentStatusOptional.get();
                    if (AgentStatusEnum.OFFLINE.name().equals(agentStatusDTO.getTargetStatus())) {
                        dataStoreService.addOfflineAgentQueue(companyCode, callinTransferAgentId,
                                System.currentTimeMillis(), callUuidContext.getNumber());
                    }
                }
            }
        });
    }

    /**
     * 呼入转坐席 坐席接通情况
     *
     * @param callUuidContext uuid上下文
     * @return true/false
     */
    private boolean callInAgentNoAnswer(CallUuidContext callUuidContext) {
        CallCdrDTO callCdrDTO = callUuidContext.getCallCdrDTO();
        // 坐席接通事件
        boolean noAnswer = !Boolean.TRUE.equals(callCdrDTO.getAnswerFlag());
        // 判断是否客户先挂断,坐席未接听, 坐席不设置为忙碌
        String relationUuid = callUuidContext.findRelationUUID();
        if (CallRoleEnum.CALLIN_TRANSFER_AGENT.equals(callUuidContext.getCallRoleEnum())) {
            // 客户侧是否挂断
            Boolean clientIsHangup = commonDataOperateService.isHangup(callUuidContext.getCompanyCode(), relationUuid);
            if (Boolean.TRUE.equals(clientIsHangup) && noAnswer) {
                return true;
            }
        }
        return noAnswer;
    }

    /**
     * 呼入转坐席 客户侧先挂断
     *
     * @param callUuidContext uuid上下文
     * @return true/false
     */
    private boolean callInAgentClientHangUpFirst(CallUuidContext callUuidContext) {
        CallCdrDTO callCdrDTO = callUuidContext.getCallCdrDTO();
        // 坐席接通事件
        boolean noAnswer = !Boolean.TRUE.equals(callCdrDTO.getAnswerFlag());
        // 判断是否客户先挂断,坐席未接听, 坐席不设置为忙碌
        String relationUuid = callUuidContext.findRelationUUID();
        if (CallRoleEnum.CALLIN_TRANSFER_AGENT.equals(callUuidContext.getCallRoleEnum())) {
            // 客户侧是否挂断
            Boolean clientIsHangup = commonDataOperateService.isHangup(callUuidContext.getCompanyCode(), relationUuid);
            return Boolean.TRUE.equals(clientIsHangup) && noAnswer;
        }
        return false;
    }

    /**
     * 外呼未接通客户
     *
     * @param callUuidContext uuid上下文
     * @return true/false
     */
    private boolean agentCallOutFail(CallUuidContext callUuidContext) {
        OriginateAfterActionEnum originateAfterActionEnum = callUuidContext.getCurrent().getOriginateAfterActionEnum();
        if (OriginateAfterActionEnum.CALL_BRIDGE_CLIENT.equals(originateAfterActionEnum)) {
            String companyCode = callUuidContext.getCompanyCode();
            // 客户
            String relationUuid = callUuidContext.findRelationUUID();
            CallUuidContext clientContext = commonDataOperateService.getCallUuidContext(companyCode, relationUuid);

            // 坐席外呼 坐席接通, 客户未接通
            return Boolean.TRUE.equals(callUuidContext.getCallCdrDTO().getAnswerFlag())
                    && !Boolean.TRUE.equals(clientContext.getCallCdrDTO().getAnswerFlag());
        }
        return false;
    }

    /**
     * 是否没有接通事件
     *
     * @param callUuidContext uuid上下文
     * @return true/false
     */
    private boolean isNoAnswer(CallUuidContext callUuidContext) {
        CallCdrDTO callCdrDTO = callUuidContext.getCurrent().getCallCdrDTO();
        boolean noAnswer = !Boolean.TRUE.equals(callCdrDTO.getAnswerFlag());
        if (BaseErrorMsgConstant.NO_ANSWER.equals(callCdrDTO.getHangupCause())) {
            log.info("NO_ANSWER");
            return true;
        }

        HangupCauseEnum hangupCauseEnum = callCdrDTO.getHangupCauseEnum();
        if (HangupCauseEnum.AGENT_REJECT.equals(hangupCauseEnum)) {
            log.info("AGENT_REJECT");
            return true;
        }
        log.info("other: {}", noAnswer);
        return noAnswer;
    }

    private AgentStatusTransferBO getAgentStatusTransferBO(CallStatusEventDTO callStatusEventDTO,
                                                           CallUuidContext callUuidContext) {
        return AgentStatusTransferBO.builder()
                .transferAction(AgentStatusTransferActionEnum.HANGUP)
                .agentId(callUuidContext.getCurrent().getAgentId())
                .companyCode(callUuidContext.getCompanyCode())
                .callRoleEnum(callUuidContext.getCurrent().getCallRoleEnum())
                .extId(callUuidContext.getCurrent().getExtId())
                .os(callUuidContext.getCurrent().getOs())
                .uuid(null)
                .eventTimestamp(callStatusEventDTO.getTimestamp())
                .checkAgentBridgeStatus(callUuidContext.getCheckAgentBridgeStatus())
                .agentNoAnswerMakerBusy(isNoAnswer(callUuidContext))
                .build();
    }

    private void saveContext(CallStatusEventDTO callStatusEventDTO, CallUuidContext callUuidContext) {
        CallCdrDTO callCdrDTO = callUuidContext.getCallCdrDTO();
        callCdrDTO.setHangupFlag(true);
        callCdrDTO.setDa2Result(callStatusEventDTO.getData().getDa2Result());
        callCdrDTO.setHangupTimestamp(callStatusEventDTO.getTimestamp());
        callCdrDTO.setHangupCause(callStatusEventDTO.getData().getHangupCause());
        callCdrDTO.setRecordFileName(callStatusEventDTO.getData().getRecordFileName());
        commonDataOperateService.saveCallUuidContext(callUuidContext);

        // 记录uuid已挂断标识
        dataStoreService.saveUuidHangupFlag(callStatusEventDTO.getCompanyCode(),
                callStatusEventDTO.getUuid(),
                callStatusEventDTO.getTimestamp());
    }

    /**
     * 判断是否挂断关联通话
     */
    private void hangupAll(CallUuidContext callUuidContext) {
        String companyCode = callUuidContext.getCompanyCode();
        boolean hangupOther = checkHangupOther(callUuidContext, companyCode);
        if (hangupOther) {
            baseReqExecutor.execute(() -> {
                Set<String> relationUuidList = callUuidContext.getRelationUuid();
                log.info("[挂断事件] 企业: {}, 挂断全部uuid: {}", companyCode, relationUuidList);
                if (CollUtil.isNotEmpty(relationUuidList)) {
                    for (String id : relationUuidList) {
                        HangupDTO hangupDTO = HangupDTO.build(companyCode, id, HangupCauseEnum.HANGUP_ALL);
                        try {
                            freeswitchRequestService.hangup(hangupDTO);
                            log.info("[挂断事件] 企业: {}, reqId: {}, 挂断uuid: {}", companyCode, hangupDTO.getReqId(), id);
                        } catch (Exception e) {
                            log.info("[挂断事件] 企业: {}, 挂断关联uuid: {}, 异常: ", companyCode, hangupDTO, e);
                        }
                    }
                }
            });
        }
    }

    /**
     * 检查是否挂断所有关联的uuid
     *
     * @param companyCode 企业id
     * @return 挂断其他
     */
    private boolean checkHangupOther(CallUuidContext callUuidContext, String companyCode) {

        // TODO 待验证?? 呼入通道(咨询, 转接, 三方通话), 未接通知SDK
        CallInChannelEnum callInChannel = callUuidContext.getCallInChannel();
        if (CallInChannelEnum.CONSULT.equals(callInChannel)) {
            dataStoreService.delAgentConsultFlag(callUuidContext.getXferUUID());
            CallCdrDTO callCdrDTO = callUuidContext.getCallCdrDTO();
            if (Objects.nonNull(callCdrDTO)) {
                if (!Boolean.TRUE.equals(callCdrDTO.getAnswerFlag())) {
                    return false;
                }
            }
            boolean isOnlyOne = dataStoreService.isOnlyOneCalling(companyCode, callUuidContext.getMainCallId());
            if (isOnlyOne) {
                return true;
            }
        }
        if (CallInChannelEnum.TRANS.equals(callInChannel) || CallInChannelEnum.THREE_WAY.equals(callInChannel)) {
            CallCdrDTO callCdrDTO = callUuidContext.getCallCdrDTO();
            if (Objects.nonNull(callCdrDTO)) {
                if (!Boolean.TRUE.equals(callCdrDTO.getAnswerFlag())) {
                    return false;
                }
            }
        }

        // hangupAll标识
        if (Boolean.FALSE.equals(callUuidContext.getCurrent().getHangupAll())) {
            log.info("[挂断事件] 企业: {}, uuid: {}, 挂断所有: {}", companyCode, callUuidContext.getUUID(), false);
            return false;
        }

        // callin agent is consult?
        String bridgeUUID = callUuidContext.getBridgeUUID();
        boolean isConsult = dataStoreService.isAgentConsult(bridgeUUID);
        if (isConsult) {
            // del key
            dataStoreService.delAgentConsultFlag(bridgeUUID);
            log.info("[挂断事件] 企业: {}, bridgeUUID: {}, is consulting. hangup self", companyCode, bridgeUUID);
            return false;
        }

        // 是否为三方通话
        Boolean threeWay = callUuidContext.getCurrent().getThreeWay();
        if (Boolean.TRUE.equals(threeWay)) {
            // 查询三方通话剩余多少人, 需要限制三方通话人数
            String mainCallId = callUuidContext.getMainCallId();
            return dataStoreService.threeWayHangupAll(companyCode, mainCallId, callUuidContext.getUUID());
        }

        if (CallInChannelEnum.CALL.equals(callInChannel)
                && CallDirectionEnum.OUTBOUND.equals(callUuidContext.getCallDirection())) {
            return true;
        }

        if (CallInChannelEnum.PREDICT_OUT_CALL.equals(callInChannel)) {
            return true;
        }

        // 呼叫角色
        CallRoleEnum callRoleEnum = callUuidContext.getCallRoleEnum();
        switch (callRoleEnum) {
            case CALL_IN_IVR_TRANS_CLIENT:
            case CALL_IN_IVR_TRANS_AGENT:
            case CALLIN_TRANSFER_AGENT:
                CallCdrDTO callCdrDTO = callUuidContext.getCallCdrDTO();
                return Boolean.TRUE.equals(callCdrDTO.getAnswerFlag());
            case AGENT_CALLER:
            case CLIENT_CALLER:
            case CLIENT_CALLEE:
            case TRANS_CLIENT:
            case TRANS_AGENT:
            case CONSULT_TO_TRANS_AGENT:
            case CONSULT_TO_TRANS_CLIENT:
            case CALLIN_TRANSFER_OFFLINE_AGENT_PHONE:
            case THREE_WAY_AGENT:
            case THREE_WAY_CLIENT:
            case FORCE_CALL_AGENT:
                return true;
            default:
                return false;
        }
    }

    /**
     * 挂断回调通知
     *
     * @param callUuidContext uuid上下文
     */
    private void hangupCallback(CallUuidContext callUuidContext) {
        String companyCode = callUuidContext.getCompanyCode();
        Boolean noAnswer = callUuidContext.isNoAnswer();
        String xferUUID = callUuidContext.getXferUUID();
        CallRoleEnum callRoleEnum = callUuidContext.getCallRoleEnum();
        if (StrUtil.isNotEmpty(xferUUID)) {
            CallUuidContext xferContext = commonDataOperateService.getCallUuidContext(companyCode, xferUUID);
            log.info("[挂断事件] xfer 企业: {}, 坐席角色: {}, 对方接通情况: {}, 回调通知SDK",
                    companyCode, callRoleEnum, noAnswer);
            switch (callRoleEnum) {
                case WHISPER_AGENT:
                    dataStoreService.notifyClient(getCallbackVO(xferContext, CallbackActionEnum.WHISPER, noAnswer));
                    return;
                case EAVESDROP_AGENT:
                    dataStoreService.notifyClient(getCallbackVO(xferContext, CallbackActionEnum.EAVESDROP, noAnswer));
                    return;
                case TRANS_CLIENT:
                case TRANS_AGENT:
                    // 坐席侧已挂断, 不通知
                    if (Boolean.TRUE.equals(xferContext.getCallCdrDTO().getHangupFlag())) {
                        return;
                    }
                    dataStoreService.notifyClient(getCallbackVO(xferContext, CallbackActionEnum.TRANS, noAnswer));
                    return;
                case CONSULT_CLIENT:
                case CONSULT_AGENT:
                    dataStoreService.notifyClient(getCallbackVO(xferContext, CallbackActionEnum.CONSULT, noAnswer));
                    return;
                case THREE_WAY_CLIENT:
                case THREE_WAY_AGENT:
                    dataStoreService.notifyClient(getCallbackVO(xferContext, CallbackActionEnum.THREE_WAY, noAnswer));
                    return;
                case FORCE_CALL_AGENT:
                    dataStoreService.notifyClient(getCallbackVO(xferContext, CallbackActionEnum.FORCE_CALL, noAnswer));
                    return;
                default:
            }
        }
    }

    private ClientCallbackVO getCallbackVO(CallUuidContext xferContext,
                                           CallbackActionEnum callbackActionEnum,
                                           Boolean noAnswer) {
        return ClientCallbackVO.buildEnd(xferContext, callbackActionEnum, noAnswer);
    }

    /**
     * 发布话单生成事件
     *
     * @param callStatusEventDTO 通话状态
     * @param callUuidContext    当前通话uuid上下文
     */
    private void cdrEvent(CallStatusEventDTO callStatusEventDTO, CallUuidContext callUuidContext) {
        CdrGenerateDTO cdrGenerateDTO = CdrGenerateDTO.builder()
                .callUuidContext(callUuidContext)
                .callStatusEventDTO(callStatusEventDTO)
                .build();
        applicationContext.publishEvent(new CdrGenerateEvent(this, cdrGenerateDTO));
    }

    @Override
    public CallStatusEventEnum getCallStatus() {
        return CallStatusEventEnum.HANGUP;
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
        return ExtStatusTransferActionEnum.HANGUP;
    }

    @Override
    public ExtStatusEnum getExtStatusEnum() {
        return ExtStatusEnum.ONLINE;
    }
}
