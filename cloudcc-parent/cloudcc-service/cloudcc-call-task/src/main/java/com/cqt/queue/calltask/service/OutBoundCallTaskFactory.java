package com.cqt.queue.calltask.service;

import com.alibaba.fastjson.JSON;
import com.cqt.base.exception.BizException;
import com.cqt.base.model.ResultVO;
import com.cqt.model.calltask.dto.CallTaskOperateDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author linshiqiang
 * date:  2023-10-25 14:45
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OutBoundCallTaskFactory implements CommandLineRunner {

    private static final Map<String, OutboundCallTaskStrategy> STRATEGY_MAP = new HashMap<>(8);

    private final List<OutboundCallTaskStrategy> strategyList;

    @Override
    public void run(String... args) {
        for (OutboundCallTaskStrategy strategy : strategyList) {
            STRATEGY_MAP.put(strategy.getCallTaskEnum().name(), strategy);
        }
        log.info("初始化外呼任务策略: {}", STRATEGY_MAP.size());
    }

    private OutboundCallTaskStrategy getStrategy(CallTaskOperateDTO callTaskOperateDTO) {
        if (log.isInfoEnabled()) {
            log.info("[外呼任务请求] params: {}", JSON.toJSONString(callTaskOperateDTO));
        }
        OutboundCallTaskStrategy strategy = STRATEGY_MAP.get(callTaskOperateDTO.getTaskType());
        if (Objects.isNull(strategy)) {
            throw new BizException("任务类型不支持!");
        }
        return strategy;
    }

    public ResultVO<Integer> addTask(CallTaskOperateDTO callTaskOperateDTO) {
        OutboundCallTaskStrategy strategy = getStrategy(callTaskOperateDTO);
        log.info("[out task] addTask");
        return strategy.addTask(callTaskOperateDTO);
    }

    public ResultVO<Void> startTask(CallTaskOperateDTO callTaskOperateDTO) {
        OutboundCallTaskStrategy strategy = getStrategy(callTaskOperateDTO);
        log.info("[out task] startTask");
        return strategy.startTask(callTaskOperateDTO);
    }

    public ResultVO<Void> stopTask(CallTaskOperateDTO callTaskOperateDTO) {
        OutboundCallTaskStrategy strategy = getStrategy(callTaskOperateDTO);
        log.info("[out task] stopTask");
        return strategy.stopTask(callTaskOperateDTO);
    }

    public ResultVO<Void> delTask(CallTaskOperateDTO callTaskOperateDTO) {
        OutboundCallTaskStrategy strategy = getStrategy(callTaskOperateDTO);
        log.info("[out task] delTask");
        return strategy.delTask(callTaskOperateDTO);
    }

    public ResultVO<Void> updateTask(CallTaskOperateDTO callTaskOperateDTO) {
        OutboundCallTaskStrategy strategy = getStrategy(callTaskOperateDTO);
        log.info("[out task] updateTask");
        return strategy.updateTask(callTaskOperateDTO);
    }
}
