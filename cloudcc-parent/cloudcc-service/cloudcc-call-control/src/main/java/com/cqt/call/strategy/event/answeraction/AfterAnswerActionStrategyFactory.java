package com.cqt.call.strategy.event.answeraction;

import com.cqt.base.enums.OriginateAfterActionEnum;
import com.cqt.model.agent.vo.CallUuidContext;
import com.cqt.model.common.CloudCallCenterProperties;
import com.cqt.model.freeswitch.dto.event.CallStatusEventDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonClient;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author linshiqiang
 * date:  2023-10-20 9:30
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AfterAnswerActionStrategyFactory implements CommandLineRunner {

    public static final Map<OriginateAfterActionEnum, AfterAnswerActionStrategy> STRATEGY_MAP = new ConcurrentHashMap<>();

    private final List<AfterAnswerActionStrategy> afterAnswerActionStrategyList;

    private final RedissonClient redissonClient;

    private final CloudCallCenterProperties cloudCallCenterProperties;

    /**
     * 初始化接通后执行动作策略
     */
    @Override
    public void run(String... args) {
        for (AfterAnswerActionStrategy afterAnswerActionStrategy : afterAnswerActionStrategyList) {
            STRATEGY_MAP.put(afterAnswerActionStrategy.getOriginateAfterAction(), afterAnswerActionStrategy);
        }
        log.info("初始化-接通后执行动作策略: {}", STRATEGY_MAP.size());
    }

    /**
     * 执行动作
     *
     * @param callStatusEventDTO 呼叫时间
     * @param callUuidContext    当前uuid上下文
     */
    public void execute(CallStatusEventDTO callStatusEventDTO, CallUuidContext callUuidContext) {
        OriginateAfterActionEnum originateAfterAction = callUuidContext.getOriginateAfterAction();
        if (Objects.isNull(originateAfterAction)) {
            return;
        }
        AfterAnswerActionStrategy strategy = STRATEGY_MAP.get(originateAfterAction);
        if (Objects.nonNull(strategy)) {
            strategy.execute(callStatusEventDTO, callUuidContext);
        }
    }

}
