package com.cqt.call.strategy.event;

import cn.hutool.core.util.StrUtil;
import com.cqt.model.freeswitch.base.FreeswitchEventBase;
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
 * date:  2023-07-03 13:33
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EventStrategyFactory implements CommandLineRunner {

    public static final Map<String, EventStrategy> EVENT_STRATEGY_MAP = new ConcurrentHashMap<>();

    private final List<EventStrategy> eventStrategyList;

    /**
     * 初始化事件策略
     */
    @Override
    public void run(String... args) {
        for (EventStrategy eventStrategy : eventStrategyList) {
            EVENT_STRATEGY_MAP.put(eventStrategy.getEventType().name(), eventStrategy);
        }
        log.info("初始化事件策略: {}", EVENT_STRATEGY_MAP.size());
    }

    /**
     * 处理具体事件
     *
     * @param message 事件消息
     */
    public void dealEvent(FreeswitchEventBase eventBase, String message) throws Exception {
        if (StrUtil.isEmpty(eventBase.getEvent())) {
            return;
        }
        // 解析消息获取event字段
        EventStrategy eventStrategy = EVENT_STRATEGY_MAP.get(eventBase.getEvent());
        if (Optional.ofNullable(eventStrategy).isPresent()) {
            eventStrategy.deal(message);
        }
    }

}
