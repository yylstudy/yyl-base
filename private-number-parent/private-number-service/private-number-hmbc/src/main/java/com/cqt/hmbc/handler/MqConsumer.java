package com.cqt.hmbc.handler;

import com.alibaba.fastjson.JSON;
import com.cqt.hmbc.config.RabbitMqConfig;
import com.cqt.hmbc.retry.RetryPushDTO;
import com.cqt.hmbc.retry.RetryQueryDTO;
import com.cqt.hmbc.service.CorpPushService;
import com.cqt.hmbc.service.DialTestResultService;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Consumer
 *
 * @author Xienx
 * @date 2023年02月10日 9:38
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MqConsumer {

    private final CorpPushService corpPushService;
    private final DialTestResultService dialTestResultService;

    @RabbitListener(queues = RabbitMqConfig.HMBC_CDR_QUERY_DELAY_QUEUE)
    public void cdrConsumer(String json, Channel channel, Message message) {
        log.info("接收到话单重查数据: {}", json);
        try {
            // 这里进行重推
            dialTestResultService.retryHandle(JSON.parseObject(json, RetryQueryDTO.class));
            // ack
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            log.error("号码拨测话单延时队列消费异常: ", e);
        }
    }


    @RabbitListener(queues = RabbitMqConfig.HMBC_RESULT_PUSH_DELAY_QUEUE)
    public void pushConsumer(String json, Channel channel, Message message) {
        log.info("接收到拨测结果重推数据: {}", json);
        try {
            // 这里进行重推
            corpPushService.pushWithRetry(JSON.parseObject(json, RetryPushDTO.class));
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            log.error("拨测结果重推延时队列消费异常: ", e);
        }
    }
}
