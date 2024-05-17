package com.cqt.cloudcc.manager.service.impl;

import cn.hutool.core.util.StrUtil;
import com.cqt.base.contants.CacheConstant;
import com.cqt.base.enums.MediaStreamEnum;
import com.cqt.base.util.CacheUtil;
import com.cqt.cloudcc.manager.context.CompanyInfoContext;
import com.cqt.cloudcc.manager.event.concurrency.ConcurrencyControlEvent;
import com.cqt.cloudcc.manager.service.FreeswitchRequestService;
import com.cqt.feign.freeswitch.FreeswitchApiFeignClient;
import com.cqt.model.common.CloudCallCenterProperties;
import com.cqt.model.company.entity.CompanyInfo;
import com.cqt.model.freeswitch.base.FreeswitchApiBase;
import com.cqt.model.freeswitch.dto.api.*;
import com.cqt.model.freeswitch.vo.*;
import com.cqt.starter.redis.util.RedissonUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * @author linshiqiang
 * date:  2023-07-13 9:42
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FreeswitchRequestServiceImpl implements FreeswitchRequestService {

    private final FreeswitchApiFeignClient freeswitchApiFeignClient;

    private final CloudCallCenterProperties cloudCallCenterProperties;

    private final RedissonUtil redissonUtil;

    private final ApplicationContext applicationContext;

    private URI getUri() {
        try {
            String baseUrl = cloudCallCenterProperties.getBase().getBaseUrl();
            return new URI(baseUrl);
        } catch (URISyntaxException e) {
            log.error("[getUri] err: ", e);
            return null;
        }
    }

    /**
     * 发送并发控制事件
     */
    private void concurrencyControl(OriginateDTO originateDTO) {
        applicationContext.publishEvent(new ConcurrencyControlEvent(this,
                null,
                originateDTO.getAudio(),
                originateDTO.getVideo(),
                originateDTO.getCompanyCode(),
                originateDTO.getCallerNumber(),
                null,
                null));
    }

    @Override
    public FreeswitchApiVO originate(OriginateDTO originateDTO) throws Exception {
        // Integer outLine = originateDTO.getOutLine();
        // String calleeNumber = originateDTO.getCalleeNumber();
        // if (OutLineEnum.IN_LINE.getCode().equals(outLine)) {
        //     String originateLockKey = CacheUtil.getOriginateLockKey(calleeNumber);
        //     boolean setNx = redissonUtil.setNx(originateLockKey, originateDTO.getOriUuid(), Duration.ofSeconds(5));
        //     if (!setNx) {
        //         return FreeswitchApiVO.fail(StrFormatter.format("内线: {}, 已经被呼叫, 请稍候再试", calleeNumber));
        //     }
        // }
        originateDTO.setServiceCode(cloudCallCenterProperties.getBase().getServiceCode());
        if (StrUtil.isEmpty(originateDTO.getDisplayNumber())) {
            originateDTO.setDisplayNumber(originateDTO.getCallerNumber());
        }

        CompanyInfo companyInfo = CompanyInfoContext.get();
        if (Objects.nonNull(companyInfo)) {
            if (MediaStreamEnum.SENDRECV.getCode().equals(originateDTO.getVideo())) {
                originateDTO.setRecordSuffix(companyInfo.getVideoRecordFormat());
            } else {
                originateDTO.setRecordSuffix(companyInfo.getVoiceRecordFormat());
            }
            Integer recordChannel = companyInfo.getRecordChannel();
            if (Objects.equals(recordChannel, 2)) {
                originateDTO.setIsStereo(true);
            }
            Integer maxRingTime = originateDTO.getMaxRingTime();
            if (Objects.isNull(maxRingTime)) {
                // 默认设置呼出振铃超时时间, 呼入手动设置
                originateDTO.setMaxRingTime(companyInfo.getCallOutRingTimeout());
            }
        }

        concurrencyControl(originateDTO);
        return freeswitchApiFeignClient.originate(getUri(), originateDTO);
    }

    @Override
    public FreeswitchApiVO hangup(HangupDTO hangupDTO) {
        boolean hangup = true;
        String data;
        try {
            String uuidHangupFlagKey = CacheUtil.getUuidHangupFlagKey(hangupDTO.getCompanyCode(), hangupDTO.getUuid());
            data = redissonUtil.get(uuidHangupFlagKey);
            if (StrUtil.isNotEmpty(data)) {
                hangup = false;
            }
        } catch (Exception e) {
            log.error("[挂断uuid] 查询uuid挂断标志异常: ", e);
        }
        if (hangup) {
            hangupDTO.setServiceCode(cloudCallCenterProperties.getBase().getServiceCode());
            return freeswitchApiFeignClient.hangup(getUri(), hangupDTO);
        }
        log.info("[挂断uuid] 企业: {}, uuid: {}, 已经挂断", hangupDTO.getCompanyCode(), hangupDTO.getUuid());
        return FreeswitchApiVO.notFindUuid("通话已经挂断!");
    }

    @Override
    public FreeswitchApiVO hangupAlwaysExt(HangupDTO hangupDTO) {
        hangupDTO.setServiceCode(cloudCallCenterProperties.getBase().getServiceCode());
        return freeswitchApiFeignClient.hangupAlwaysExt(getUri(), hangupDTO);
    }

    @Override
    public FreeswitchApiVO mediaToggle(MediaToggleDTO mediaToggleDTO) {
        mediaToggleDTO.setServiceCode(cloudCallCenterProperties.getBase().getServiceCode());
        return freeswitchApiFeignClient.mediaToggle(getUri(), mediaToggleDTO);
    }

    @Override
    public FreeswitchApiVO bridge(BridgeDTO bridgeDTO) {
        bridgeDTO.setServiceCode(cloudCallCenterProperties.getBase().getServiceCode());
        return freeswitchApiFeignClient.bridge(getUri(), bridgeDTO);
    }

    @Override
    public FreeswitchApiVO callBridge(CallBridgeDTO callBridgeDTO) {
        // inline lock agent
        // Integer outLine = callBridgeDTO.getOutLine();
        // String calleeNumber = callBridgeDTO.getCalleeNumber();
        // if (OutLineEnum.IN_LINE.getCode().equals(outLine)) {
        //     String originateLockKey = CacheUtil.getOriginateLockKey(calleeNumber);
        //     boolean setNx = redissonUtil.setNx(originateLockKey, callBridgeDTO.getOriUuid(), Duration.ofSeconds(5));
        //     if (!setNx) {
        //         return FreeswitchApiVO.fail(StrFormatter.format("内线: {}, 已经被呼叫, 请稍候再试", calleeNumber));
        //     }
        // }
        callBridgeDTO.setServiceCode(cloudCallCenterProperties.getBase().getServiceCode());
        CompanyInfo companyInfo = CompanyInfoContext.get();
        if (Objects.nonNull(companyInfo)) {
            if (MediaStreamEnum.SENDRECV.getCode().equals(callBridgeDTO.getVideo())) {
                callBridgeDTO.setRecordSuffix(companyInfo.getVideoRecordFormat());
            } else {
                callBridgeDTO.setRecordSuffix(companyInfo.getVoiceRecordFormat());
            }
            Integer recordChannel = companyInfo.getRecordChannel();
            if (Objects.equals(recordChannel, 2)) {
                callBridgeDTO.setIsStereo(true);
            }
            Integer maxRingTime = callBridgeDTO.getMaxRingTime();
            if (Objects.isNull(maxRingTime)) {
                // 默认设置呼出振铃超时时间, 呼入手动设置
                callBridgeDTO.setMaxRingTime(companyInfo.getCallOutRingTimeout());
            }
        }
        return freeswitchApiFeignClient.callBridge(getUri(), callBridgeDTO);
    }

    @Override
    public FreeswitchApiVO answer(AnswerDTO answerDTO) {
        answerDTO.setServiceCode(cloudCallCenterProperties.getBase().getServiceCode());
        return freeswitchApiFeignClient.answer(getUri(), answerDTO);
    }

    @Override
    public FreeswitchApiVO playback(PlaybackDTO playbackDTO) {
        playbackDTO.setServiceCode(cloudCallCenterProperties.getBase().getServiceCode());
        FreeswitchApiVO freeswitchApiVO = freeswitchApiFeignClient.playback(getUri(), playbackDTO);
        if (freeswitchApiVO.getResult()) {
            // 标志已放音
            String playbackFlagKey = CacheUtil.getPlaybackFlagKey(playbackDTO.getCompanyCode(), playbackDTO.getUuid());
            redissonUtil.set(playbackFlagKey, playbackDTO.getFileName(), CacheConstant.TTL, TimeUnit.HOURS);
        }
        return freeswitchApiVO;
    }

    @Override
    public FreeswitchApiVO stopPlay(StopPlayDTO stopPlayDTO) {
        String playbackFlagKey = CacheUtil.getPlaybackFlagKey(stopPlayDTO.getCompanyCode(), stopPlayDTO.getUuid());
        String fileName = redissonUtil.get(playbackFlagKey);
        if (StrUtil.isEmpty(fileName)) {
            return null;
        }
        stopPlayDTO.setServiceCode(cloudCallCenterProperties.getBase().getServiceCode());
        return freeswitchApiFeignClient.stopPlay(getUri(), stopPlayDTO);
    }

    @Override
    public FreeswitchApiVO sendDtmf(SendDtmfDTO sendDtmfDTO) {
        sendDtmfDTO.setServiceCode(cloudCallCenterProperties.getBase().getServiceCode());
        return freeswitchApiFeignClient.sendDtmf(getUri(), sendDtmfDTO);
    }

    @Override
    public FreeswitchApiVO hold(HoldDTO holdDTO) {
        holdDTO.setServiceCode(cloudCallCenterProperties.getBase().getServiceCode());
        return freeswitchApiFeignClient.hold(getUri(), holdDTO);
    }

    @Override
    public RecordVO record(RecordDTO recordDTO) {
        recordDTO.setServiceCode(cloudCallCenterProperties.getBase().getServiceCode());
        return freeswitchApiFeignClient.record(getUri(), recordDTO);
    }

    @Override
    public FreeswitchApiVO stopRecord(StopRecordDTO stopRecordDTO) {
        stopRecordDTO.setServiceCode(cloudCallCenterProperties.getBase().getServiceCode());
        return freeswitchApiFeignClient.stopRecord(getUri(), stopRecordDTO);
    }

    @Override
    public FreeswitchApiVO xfer(XferDTO xferDTO) throws Exception {
        xferDTO.setServiceCode(cloudCallCenterProperties.getBase().getServiceCode());
        return freeswitchApiFeignClient.xfer(getUri(), xferDTO);
    }

    @Override
    public FreeswitchApiVO setSessionVar(SetSessionVarDTO setSessionVarDTO) {
        setSessionVarDTO.setServiceCode(cloudCallCenterProperties.getBase().getServiceCode());
        return freeswitchApiFeignClient.setSessionVar(getUri(), setSessionVarDTO);
    }

    @Override
    public GetSessionVarVO getSessionVar(GetSessionVarDTO getSessionVarDTO) {
        getSessionVarDTO.setServiceCode(cloudCallCenterProperties.getBase().getServiceCode());
        return freeswitchApiFeignClient.getSessionVar(getUri(), getSessionVarDTO);
    }

    @Override
    public ExecuteLuaVO executeLua(ExecuteLuaDTO executeLuaDTO) {
        executeLuaDTO.setServiceCode(cloudCallCenterProperties.getBase().getServiceCode());
        return freeswitchApiFeignClient.executeLua(getUri(), executeLuaDTO);
    }

    @Override
    public ExecuteLuaVO trans2lua(ExecuteLuaDTO executeLuaDTO) {
        executeLuaDTO.setServiceCode(cloudCallCenterProperties.getBase().getServiceCode());
        return freeswitchApiFeignClient.trans2lua(getUri(), executeLuaDTO);
    }

    @Override
    public GetExtensionRegVO getExtensionReg(GetExtensionRegStatusDTO getExtensionRegStatusDTO) {
        getExtensionRegStatusDTO.setServiceCode(cloudCallCenterProperties.getBase().getServiceCode());
        return freeswitchApiFeignClient.getExtensionReg(getUri(), getExtensionRegStatusDTO);
    }

    @Override
    public FreeswitchApiVO callQueueExit(CallQueueExitDTO callQueueExitDTO) {
        callQueueExitDTO.setServiceCode(cloudCallCenterProperties.getBase().getServiceCode());
        return freeswitchApiFeignClient.callQueueExit(getUri(), callQueueExitDTO);
    }

    @Override
    public FreeswitchApiVO callIvrExit(CallIvrExitDTO callIvrExitDTO) {
        callIvrExitDTO.setServiceCode(cloudCallCenterProperties.getBase().getServiceCode());
        return freeswitchApiFeignClient.callIvrExit(getUri(), callIvrExitDTO);
    }

    @Override
    public DisExtensionRegAddrVO disExtensionRegAddr(DisExtensionRegAddrDTO disExtensionRegAddrDTO) {
        disExtensionRegAddrDTO.setServiceCode(cloudCallCenterProperties.getBase().getServiceCode());
        return freeswitchApiFeignClient.disExtensionRegAddr(getUri(), disExtensionRegAddrDTO);
    }

    @Override
    public FreeswitchApiVO callIvrLua(CallIvrLuaDTO callIvrLuaDTO) {
        callIvrLuaDTO.setServiceCode(cloudCallCenterProperties.getBase().getServiceCode());
        return freeswitchApiFeignClient.callIvrLua(getUri(), callIvrLuaDTO);
    }

    @Override
    public FreeswitchApiVO callQueueToAgent(CallQueueToAgentDTO callQueueToAgentDTO) {
        callQueueToAgentDTO.setServiceCode(cloudCallCenterProperties.getBase().getServiceCode());
        return freeswitchApiFeignClient.callQueueToAgent(getUri(), callQueueToAgentDTO);
    }

    @Override
    public FreeswitchApiVO playAndGetDigits(PlayAndGetDigitsDTO playAndGetDigitsDTO) {
        playAndGetDigitsDTO.setServiceCode(cloudCallCenterProperties.getBase().getServiceCode());
        return freeswitchApiFeignClient.playAndGetDigits(getUri(), playAndGetDigitsDTO);
    }

    @Override
    public CompanyConcurrencyVO getOnlineCompanyConcurrency(@RequestBody FreeswitchApiBase freeswitchApiBase) {
        freeswitchApiBase.setServiceCode(cloudCallCenterProperties.getBase().getServiceCode());
        return freeswitchApiFeignClient.getOnlineCompanyConcurrency(getUri(), freeswitchApiBase);
    }

    @Override
    public FreeswitchApiVO playbackControl(PlaybackControlDTO playbackControlDTO) {
        playbackControlDTO.setServiceCode(cloudCallCenterProperties.getBase().getServiceCode());
        return freeswitchApiFeignClient.playbackControl(getUri(), playbackControlDTO);
    }
}
