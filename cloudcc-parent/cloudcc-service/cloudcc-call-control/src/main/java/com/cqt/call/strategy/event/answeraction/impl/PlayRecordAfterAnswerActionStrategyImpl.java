package com.cqt.call.strategy.event.answeraction.impl;

import com.cqt.base.enums.CallbackActionEnum;
import com.cqt.base.enums.OriginateAfterActionEnum;
import com.cqt.call.service.DataStoreService;
import com.cqt.call.strategy.event.answeraction.AfterAnswerActionStrategy;
import com.cqt.cloudcc.manager.service.FreeswitchRequestService;
import com.cqt.model.agent.vo.CallUuidContext;
import com.cqt.model.client.vo.ClientCallbackVO;
import com.cqt.model.freeswitch.dto.api.PlaybackDTO;
import com.cqt.model.freeswitch.dto.event.CallStatusEventDTO;
import com.cqt.model.freeswitch.vo.FreeswitchApiVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.stereotype.Service;

/**
 * @author linshiqiang
 * date:  2023-10-20 9:32
 * 接通后 播放录音
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PlayRecordAfterAnswerActionStrategyImpl implements AfterAnswerActionStrategy {

    private final DataStoreService dataStoreService;

    private final FreeswitchRequestService freeswitchRequestService;

    @Override
    public OriginateAfterActionEnum getOriginateAfterAction() {
        return OriginateAfterActionEnum.PLAY_RECORD;
    }

    @Override
    public void execute(CallStatusEventDTO callStatusEventDTO, CallUuidContext callUuidContext) {
        playRecord(callStatusEventDTO, callUuidContext);
    }

    /**
     * 坐席接通 话务条播放录音
     *
     * @param callStatusEventDTO 当前事件消息
     * @param callUuidContext    当前uuid上下文-坐席
     */
    private void playRecord(CallStatusEventDTO callStatusEventDTO, CallUuidContext callUuidContext) {
        String companyCode = callStatusEventDTO.getCompanyCode();
        String uuid = callStatusEventDTO.getUuid();
        String playRecordPath = callUuidContext.getPlayRecordPath();
        PlaybackDTO playbackDTO = PlaybackDTO.build(companyCode, uuid, playRecordPath, 1);
        FreeswitchApiVO apiVO = freeswitchRequestService.playback(playbackDTO);
        if (BooleanUtils.isTrue(apiVO.getResult())) {
            ClientCallbackVO vo = ClientCallbackVO.buildInit(callUuidContext, CallbackActionEnum.PLAY_RECORD,
                    true, "录音播放成功!");
            dataStoreService.notifyClient(vo);
        }
    }
}
