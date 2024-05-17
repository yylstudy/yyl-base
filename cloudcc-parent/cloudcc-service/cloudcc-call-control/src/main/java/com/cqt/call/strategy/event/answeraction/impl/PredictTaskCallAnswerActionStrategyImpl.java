package com.cqt.call.strategy.event.answeraction.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.cqt.base.enums.*;
import com.cqt.base.enums.agent.AgentStatusEnum;
import com.cqt.base.util.CacheUtil;
import com.cqt.call.service.DataStoreService;
import com.cqt.call.strategy.event.answeraction.AfterAnswerActionStrategy;
import com.cqt.cloudcc.manager.service.CommonDataOperateService;
import com.cqt.cloudcc.manager.service.FreeswitchRequestService;
import com.cqt.model.agent.dto.AgentStatusDTO;
import com.cqt.model.agent.vo.CallUuidContext;
import com.cqt.model.agent.vo.CallUuidRelationDTO;
import com.cqt.model.agent.vo.RelateUuidDTO;
import com.cqt.model.cdr.dto.CallCdrDTO;
import com.cqt.model.client.dto.ClientOutboundCallTaskDTO;
import com.cqt.model.freeswitch.dto.api.CallBridgeDTO;
import com.cqt.model.freeswitch.dto.api.PlaybackDTO;
import com.cqt.model.freeswitch.dto.event.CallStatusEventDTO;
import com.cqt.model.freeswitch.vo.FreeswitchApiVO;
import com.cqt.starter.redis.util.RedissonUtil;
import com.google.common.collect.Sets;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * @author linshiqiang
 * date:  2023-10-20 9:32
 * 接通后 预测外呼
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PredictTaskCallAnswerActionStrategyImpl implements AfterAnswerActionStrategy {

    private final FreeswitchRequestService freeswitchRequestService;

    private final CommonDataOperateService commonDataOperateService;

    private final DataStoreService dataStoreService;

    private final RedissonUtil redissonUtil;

    @Override
    public OriginateAfterActionEnum getOriginateAfterAction() {
        return OriginateAfterActionEnum.PREDICT_TASK;
    }

    @Override
    public void execute(CallStatusEventDTO callStatusEventDTO, CallUuidContext callUuidContext) {
        // 播放等待音 - 坐席接通后停止放音
        playWaitingDone(callStatusEventDTO, callUuidContext);

        // 分配坐席
        String clientUuid = callUuidContext.getUUID();
        ClientOutboundCallTaskDTO callTaskDTO = callUuidContext.getClientOutboundCallTaskDTO();
        String companyCode = callTaskDTO.getCompanyCode();
        String taskId = callTaskDTO.getTaskId();
        String clientNumber = callTaskDTO.getClientNumber();
        dataStoreService.answerPredictNotice(companyCode, taskId, callTaskDTO.getMember());

        log.info("[接通事件-预测外呼] 转坐席, 企业: {}, 任务id: {}, 客户号码: {}", companyCode, taskId, clientNumber);
        // lock taskId 接通后 要顺序分配坐席
        String outCallTaskLockKey = CacheUtil.getOutCallTaskLockKey(taskId);
        RLock lock = redissonUtil.getLock(outCallTaskLockKey);
        lock.lock(30, TimeUnit.SECONDS);
        try {
            String agentUuid = getOriginateAfterAction().getName() + StrUtil.DASHED + IdUtil.fastUUID();
            List<String> freeAgents = commonDataOperateService.getPredictFreeAgentQueue(companyCode, taskId);
            for (String agentId : freeAgents) {
                Optional<AgentStatusDTO> optional = commonDataOperateService.getActualAgentStatus(companyCode, agentId);
                if (!optional.isPresent()) {
                    continue;
                }
                AgentStatusDTO agentStatusDTO = optional.get();
                if (AgentStatusEnum.FREE.name().equals(agentStatusDTO.getTargetStatus())) {
                    Boolean setNx = dataStoreService.lockOriginate(companyCode, agentId, callStatusEventDTO.getUuid());
                    if (!Boolean.TRUE.equals(setNx)) {
                        log.info("[接通事件-预测外呼] 企业: {}, 任务id: {}, 坐席: {}, 坐席已呼叫, 未挂断", companyCode, taskId, agentId);
                        continue;
                    }
                    // 保存坐席uuid上下文
                    String extId = agentStatusDTO.getExtId();
                    writeAgentContext(callStatusEventDTO, callUuidContext, agentUuid, agentStatusDTO);

                    log.info("[接通事件-预测外呼] 企业: {}, 任务id: {}, 分配坐席: {}, 客户号码: {}",
                            companyCode, taskId, agentId, clientNumber);
                    // 桥接客户和坐席
                    CallBridgeDTO callBridgeDTO = CallBridgeDTO.buildCallBridgeDTO(callStatusEventDTO, callUuidContext,
                            agentUuid, extId);
                    FreeswitchApiVO freeswitchApiVO = freeswitchRequestService.callBridge(callBridgeDTO);
                    if (freeswitchApiVO.getResult()) {
                        // 话单关联
                        String mainCallId = callUuidContext.getMainCallId();
                        commonDataOperateService.saveCdrLink(companyCode, mainCallId, clientUuid, agentUuid);

                        // TODO 坐席移除空闲队列 在invite
                        log.info("[接通事件-预测外呼] 转坐席成功, 企业: {}, 任务id: {}, 坐席uuid: {}, 坐席: {}",
                                companyCode, taskId, agentUuid, agentId);
                        return;
                    }
                }
            }
        } catch (Exception e) {
            log.error("[接通事件-预测外呼] 转坐席失败, 企业: {}, 任务id: {}, 异常: ", companyCode, taskId, e);
        } finally {
            try {
                lock.unlock();
            } catch (Exception e) {
                log.error("[接通事件-预测外呼] 企业: {}, 任务id: {}, lock.unlock 异常: ", companyCode, taskId, e);
            }
        }
        // 分配坐席失败, 等待音放完挂断
        callUuidContext.getCurrent().setHangupAfterPlayback(true);
        commonDataOperateService.saveCallUuidContext(callUuidContext);
        log.info("[接通事件-预测外呼] 转坐席失败, 企业: {}, 任务id: {}, 客户号码: {}", companyCode, taskId, clientNumber);
    }

    /**
     * 客户接通, 播放等待音
     *
     * @param callStatusEventDTO 当前事件消息
     * @param callUuidContext    当前uuid上下文-客户
     */
    private void playWaitingDone(CallStatusEventDTO callStatusEventDTO, CallUuidContext callUuidContext) {
        String companyCode = callStatusEventDTO.getCompanyCode();
        String uuid = callStatusEventDTO.getUuid();
        ClientOutboundCallTaskDTO callTaskDTO = callUuidContext.getClientOutboundCallTaskDTO();
        String waitingToneId = callTaskDTO.getWaitingTone();
        String filePath = commonDataOperateService.getFilePath(companyCode, waitingToneId);
        if (StrUtil.isEmpty(filePath)) {
            return;
        }
        PlaybackDTO playbackDTO = PlaybackDTO.build(companyCode, uuid, filePath, 0);
        FreeswitchApiVO apiVO = freeswitchRequestService.playback(playbackDTO);
        if (apiVO.getResult()) {
            log.info("[接通事件] 预测外呼-播放等待音成功， 企业： {}, uuid: {}", companyCode, uuid);
        }
    }

    /**
     * 客户接通, 保存坐席uuid上下文
     *
     * @param callStatusEventDTO 当前事件消息
     * @param clientContext      当前uuid上下文-客户
     */
    private void writeAgentContext(CallStatusEventDTO callStatusEventDTO,
                                   CallUuidContext clientContext,
                                   String agentUuid,
                                   AgentStatusDTO agentStatusDTO) {
        String companyCode = callStatusEventDTO.getCompanyCode();
        String clientUuid = clientContext.getUUID();

        ClientOutboundCallTaskDTO callTaskDTO = clientContext.getClientOutboundCallTaskDTO();
        CallUuidRelationDTO bridgeCallUuidRelationDTO = new CallUuidRelationDTO();
        bridgeCallUuidRelationDTO.setMainCdrFlag(false);
        bridgeCallUuidRelationDTO.setMainCallId(clientContext.getMainCallId());
        bridgeCallUuidRelationDTO.setUuid(agentUuid);
        bridgeCallUuidRelationDTO.setMainUuid(clientContext.getMainUUID());
        bridgeCallUuidRelationDTO.setRelationUuid(clientUuid);
        bridgeCallUuidRelationDTO.setNumber(agentStatusDTO.getExtId());
        bridgeCallUuidRelationDTO.setCallDirectionEnum(CallDirectionEnum.INBOUND);
        bridgeCallUuidRelationDTO.setCallRoleEnum(CallRoleEnum.AGENT_CALLEE);
        bridgeCallUuidRelationDTO.setCallTypeEnum(CallTypeEnum.AGENT);
        bridgeCallUuidRelationDTO.setAgentId(agentStatusDTO.getAgentId());
        bridgeCallUuidRelationDTO.setExtId(agentStatusDTO.getExtId());
        bridgeCallUuidRelationDTO.setReqId(callTaskDTO.getReqId());
        bridgeCallUuidRelationDTO.setCallerNumber(callTaskDTO.getCallerNumber());
        bridgeCallUuidRelationDTO.setDisplayNumber(callTaskDTO.getCalleeNumber());
        bridgeCallUuidRelationDTO.setCalleeNumber(callTaskDTO.getCalleeNumber());
        bridgeCallUuidRelationDTO.setPlatformNumber(callTaskDTO.getDisplayNumber());
        bridgeCallUuidRelationDTO.setCompanyCode(callStatusEventDTO.getCompanyCode());
        // 若是桥接内线 查询下坐席的信息
        bridgeCallUuidRelationDTO.setServerId(callStatusEventDTO.getServerId());
        bridgeCallUuidRelationDTO.setVideo(clientContext.getVideo());
        bridgeCallUuidRelationDTO.setAudio(clientContext.getAudio());
        bridgeCallUuidRelationDTO.setCallInFlag(true);
        bridgeCallUuidRelationDTO.setCallInChannel(CallInChannelEnum.PREDICT_OUT_CALL);
        bridgeCallUuidRelationDTO.setCallCdrDTO(new CallCdrDTO());
        bridgeCallUuidRelationDTO.setRecordNode(RecordNodeEnum.CALL_OUT_B);
        bridgeCallUuidRelationDTO.setOs(agentStatusDTO.getOs());
        // 坐席uuid上下文- 外呼并桥接坐席
        CallUuidContext agentCallUuidContext = CallUuidContext.builder()
                .current(bridgeCallUuidRelationDTO)
                .relationUuid(Sets.newHashSet(clientUuid))
                .relateUuidDTO(RelateUuidDTO.builder().bridgeUUID(clientUuid).build())
                .clientOutboundCallTaskDTO(callTaskDTO)
                .build();
        commonDataOperateService.saveCallUuidContext(agentCallUuidContext);

        // 当前上下文-客户, 保存关联坐席uuid
        clientContext.setRelationUuid(Sets.newHashSet(agentUuid));
        clientContext.fillRelationUuidSet(agentUuid);
        clientContext.fillRelateUuidDtoByCallBridge(agentUuid);

        // 子话单uuid
        commonDataOperateService.saveCallUuidContext(clientContext);
        commonDataOperateService.saveCdrLink(companyCode, clientContext.getMainCallId(), clientUuid, agentUuid);
    }
}
