package com.cqt.call.strategy.event.impl;

import com.cqt.base.enums.EventEnum;
import com.cqt.call.strategy.event.EventStrategy;
import com.cqt.call.strategy.event.callstatus.CallStatusStrategyFactory;
import com.cqt.model.freeswitch.dto.event.CallStatusEventDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author linshiqiang
 * date:  2023-07-03 11:48
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CallStatusEventStrategyImpl implements EventStrategy {

    private final ObjectMapper objectMapper;

    private final CallStatusStrategyFactory callStatusStrategyFactory;

    @Override
    public void deal(String message) throws Exception {
        CallStatusEventDTO callStatusEventDTO = objectMapper.readValue(message, CallStatusEventDTO.class);
        String status = callStatusEventDTO.getData().getStatus();
        if (log.isInfoEnabled()) {
            log.info("[通话状态事件-{}] 消息: {}", status, message);
        }
        callStatusStrategyFactory.dealCallStatus(callStatusEventDTO);
    }

    @Override
    public EventEnum getEventType() {
        return EventEnum.call_status;
    }
}
