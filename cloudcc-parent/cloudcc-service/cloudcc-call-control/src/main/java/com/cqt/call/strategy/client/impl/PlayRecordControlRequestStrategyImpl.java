package com.cqt.call.strategy.client.impl;

import com.cqt.base.enums.MsgTypeEnum;
import com.cqt.call.converter.ModelConverter;
import com.cqt.call.strategy.client.ClientRequestStrategy;
import com.cqt.cloudcc.manager.service.FreeswitchRequestService;
import com.cqt.model.client.base.ClientResponseBaseVO;
import com.cqt.model.client.dto.ClientPlayRecordControlDTO;
import com.cqt.model.freeswitch.dto.api.PlaybackControlDTO;
import com.cqt.model.freeswitch.vo.FreeswitchApiVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.stereotype.Service;

/**
 * @author linshiqiang
 * date:  2023-07-03 14:42
 * 播放录音 控制进度
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PlayRecordControlRequestStrategyImpl extends AbstractClientChecker implements ClientRequestStrategy {

    private final FreeswitchRequestService freeswitchRequestService;

    @Override
    public MsgTypeEnum getMsgType() {
        return MsgTypeEnum.play_record_control;
    }

    @Override
    public ClientResponseBaseVO deal(String requestBody) throws Exception {
        ClientPlayRecordControlDTO playRecordControlDTO = convert(requestBody, ClientPlayRecordControlDTO.class);
        PlaybackControlDTO playbackControlDTO = ModelConverter.INSTANCE.client2base(playRecordControlDTO);
        FreeswitchApiVO freeswitchApiVO = freeswitchRequestService.playbackControl(playbackControlDTO);
        if (BooleanUtils.isTrue(freeswitchApiVO.getResult())) {
            return ClientResponseBaseVO.response(playRecordControlDTO, "0", "放音控制成功!");
        }
        return ClientResponseBaseVO.response(playRecordControlDTO, "1", "放音控制失败!");
    }

}
