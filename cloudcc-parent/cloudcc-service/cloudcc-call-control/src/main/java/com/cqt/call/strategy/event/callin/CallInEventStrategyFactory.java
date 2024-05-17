package com.cqt.call.strategy.event.callin;

import com.cqt.model.freeswitch.dto.event.CallInEventDTO;
import com.cqt.model.number.entity.NumberInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author linshiqiang
 * date:  2023-07-21 10:24
 * 呼入策略工厂
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CallInEventStrategyFactory implements CommandLineRunner {

    public static final Map<Integer, CallInEventStrategy> STRATEGY_MAP = new ConcurrentHashMap<>();

    private final List<CallInEventStrategy> callInEventStrategyList;

    /**
     * 初始化呼入策略
     */
    @Override
    public void run(String... args) {
        for (CallInEventStrategy strategy : callInEventStrategyList) {
            STRATEGY_MAP.put(strategy.getCallInStrategy().getCode(), strategy);
        }
        log.info("初始化呼入策略: {}", STRATEGY_MAP.size());
    }

    /**
     * 执行
     */
    public Boolean deal(Integer serviceWay, CallInEventDTO callInEventDTO, NumberInfo numberInfo) throws Exception {
        try {
            CallInEventStrategy strategy = STRATEGY_MAP.get(serviceWay);
            if (Optional.ofNullable(strategy).isPresent()) {
                return strategy.execute(callInEventDTO, numberInfo);
            }
            log.warn("[呼入策略] 服务方式: {}, 未找到实现", serviceWay);
            return false;
        } catch (Exception e) {
            log.error("[呼入策略] 服务方式: {}, 异常", serviceWay, e);
        }
        return false;
    }
}
