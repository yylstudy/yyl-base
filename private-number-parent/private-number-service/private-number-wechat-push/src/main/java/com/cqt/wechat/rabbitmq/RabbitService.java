package com.cqt.wechat.rabbitmq;

import com.cqt.wechat.config.RabbitMqConfig;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 队列新增监听
 *
 * @author hlx
 * @date 2022-04-06
 */
@Component
public class RabbitService {

    @Autowired
    private SimpleMessageListenerContainer simpleMessageListenerContainer;

    @Autowired
    private RabbitAdmin rabbitAdmin;

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
        QUEUE_MAP.put(queueName, RabbitMqConfig.PRIVATE_PUSH_EXCHANGE);

        // 新增队列
        Queue queue = new Queue(queueName);
        DirectExchange directExchange = new DirectExchange(RabbitMqConfig.PRIVATE_PUSH_EXCHANGE, true, true);
        directExchange.setDelayed(true);
        directExchange.isAutoDelete();
        rabbitAdmin.declareQueue(queue);
        rabbitAdmin.declareExchange(directExchange);
        rabbitAdmin.declareBinding(BindingBuilder.bind(queue).to(directExchange).with(queueName));

        // 新增监听
        simpleMessageListenerContainer.addQueueNames(queueName);
    }

}
