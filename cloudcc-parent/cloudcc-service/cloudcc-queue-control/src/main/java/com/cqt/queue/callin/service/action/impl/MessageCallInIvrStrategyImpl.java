package com.cqt.queue.callin.service.action.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.cqt.base.enums.CallInIvrActionEnum;
import com.cqt.base.enums.DefaultToneEnum;
import com.cqt.base.model.ResultVO;
import com.cqt.cloudcc.manager.service.CommonDataOperateService;
import com.cqt.cloudcc.manager.service.FreeswitchRequestService;
import com.cqt.model.agent.vo.CallUuidContext;
import com.cqt.model.freeswitch.dto.api.CallIvrExitDTO;
import com.cqt.model.freeswitch.dto.api.PlayAndGetDigitsDTO;
import com.cqt.model.freeswitch.dto.api.RecordDTO;
import com.cqt.model.freeswitch.dto.api.StopRecordDTO;
import com.cqt.model.freeswitch.vo.FreeswitchApiVO;
import com.cqt.model.freeswitch.vo.RecordVO;
import com.cqt.model.queue.dto.CallInIvrActionDTO;
import com.cqt.model.queue.vo.CallInIvrActionVO;
import com.cqt.queue.callin.service.action.CallInIvrStrategy;
import io.netty.util.HashedWheelTimer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * @author linshiqiang
 * date:  2023-08-21 10:26
 * 留言
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MessageCallInIvrStrategyImpl implements CallInIvrStrategy {

    private static final HashedWheelTimer MESSAGE_TIMER = new HashedWheelTimer();

    private final CommonDataOperateService commonDataOperateService;

    private final FreeswitchRequestService freeswitchRequestService;

    @Override
    public CallInIvrActionEnum getAction() {
        return CallInIvrActionEnum.IVR_MESSAGE;
    }

    @Override
    public ResultVO<CallInIvrActionVO> execute(CallInIvrActionDTO callInIvrActionDTO) {
        String companyCode = callInIvrActionDTO.getCompanyCode();
        String uuid = callInIvrActionDTO.getUuid();
        CallUuidContext callUuidContext = commonDataOperateService.getCallUuidContext(companyCode, uuid);
        callUuidContext.setVoiceMailFlag(true);
        callUuidContext.setVoiceMailStopDtmf(callInIvrActionDTO.getDtmf());
        commonDataOperateService.saveCallUuidContext(callUuidContext);

        // 留言提示音
        initMessageTone(callInIvrActionDTO);
        // 调用放音收号接口
        PlayAndGetDigitsDTO playAndGetDigitsDTO = PlayAndGetDigitsDTO.messagePlay(callInIvrActionDTO);
        FreeswitchApiVO apiVO = freeswitchRequestService.playAndGetDigits(playAndGetDigitsDTO);
        if (apiVO.getResult()) {
            callUuidContext.setVoiceMailStartTime(DateUtil.date());
            // 开启录音
            // 监听digits_callback事件, 在收到dtmf按键时 结束录音并挂断 --> call-control
            RecordDTO recordDTO = RecordDTO.build(companyCode, uuid);
            log.info("[呼入ivr-留言] 企业: {}, 来电: {}, 开启录制.",
                    callInIvrActionDTO.getCompanyCode(), callInIvrActionDTO.getCallerNumber());
            RecordVO recordVO = freeswitchRequestService.record(recordDTO);
            if (recordVO.getResult()) {
                // 开启定时任务, 超时挂断
                callUuidContext.setRecordId(recordVO.getRecordId());
                callUuidContext.setRecordFileName(recordVO.getRecordFileName());
                commonDataOperateService.saveCallUuidContext(callUuidContext);
                timeoutMessage(callInIvrActionDTO, recordVO.getRecordId());
                return ResultVO.ok();
            }
        }
        return ResultVO.fail(400, apiVO.getMsg());
    }

    /**
     * 查询平台留言默认提示音
     *
     * @param callInIvrActionDTO 呼入ivr参数
     */
    private void initMessageTone(CallInIvrActionDTO callInIvrActionDTO) {
        String defaultTone = commonDataOperateService.getDefaultTone(DefaultToneEnum.MESSAGE);
        if (StrUtil.isEmpty(defaultTone)) {
            return;
        }
        callInIvrActionDTO.setMessageStartRecordTone(defaultTone);
    }

    /**
     * 超时回到ivr
     *
     * @param callInIvrActionDTO 呼入ivr参数
     * @param recordId             录制文件id
     */
    private void timeoutMessage(CallInIvrActionDTO callInIvrActionDTO, String recordId) {
        Integer timeout = callInIvrActionDTO.getTimeout();
        MESSAGE_TIMER.newTimeout(task -> {
            String companyCode = callInIvrActionDTO.getCompanyCode();
            String uuid = callInIvrActionDTO.getUuid();
            String callerNumber = callInIvrActionDTO.getCallerNumber();

            // 查询是否已挂断
            Boolean isHangup = commonDataOperateService.isHangup(companyCode, uuid);
            if (Boolean.TRUE.equals(isHangup)) {
                return;
            }
            // 结束录制
            StopRecordDTO stopRecordDTO = StopRecordDTO.builder()
                    .reqId(IdUtil.fastUUID())
                    .companyCode(companyCode)
                    .uuid(uuid)
                    .recordId(recordId)
                    .build();
            freeswitchRequestService.stopRecord(stopRecordDTO);
            CallUuidContext callUuidContext = commonDataOperateService.getCallUuidContext(companyCode, uuid);
            callUuidContext.setVoiceMailEndTime(DateUtil.date());
            commonDataOperateService.saveCallUuidContext(callUuidContext);

            // 回到ivr
            CallIvrExitDTO callIvrExitDTO = CallIvrExitDTO.build(IdUtil.fastUUID(), companyCode, uuid);
            freeswitchRequestService.callIvrExit(callIvrExitDTO);
            log.info("[呼入ivr-留言] 企业: {}, 来电: {}, 超时 {}s, 回到ivr.", companyCode, callerNumber, timeout);
        }, timeout, TimeUnit.SECONDS);
    }
}
