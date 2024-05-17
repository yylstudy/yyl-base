package com.cqt.call.strategy.event.impl;

import cn.hutool.core.date.DateUtil;
import com.cqt.base.enums.*;
import com.cqt.base.enums.cdr.HangupCauseEnum;
import com.cqt.base.enums.cdr.ReleaseDirEnum;
import com.cqt.base.util.CacheUtil;
import com.cqt.call.config.ThreadPoolConfig;
import com.cqt.call.strategy.event.EventStrategy;
import com.cqt.call.strategy.event.callin.CallInEventStrategyFactory;
import com.cqt.cloudcc.manager.event.concurrency.ConcurrencyControlEvent;
import com.cqt.cloudcc.manager.service.CommonDataOperateService;
import com.cqt.cloudcc.manager.service.FreeswitchRequestService;
import com.cqt.model.agent.vo.CallUuidContext;
import com.cqt.model.agent.vo.CallUuidRelationDTO;
import com.cqt.model.cdr.dto.CallCdrDTO;
import com.cqt.model.common.CloudCallCenterProperties;
import com.cqt.model.freeswitch.dto.api.AnswerDTO;
import com.cqt.model.freeswitch.dto.api.HangupDTO;
import com.cqt.model.freeswitch.dto.event.CallInEventDTO;
import com.cqt.model.number.entity.NumberInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonClient;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.Duration;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * @author linshiqiang
 * date:  2023-07-03 11:48
 * 客户呼入事件
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CallInEventStrategyImpl implements EventStrategy {

    private final ObjectMapper objectMapper;

    private final CallInEventStrategyFactory callInEventStrategyFactory;

    private final FreeswitchRequestService freeswitchRequestService;

    private final CommonDataOperateService commonDataOperateService;

    private final CloudCallCenterProperties cloudCallCenterProperties;

    private final RedissonClient redissonClient;

    private final ApplicationContext applicationContext;

    @Resource(name = ThreadPoolConfig.BASE_POOL_NAME)
    private Executor baseReqExecutor;

    @Override
    public EventEnum getEventType() {
        return EventEnum.call_in;
    }

    @Override
    public void deal(String message) throws Exception {
        CallInEventDTO callInEventDTO = objectMapper.readValue(message, CallInEventDTO.class);
        String uuid = callInEventDTO.getUuid();

        // 呼入消息幂等
        String idempotentLockKey = CacheUtil.getMessageIdempotentLockKey(uuid, callInEventDTO.getEvent());
        Duration idempotent = cloudCallCenterProperties.getBase().getMessageIdempotent();
        boolean absent = redissonClient.getBucket(idempotentLockKey).setIfAbsent(1, idempotent);
        if (!absent) {
            log.warn("[客户呼入事件] 消息重复已处理过, uuid: {}, event: {}", idempotentLockKey, callInEventDTO.getEvent());
            return;
        }
        CallInEventDTO.EventData eventData = callInEventDTO.getData();
        String companyCode = callInEventDTO.getCompanyCode();
        String callerNumber = eventData.getCallerNumber();

        // 企业号码
        String calleeNumber = eventData.getCalleeNumber();

        log.info("[客户呼入事件] 企业: {}, 呼入主叫: {}, 呼入被叫: {}, message: {}",
                companyCode, eventData.getCallerNumber(), calleeNumber, message);
        // 保存uuid上下文
        CallUuidContext callUuidContext = getCallUuidContext(callInEventDTO);

        // 保存计费号码
        commonDataOperateService.saveCdrPlatformNumber(companyCode, callUuidContext.getMainCallId(), calleeNumber);

        boolean isBlack = commonDataOperateService.checkBlackNumber(companyCode, callerNumber, CallDirectionEnum.INBOUND);
        if (isBlack) {
            log.info("[客户呼入事件] 企业: {}, 来电号码: {}, 在黑名单内!", companyCode, callerNumber);
            callUuidContext.getCurrent().getCallCdrDTO().setHangupCauseEnum(HangupCauseEnum.NUMBER_INVALID);
            callUuidContext.getCurrent().setReleaseDir(ReleaseDirEnum.PLATFORM);
            commonDataOperateService.saveCallUuidContext(callUuidContext);
            HangupDTO hangupDTO = HangupDTO.build(companyCode, uuid, HangupCauseEnum.CALL_IN_NUMBER_IN_BLACK_LIST);
            freeswitchRequestService.hangup(hangupDTO);
            return;
        }

        // 并发控制
        concurrencyControl(callInEventDTO);

        // 根据被叫号码查询号码呼入策略
        Optional<NumberInfo> numberInfoOptional = commonDataOperateService.getNumberInfo(calleeNumber);
        if (!numberInfoOptional.isPresent()) {
            callUuidContext.getCurrent().getCallCdrDTO().setHangupCauseEnum(HangupCauseEnum.NUMBER_INVALID);
            callUuidContext.getCurrent().setReleaseDir(ReleaseDirEnum.PLATFORM);
            commonDataOperateService.saveCallUuidContext(callUuidContext);
            // 未查询到号码信息, 直接挂断hangup
            HangupDTO hangupDTO = HangupDTO.build(companyCode, uuid, HangupCauseEnum.CALL_IN_NOT_FIND_NUMBER_INFO);
            freeswitchRequestService.hangup(hangupDTO);
            log.info("[客户呼入事件] 企业: {}, 未找到号码信息: {}, 挂断客户通话", companyCode, calleeNumber);
            return;
        }
        NumberInfo numberInfo = numberInfoOptional.get();
        // 执行具体服务 ivr skill
        Integer serviceWay = numberInfo.getServiceWay();

        if (CallInStrategyEnum.IVR.getCode().equals(serviceWay)) {
            callUuidContext.setCallinIVR(true);
            callUuidContext.setStartIvrTime(DateUtil.date());
        }
        commonDataOperateService.saveCallUuidContext(callUuidContext);

        if (!Objects.equals(numberInfo.getStatus(), 1)) {
            log.info("[客户呼入事件] 企业: {}, 号码: {}, 不可用", companyCode, calleeNumber);
            callUuidContext.getCurrent().setReleaseDir(ReleaseDirEnum.PLATFORM);
            callUuidContext.getCurrent().getCallCdrDTO().setHangupCauseEnum(HangupCauseEnum.NUMBER_UN_ENABLE);
            commonDataOperateService.saveCallUuidContext(callUuidContext);
            HangupDTO hangupDTO = HangupDTO.build(companyCode, uuid, HangupCauseEnum.NUMBER_UN_ENABLE);
            freeswitchRequestService.hangup(hangupDTO);
            return;
        }

        // 接听
        CompletableFuture.runAsync(() -> {
            freeswitchRequestService.answer(AnswerDTO.build(callInEventDTO));
        }, baseReqExecutor).thenRun(() -> {
            try {
                boolean deal = callInEventStrategyFactory.deal(serviceWay, callInEventDTO, numberInfo);
                log.info("[客户呼入事件] 企业: {}, 企业号码: {}, 服务方式: {}, 执行结束: {}", companyCode, calleeNumber, serviceWay, deal);
            } catch (Exception e) {
                log.info("[客户呼入事件] 企业: {}, 企业号码: {}, 服务方式: {}, 执行异常: ", companyCode, calleeNumber, serviceWay, e);
            }
        });
    }

    private void concurrencyControl(CallInEventDTO callInEventDTO) {
        applicationContext.publishEvent(new ConcurrencyControlEvent(this,
                callInEventDTO.getUuid(),
                callInEventDTO.getAudioCode(),
                callInEventDTO.getVideoCode(),
                callInEventDTO.getCompanyCode(),
                callInEventDTO.getData().getCalleeNumber(),
                null,
                null));
    }

    private CallUuidContext getCallUuidContext(CallInEventDTO callInEventDTO) {
        CallInEventDTO.EventData eventData = callInEventDTO.getData();
        CallUuidContext callUuidContext = new CallUuidContext();
        CallUuidRelationDTO callUuidRelationDTO = new CallUuidRelationDTO();
        callUuidRelationDTO.setUuid(callInEventDTO.getUuid());
        callUuidRelationDTO.setMainCdrFlag(true);
        callUuidRelationDTO.setMainUuid(callInEventDTO.getUuid());
        callUuidRelationDTO.setMainCallId(commonDataOperateService.createMainCallId());
        callUuidRelationDTO.setCompanyCode(callInEventDTO.getCompanyCode());
        callUuidRelationDTO.setServerId(callInEventDTO.getServerId());
        callUuidRelationDTO.setNumber(eventData.getCallerNumber());
        callUuidRelationDTO.setCallerNumber(eventData.getCallerNumber());
        callUuidRelationDTO.setDisplayNumber(eventData.getCallerNumber());
        callUuidRelationDTO.setCalleeNumber(eventData.getCalleeNumber());
        callUuidRelationDTO.setPlatformNumber(eventData.getCalleeNumber());
        callUuidRelationDTO.setCallDirectionEnum(CallDirectionEnum.INBOUND);
        callUuidRelationDTO.setCallRoleEnum(CallRoleEnum.CLIENT_CALLER);
        callUuidRelationDTO.setCallTypeEnum(CallTypeEnum.CLIENT);
        callUuidRelationDTO.setVideo(MediaStreamEnum.valueOf(callInEventDTO.getData().getVideo()).getCode());
        callUuidRelationDTO.setAudio(MediaStreamEnum.valueOf(callInEventDTO.getData().getAudio()).getCode());
        callUuidRelationDTO.setRecordNode(RecordNodeEnum.CALL_IN_A);
        CallCdrDTO callCdrDTO = new CallCdrDTO();
        callCdrDTO.setUuid(callInEventDTO.getUuid());
        callCdrDTO.setCallInFlag(true);
        callCdrDTO.setCalInTimestamp(callInEventDTO.getTimestamp());
        callUuidRelationDTO.setCallCdrDTO(callCdrDTO);
        callUuidContext.setCurrent(callUuidRelationDTO);
        return callUuidContext;
    }
}
