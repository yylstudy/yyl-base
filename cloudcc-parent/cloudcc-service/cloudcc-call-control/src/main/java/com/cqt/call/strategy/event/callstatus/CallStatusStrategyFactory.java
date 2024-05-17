package com.cqt.call.strategy.event.callstatus;

import cn.hutool.core.util.StrUtil;
import com.cqt.base.util.CacheUtil;
import com.cqt.model.common.CloudCallCenterProperties;
import com.cqt.model.freeswitch.dto.event.CallStatusEventDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonClient;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author linshiqiang
 * date:  2023-07-03 13:37
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CallStatusStrategyFactory implements CommandLineRunner {

    public static final Map<String, CallStatusStrategy> CALL_STATUS_STRATEGY_MAP = new ConcurrentHashMap<>();

    private final List<CallStatusStrategy> callStatusStrategyList;

    private final RedissonClient redissonClient;

    private final CloudCallCenterProperties cloudCallCenterProperties;

    /**
     * 初始化呼叫状态策略
     */
    @Override
    public void run(String... args) {
        for (CallStatusStrategy callStatusStrategy : callStatusStrategyList) {
            CALL_STATUS_STRATEGY_MAP.put(callStatusStrategy.getCallStatus().name(), callStatusStrategy);
        }
        log.info("初始化呼叫状态策略: {}", CALL_STATUS_STRATEGY_MAP.size());
    }

    /**
     * 处理具体事件
     *
     * @param callStatusEventDTO 呼叫状态事件消息
     */
    public void dealCallStatus(CallStatusEventDTO callStatusEventDTO) {
        CallStatusEventDTO.EventData data = callStatusEventDTO.getData();
        String uuid = callStatusEventDTO.getUuid();
        if (StrUtil.isEmpty(uuid)) {
            log.warn("[呼叫状态事件] data: {}, uuid为空不处理!: ", data);
            return;
        }

        if (Optional.ofNullable(data).isPresent()) {
            String status = data.getStatus();
            // 幂等性
            String idempotentLockKey = CacheUtil.getMessageIdempotentLockKey(uuid, status)
                    + StrUtil.COLON + callStatusEventDTO.getTimestamp();
            Duration idempotent = cloudCallCenterProperties.getBase().getMessageIdempotent();
            boolean absent = redissonClient.getBucket(idempotentLockKey).setIfAbsent(1, idempotent);
            if (!absent) {
                log.warn("[呼叫状态事件] 消息重复已处理过, uuid: {}, status: {}", idempotentLockKey, status);
                return;
            }

            CallStatusStrategy callStatusStrategy = CALL_STATUS_STRATEGY_MAP.get(status);
            if (Optional.ofNullable(callStatusStrategy).isPresent()) {
                try {
                    callStatusStrategy.deal(callStatusEventDTO);
                } catch (Exception e) {
                    log.error("[呼叫状态事件-{}] data: {}, 处理异常: ", status, callStatusEventDTO, e);
                }
                log.info("[呼叫状态事件]: {}, 处理完成", status);
            }
        }
    }
}
