package com.cqt.queue.callin.service.action.impl;

import com.cqt.base.enums.CallInIvrActionEnum;
import com.cqt.base.model.ResultVO;
import com.cqt.model.queue.dto.CallInIvrActionDTO;
import com.cqt.model.queue.vo.CallInIvrActionVO;
import com.cqt.queue.callin.service.action.CallInIvrStrategy;
import com.cqt.rpc.queue.QueueControlRemoteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author linshiqiang
 * date:  2023-08-21 10:26
 * 排队
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class QueueUpCallInIvrStrategyImpl implements CallInIvrStrategy {

    private final QueueControlRemoteService queueControlRemoteService;

    @Override
    public CallInIvrActionEnum getAction() {
        return CallInIvrActionEnum.IVR_QUEUE_UP;
    }

    @Override
    public ResultVO<CallInIvrActionVO> execute(CallInIvrActionDTO callInIvrActionDTO) throws Exception {
        // type=1 ivr服务 闲时策略
        return queueControlRemoteService.distributeAgent(callInIvrActionDTO);
    }
}
