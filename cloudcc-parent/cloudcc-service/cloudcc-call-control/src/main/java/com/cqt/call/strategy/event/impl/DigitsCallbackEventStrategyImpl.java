package com.cqt.call.strategy.event.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.cqt.base.enums.EventEnum;
import com.cqt.call.config.ThreadPoolConfig;
import com.cqt.call.strategy.event.EventStrategy;
import com.cqt.cloudcc.manager.service.CommonDataOperateService;
import com.cqt.cloudcc.manager.service.FreeswitchRequestService;
import com.cqt.model.agent.vo.CallUuidContext;
import com.cqt.model.freeswitch.dto.api.CallIvrExitDTO;
import com.cqt.model.freeswitch.dto.api.StopRecordDTO;
import com.cqt.model.freeswitch.dto.event.DigitsCallbackEventDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * @author linshiqiang
 * date:  2023-07-03 11:48
 * 放音收号回调事件
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DigitsCallbackEventStrategyImpl implements EventStrategy {

    private final ObjectMapper objectMapper;

    private final FreeswitchRequestService freeswitchRequestService;

    private final CommonDataOperateService commonDataOperateService;

    @Override
    public EventEnum getEventType() {
        return EventEnum.digits_callback;
    }

    @Async(ThreadPoolConfig.BASE_POOL_NAME)
    @Override
    public void deal(String message) throws Exception {
        log.info("[放音收号事件] 消息: {}", message);
        DigitsCallbackEventDTO callbackEventDTO = objectMapper.readValue(message, DigitsCallbackEventDTO.class);
        String companyCode = callbackEventDTO.getCompanyCode();
        String uuid = callbackEventDTO.getUuid();
        String content = callbackEventDTO.getData().getContent();
        CallUuidContext callUuidContext = commonDataOperateService.getCallUuidContext(companyCode, uuid);
        if (Objects.isNull(callUuidContext)) {
            return;
        }
        Boolean voiceMailFlag = callUuidContext.getVoiceMailFlag();
        log.info("[放音收号事件] 企业: {}, uuid: {}, 留言按键: {}", companyCode, uuid, voiceMailFlag);
        // 留言结束收号
        if (Boolean.TRUE.equals(voiceMailFlag)) {
            String voiceMailStopDtmf = callUuidContext.getVoiceMailStopDtmf();
            if (StrUtil.isEmpty(voiceMailStopDtmf)) {
                return;
            }
            if (voiceMailStopDtmf.equals(content)) {
                // 结束录制
                StopRecordDTO stopRecordDTO = StopRecordDTO.builder()
                        .reqId(IdUtil.fastUUID())
                        .companyCode(companyCode)
                        .uuid(uuid)
                        .recordId(callUuidContext.getRecordId())
                        .build();
                freeswitchRequestService.stopRecord(stopRecordDTO);
                callUuidContext.setVoiceMailEndTime(DateUtil.date());
                commonDataOperateService.saveCallUuidContext(callUuidContext);

                // 回到ivr
                CallIvrExitDTO callIvrExitDTO = CallIvrExitDTO.build(IdUtil.fastUUID(), companyCode, uuid);
                freeswitchRequestService.callIvrExit(callIvrExitDTO);
                log.info("[dtmf事件] 企业: {}, 按键: {}, 回到ivr", companyCode, content);
            }
        }
    }

}
