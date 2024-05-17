package com.cqt.sms.rabbitmq;


import com.cqt.sms.config.RabbitMqConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 往队列发送数据
 *
 * @author zhengsuhao
 * @date 2023-02-11
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RabbitMqService {

    private final SimpleMessageListenerContainer simpleMessageListenerContainer;
    private final RabbitAdmin rabbitAdmin;

    /**
     * 记录队列监听情况 key : queueName   value: exchangeName
     */
    private static final Map<String, String> QUEUE_MAP = new ConcurrentHashMap<>(16);

    /**
     * 新增企业队列
     *
     * @param queueName 队列名
     */
    public void createQueue(String queueName) {
        if (QUEUE_MAP.containsKey(queueName)) {
            return;
        }
        DirectExchange directExchange;
        if (queueName.contains("ali-sms-push-fail-delay-queue")) {
            QUEUE_MAP.put(queueName, RabbitMqConfig.ALI_SMS_PUSH_EXCHANGE);
            directExchange = new DirectExchange(RabbitMqConfig.ALI_SMS_PUSH_EXCHANGE, true, true);

        } else {
            QUEUE_MAP.put(queueName, RabbitMqConfig.ALI_SMS_INTERCEPT_EXCHANGE);
            directExchange = new DirectExchange(RabbitMqConfig.ALI_SMS_INTERCEPT_EXCHANGE, true, true);
        }

        // 新增队列
        Queue queue = new Queue(queueName);
        directExchange.setDelayed(true);
        directExchange.isAutoDelete();
        rabbitAdmin.declareQueue(queue);
        rabbitAdmin.declareExchange(directExchange);
        rabbitAdmin.declareBinding(BindingBuilder.bind(queue).to(directExchange).with(queueName));

        // 新增监听
        simpleMessageListenerContainer.addQueueNames(queueName);
    }

}
