package com.cqt.call.strategy.event.answeraction.impl;

import cn.hutool.core.util.StrUtil;
import com.cqt.base.enums.OriginateAfterActionEnum;
import com.cqt.call.service.DataStoreService;
import com.cqt.call.strategy.event.answeraction.AfterAnswerActionStrategy;
import com.cqt.cloudcc.manager.service.CommonDataOperateService;
import com.cqt.cloudcc.manager.service.FreeswitchRequestService;
import com.cqt.model.agent.vo.CallUuidContext;
import com.cqt.model.client.dto.ClientOutboundCallTaskDTO;
import com.cqt.model.freeswitch.dto.api.PlaybackDTO;
import com.cqt.model.freeswitch.dto.event.CallStatusEventDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author linshiqiang
 * date:  2023-10-20 9:32
 * 接通后 语音通知
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VoiceNoticeAfterAnswerActionStrategyImpl implements AfterAnswerActionStrategy {

    private final FreeswitchRequestService freeswitchRequestService;

    private final CommonDataOperateService commonDataOperateService;

    private final DataStoreService dataStoreService;

    @Override
    public OriginateAfterActionEnum getOriginateAfterAction() {
        return OriginateAfterActionEnum.VOICE_NOTICE;
    }

    @Override
    public void execute(CallStatusEventDTO callStatusEventDTO, CallUuidContext callUuidContext) {

        ClientOutboundCallTaskDTO clientOutboundCallTaskDTO = callUuidContext.getClientOutboundCallTaskDTO();

        String taskId = clientOutboundCallTaskDTO.getTaskId();
        String member = clientOutboundCallTaskDTO.getMember();
        dataStoreService.answerIvrNotice(clientOutboundCallTaskDTO.getCompanyCode(), taskId, member);

        playVoice(callStatusEventDTO, clientOutboundCallTaskDTO.getVoiceNotifyFileId());
    }

    /**
     * 客户接通, 播放语音
     *
     * @param callStatusEventDTO 当前事件消息
     * @param fileId    文件id
     */
    private void playVoice(CallStatusEventDTO callStatusEventDTO, String fileId) {
        String companyCode = callStatusEventDTO.getCompanyCode();
        String uuid = callStatusEventDTO.getUuid();
        String filePath = commonDataOperateService.getFilePath(companyCode, fileId);
        if (StrUtil.isNotEmpty(filePath)) {
            PlaybackDTO playbackDTO = PlaybackDTO.build(companyCode, uuid, filePath, 1);
            freeswitchRequestService.playback(playbackDTO);
        }
    }
}
