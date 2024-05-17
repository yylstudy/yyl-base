package com.cqt.hmyc.config.rabbitmq;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.ObjectUtil;
import com.cqt.model.common.properties.HideProperties;
import com.cqt.rabbitmq.util.RabbitmqUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author linshiqiang
 * @date 2021/11/25 14:59
 */
@Configuration
@Slf4j
@AllArgsConstructor
public class RabbitConfig {

    public static final Map<Duration, String> DELAY_QUEUE_MAP = new ConcurrentHashMap<>(64);

    public static final Map<Duration, String> DELAY_EXCHANGE_MAP = new ConcurrentHashMap<>(64);

    private final HideProperties hideProperties;

    private final RabbitmqUtil rabbitmqUtil;

    private static final String SECOND = "s";

    private static final String HOUR = "h";

    private static final String DAY = "d";

    /**
     * 监听使用
     */
    @Bean
    public String[] deadQueues() {
        Map<Duration, String> deadQueues = createDeadQueues(hideProperties.getDelayLevel());
        return Convert.toStrArray(deadQueues.values());
    }

    /**
     * 自动创建绑定延时队列
     */
    @PostConstruct
    @SuppressWarnings("all")
    public void createQueue() {
        HideProperties.DelayLevel delayLevel = hideProperties.getDelayLevel();
        try {
            Map<Duration, String> deadQueues = createDeadQueues(delayLevel);
            for (Map.Entry<Duration, String> entry : deadQueues.entrySet()) {
                Duration duration = entry.getKey();
                String deadQueue = entry.getValue();
                String deadExchange = deadQueue.replace("queue", "exchange");
                String delayQueue = deadQueue.replace("dead", "delay");
                String delayExchange = deadExchange.replace("dead", "delay");
                DELAY_QUEUE_MAP.put(duration, delayQueue);
                DELAY_EXCHANGE_MAP.put(duration, delayExchange);
                rabbitmqUtil.createDlxQueueAndExchange(delayExchange, delayQueue,
                        deadExchange, deadQueue,
                        duration);
            }

        } catch (Exception e) {
            log.error("自动创建死信队列和交换机异常: ", e);
        }
        log.info("初始化 delayQueue: {}", DELAY_QUEUE_MAP.size());
        log.info("初始化 delayExchange: {}", DELAY_EXCHANGE_MAP.size());

        // 绑定关系数据库连接异常时, 无法入库时 推送到死信队列重试
        rabbitmqUtil.createDlxQueueAndExchange(
                RabbitMqConfig.BIND_DB_ERROR_DELAY_EXCHANGE,
                RabbitMqConfig.BIND_DB_ERROR_DELAY_QUEUE,
                RabbitMqConfig.BIND_DB_ERROR_DEAD_EXCHANGE,
                RabbitMqConfig.BIND_DB_ERROR_DEAD_QUEUE,
                Duration.ofMinutes(10));
    }

    private Map<Duration, String> createDeadQueues(HideProperties.DelayLevel delayLevel) {
        Map<Duration, String> map = new HashMap<>(16);
        // 10s-120s,1h-30h,2d-30d
        Duration minSecond = delayLevel.getMinSecond();
        Duration maxSecond = delayLevel.getMaxSecond();
        if (ObjectUtil.isNotEmpty(minSecond) && ObjectUtil.isNotEmpty(maxSecond)) {
            for (long i = minSecond.getSeconds() / 10; i <= maxSecond.getSeconds() / 10; i++) {
                map.put(Duration.ofSeconds(i * 10L), String.format(DelayRabbitMqConfig.RECYCLE_QUEUE_DEAD, i * 10 + SECOND));
            }
        }

        Duration minHour = delayLevel.getMinHour();
        Duration maxHour = delayLevel.getMaxHour();
        if (ObjectUtil.isNotEmpty(minHour) && ObjectUtil.isNotEmpty(maxHour)) {
            for (long i = minHour.toHours(); i <= maxHour.toHours(); i++) {
                map.put(Duration.ofHours(i), String.format(DelayRabbitMqConfig.RECYCLE_QUEUE_DEAD, i + HOUR));
            }
        }

        Duration minDay = delayLevel.getMinDay();
        Duration maxDay = delayLevel.getMaxDay();
        if (ObjectUtil.isNotEmpty(minHour) && ObjectUtil.isNotEmpty(maxHour)) {
            for (long i = minDay.toDays(); i <= maxDay.toDays(); i++) {
                map.put(Duration.ofDays(i), String.format(DelayRabbitMqConfig.RECYCLE_QUEUE_DEAD, i + DAY));
            }
        }
        return map;
    }

    public static String getDelayQueueName(Duration duration) {

        return DELAY_QUEUE_MAP.get(duration);
    }

    public static String getDelayExchangeName(Duration duration) {

        return DELAY_EXCHANGE_MAP.get(duration);
    }

}
