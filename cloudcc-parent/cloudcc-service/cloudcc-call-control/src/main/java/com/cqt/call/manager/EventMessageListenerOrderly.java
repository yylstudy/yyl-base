package com.cqt.call.manager;

import cn.hutool.core.date.DateUtil;
import com.cqt.base.util.TraceIdUtil;
import com.cqt.call.strategy.event.EventStrategyFactory;
import com.cqt.model.common.CloudCallCenterProperties;
import com.cqt.model.freeswitch.base.FreeswitchEventBase;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.consumer.listener.ConsumeOrderlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeOrderlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerOrderly;
import org.apache.rocketmq.common.message.MessageExt;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;

/**
 * @author linshiqiang
 * date:  2023-11-14 14:42
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EventMessageListenerOrderly implements MessageListenerOrderly {

    private final ObjectMapper objectMapper;

    private final EventStrategyFactory eventStrategyFactory;

    private final CloudCallCenterProperties cloudCallCenterProperties;

    /**
     * Suspending pulling time in orderly mode.
     * <p>
     * The minimum value is 10 and the maximum is 30000.
     */
    private long suspendCurrentQueueTimeMillis = 1000;

    @Override
    public ConsumeOrderlyStatus consumeMessage(List<MessageExt> msgs, ConsumeOrderlyContext context) {
        log.debug("msg size: {}", msgs.size());
        for (MessageExt messageExt : msgs) {
            log.debug("received msg: {}", messageExt);
            try {
                handleMessage(messageExt);
            } catch (Exception e) {
                log.warn("consume message failed. messageId:{}, topic:{}, reconsumeTimes:{}",
                        messageExt.getMsgId(), messageExt.getTopic(), messageExt.getReconsumeTimes(), e);
                context.setSuspendCurrentQueueTimeMillis(suspendCurrentQueueTimeMillis);
                return ConsumeOrderlyStatus.SUSPEND_CURRENT_QUEUE_A_MOMENT;
            }
        }

        return ConsumeOrderlyStatus.SUCCESS;
    }

    private void handleMessage(MessageExt messageExt) {
        String message = new String(messageExt.getBody());
        try {
            FreeswitchEventBase base = objectMapper.readValue(message, FreeswitchEventBase.class);
            // 事件时间大于 x min的消息不处理
            Duration messageTimeout = cloudCallCenterProperties.getBase().getMessageTimeout();
            Long timestamp = base.getTimestamp();
            if ((System.currentTimeMillis() - timestamp) > messageTimeout.toMillis()) {
                log.warn("[MQ事件-{}] companyCode: {}, uuid: {}, time: {}, message expired",
                        base.getEvent(), base.getCompanyCode(), base.getTimestamp(), base.getEvent());
                return;
            }
            String traceId = TraceIdUtil.buildTraceId(base.getUuid(), base.getEvent(), base.getCompanyCode(), timestamp);
            TraceIdUtil.setTraceId(traceId);
            long now = System.currentTimeMillis();
            long delay = now - timestamp;
            if (delay > 1000) {
                log.warn("base date: {}, delay: {}", DateUtil.formatDateTime(DateUtil.date(timestamp)), delay);
            }
            eventStrategyFactory.dealEvent(base, message);
            long costTime = System.currentTimeMillis() - now;
            if (costTime > 200) {
                log.warn("event consume {} cost: {} ms", messageExt.getMsgId(), costTime);
            }
        } catch (Exception e) {
            log.error("[MQ事件] 消息: {}, 消费异常: {}", message, e);
        } finally {
            TraceIdUtil.remove();
        }
    }
}
