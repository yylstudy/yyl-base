package com.cqt.hmyc.web.bind.manager;

import cn.hutool.core.util.ObjectUtil;
import com.cqt.hmyc.config.rabbitmq.RabbitConfig;
import com.cqt.hmyc.web.bind.service.recycle.NumberRecycleService;
import com.cqt.model.bind.dto.BindRecycleDTO;
import com.cqt.model.common.properties.HideProperties;
import com.cqt.rabbitmq.util.RabbitmqUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;

/**
 * @author linshiqiang
 * @date 2021/9/12 14:57
 */
@Component
@Slf4j
@AllArgsConstructor
public class PrivateMqProducer {

    private final RabbitmqUtil rabbitmqUtil;

    private final NumberRecycleService numberRecycleService;

    private final HideProperties hideProperties;

    public void sendMessage(String exchange, String routingKey, Object message) {
        rabbitmqUtil.sendMessage(exchange, routingKey, message);
    }

    @Retryable(value = AmqpException.class, maxAttempts = 2, backoff = @Backoff(delay = 2000L, multiplier = 1.5))
    public void sendLazy(BindRecycleDTO bindRecycleDTO, Integer delay, String exchange, String queue, String type) {
        String bindId = bindRecycleDTO.getBindId();

        // 判断下延时过长的数据不推mq
        Duration delayThreshold = hideProperties.getDelayLevel().getDelayThreshold();
        if (ObjectUtil.isNotEmpty(delayThreshold)) {
            if (delay > delayThreshold.getSeconds()) {
                log.info("{} bindId: {},  expiration: {}, 消息延时过长, 不推送mq.", type, bindId, delay);
                return;
            }
        }

        // 延迟几秒发送mq, 要比redis绑定关系过期之后再回收
        Integer delayTimeout = hideProperties.getDelayTimeout();
        int newDelay = delayTimeout + delay;

        // 根据延时时间(秒) 查找该送到哪个队列
        Optional<Duration> durationOptional = getDeadQueue(newDelay);
        // 发送死信队列
        if (durationOptional.isPresent()) {
            Duration duration = durationOptional.get();
            String delayQueueName = RabbitConfig.getDelayQueueName(duration);
            rabbitmqUtil.sendDelayMessage(bindId,
                    Duration.ofSeconds(newDelay),
                    RabbitConfig.getDelayExchangeName(duration),
                    delayQueueName,
                    bindRecycleDTO);
            log.info("{} bindId: {}, expiration: {}, mq延时-死信: {}, 消息发送完成.", type, bindId, newDelay, delayQueueName);
            return;
        }

        //发送延时插件消息
        rabbitmqUtil.sendDelayPluginMessage(bindId,
                Duration.ofSeconds(newDelay),
                exchange,
                queue,
                bindRecycleDTO);
        log.info("{} bindId: {},  expiration: {}, mq延时插件消息发送完成.", type, bindId, newDelay);
    }

    /**
     * 根据延时时间(秒) 查找该送到哪个队列
     *
     * @param delay 延时时间(秒)
     * @return 队列名称
     */
    private Optional<Duration> getDeadQueue(Integer delay) {
        HideProperties.DelayLevel delayLevel = hideProperties.getDelayLevel();
        Duration minSecond = delayLevel.getMinSecond();
        Duration maxSecond = delayLevel.getMaxSecond();
        if (delay >= minSecond.getSeconds() && delay <= maxSecond.getSeconds()) {
            int index = delay % 10 == 0 ? delay / 10 : delay / 10 + 1;
            return Optional.of(Duration.ofSeconds(index * 10L));
        }

        Duration minHour = delayLevel.getMinHour();
        Duration maxHour = delayLevel.getMaxHour();
        if (delay >= minHour.getSeconds() && delay <= maxHour.getSeconds()) {
            int index = delay % 3600 == 0 ? delay / 3600 : delay / 3600 + 1;
            return Optional.of(Duration.ofHours(index));
        }

        Duration minDay = delayLevel.getMinDay();
        Duration maxDay = delayLevel.getMaxDay();
        if (delay >= minDay.getSeconds() && delay <= maxDay.getSeconds()) {
            int index = delay % 86400 == 0 ? delay / 86400 : delay / 86400 + 1;
            return Optional.of(Duration.ofDays(index));
        }
        return Optional.empty();
    }

    @Recover
    public void savePushFail(Throwable e, BindRecycleDTO bindRecycleDTO, Integer delay, String exchange, String queue, String typ) throws ArithmeticException {
        log.error("队列: {}, 号码回收推mq异常, 数据: {}, 异常: {}", queue, bindRecycleDTO, e.getMessage());
        // 保存到推送失败表 mt_recycle_push_fail, 再定时任务
        numberRecycleService.savePushFail(bindRecycleDTO);
    }


}
