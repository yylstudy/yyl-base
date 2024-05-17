package com.cqt.call.strategy.event.impl;

import com.cqt.base.enums.EventEnum;
import com.cqt.base.enums.cdr.HangupCauseEnum;
import com.cqt.base.util.CacheUtil;
import com.cqt.call.config.ThreadPoolConfig;
import com.cqt.call.strategy.event.EventStrategy;
import com.cqt.cloudcc.manager.service.CommonDataOperateService;
import com.cqt.cloudcc.manager.service.FreeswitchRequestService;
import com.cqt.model.agent.vo.CallUuidContext;
import com.cqt.model.freeswitch.dto.api.HangupDTO;
import com.cqt.model.freeswitch.dto.event.PlayStatusEventDTO;
import com.cqt.starter.redis.util.RedissonUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * @author linshiqiang
 * date:  2023-07-03 11:48
 * <pre>
 * {
 *   "server_id": "test-acd01",
 *   "company_code": "000006",
 *   "service_code": "cloudcc",
 *   "uuid": "6dd04eaf-1c0e-461f-8d2d-c0a9de50f197",
 *   "timestamp": 1700807497972,
 *   "event": "play_status",
 *   "data": {
 *     "req_id": "02366f48-d72a-4edb-8ad6-0dba5fbf2948",
 *     "caller_number": "15010071007",
 *     "callee_number": "059170007002",
 *     "status": "start"
 *   }
 * }
 * </pre>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PlayStatusEventStrategyImpl implements EventStrategy {

    private final ObjectMapper objectMapper;

    private final RedissonUtil redissonUtil;

    private final CommonDataOperateService commonDataOperateService;

    private final FreeswitchRequestService freeswitchRequestService;

    @Async(ThreadPoolConfig.BASE_POOL_NAME)
    @Override
    public void deal(String message) throws JsonProcessingException {
        log.info("[放音状态] message: {}", message);
        PlayStatusEventDTO playStatusEventDTO = objectMapper.readValue(message, PlayStatusEventDTO.class);
        String status = playStatusEventDTO.getData().getStatus();
        String companyCode = playStatusEventDTO.getCompanyCode();
        String uuid = playStatusEventDTO.getUuid();
        String playbackFlagKey = CacheUtil.getPlaybackFlagKey(companyCode, uuid);
        if ("end".equals(status)) {
            redissonUtil.delKey(playbackFlagKey);
            CallUuidContext callUuidContext = commonDataOperateService.getCallUuidContext(companyCode, uuid);
            Boolean hangupAfterPlayback = callUuidContext.getCurrent().getHangupAfterPlayback();
            if (Boolean.TRUE.equals(hangupAfterPlayback)) {
                HangupDTO hangupDTO = HangupDTO.build(companyCode, uuid, HangupCauseEnum.PLAY_RECORD_END_HANGUP);
                freeswitchRequestService.hangup(hangupDTO);
            }
        }
    }

    @Override
    public EventEnum getEventType() {
        return EventEnum.play_status;
    }
}
