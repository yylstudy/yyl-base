package com.cqt.rabbitmq.util;

import cn.hutool.core.convert.Convert;
import cn.hutool.http.ContentType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * @author linshiqiang
 * date:  2023-02-16 9:23
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RabbitmqUtil {

    private final AmqpAdmin rabbitAdmin;

    private final RabbitTemplate rabbitTemplate;

    /**
     * 创建一对一交换机和队列
     */
    public void createDirectQueueAndExchange(String queueName, String exchangeName) {
        QueueInformation queueInfo = rabbitAdmin.getQueueInfo(queueName);
        if (queueInfo != null) {
            return;
        }
        DirectExchange directExchange = (DirectExchange) getExchange(ExchangeTypes.DIRECT, exchangeName);
        Queue queue = getQueue(queueName);
        rabbitAdmin.declareQueue(queue);
        rabbitAdmin.declareExchange(directExchange);
        rabbitAdmin.declareBinding(BindingBuilder.bind(queue).to(directExchange).withQueueName());
        log.info("create queue: {}, direct exchange: {}, finish", queueName, exchangeName);
    }

    /**
     * 创建广播交换机和队列
     */
    public void createFanoutQueueAndExchange(String queueName, String exchangeName) {
        QueueInformation queueInfo = rabbitAdmin.getQueueInfo(queueName);
        if (queueInfo != null) {
            return;
        }
        FanoutExchange directExchange = (FanoutExchange) getExchange(ExchangeTypes.FANOUT, exchangeName);
        Queue queue = getQueue(queueName);
        rabbitAdmin.declareQueue(queue);
        rabbitAdmin.declareExchange(directExchange);
        rabbitAdmin.declareBinding(BindingBuilder.bind(queue).to(directExchange));
        log.info("create queue: {}, fanout exchange: {}, finish", queueName, exchangeName);
    }

    /**
     * 交换机
     *
     * @param exchangeType 交换机类型 @see ExchangeTypes
     * @param exchangeName 交换机名称
     * @return 交换机
     */
    private Exchange getExchange(String exchangeType, String exchangeName) {
        switch (exchangeType) {
            case ExchangeTypes.DIRECT:
                return ExchangeBuilder.directExchange(exchangeName).durable(true).build();
            case ExchangeTypes.TOPIC:
                return ExchangeBuilder.topicExchange(exchangeName).durable(true).build();
            case ExchangeTypes.FANOUT:
                return ExchangeBuilder.fanoutExchange(exchangeName).durable(true).build();
            case ExchangeTypes.HEADERS:
                return ExchangeBuilder.headersExchange(exchangeName).durable(true).build();
            default:
                throw new RuntimeException("不支持的交换器类型");
        }
    }

    private Queue getQueue(String queueName) {
        return QueueBuilder.durable(queueName).build();
    }

    /**
     * 创建死信交换机和队列
     *
     * @param delayExchange 延迟正常交换机 direct - 消息过期转到这
     * @param delayQueue    延迟正常队列
     * @param deadExchange  死信交换机 - 消息发到这
     * @param deadQueue     死信队列
     * @param duration      延迟时间
     */
    public void createDlxQueueAndExchange(String delayExchange, String delayQueue,
                                          String deadExchange, String deadQueue,
                                          Duration duration) {
        QueueInformation information = rabbitAdmin.getQueueInfo(delayQueue);
        if (information != null) {
            return;
        }
        Map<String, Object> arguments = new HashMap<>(3);
        arguments.put("x-dead-letter-exchange", deadExchange);
        arguments.put("x-dead-letter-routing-key", deadQueue);
        arguments.put("x-message-ttl", duration.toMillis());
        Queue queue = new Queue(delayQueue, true, false, false, arguments);
        QueueInformation queueInfo = rabbitAdmin.getQueueInfo(delayQueue);
        if (queueInfo == null) {
            Queue dlxQueue = new Queue(deadQueue, true, false, false);
            DirectExchange dlxDirectExchange = new DirectExchange(deadExchange);
            rabbitAdmin.declareQueue(dlxQueue);
            rabbitAdmin.declareExchange(dlxDirectExchange);
            rabbitAdmin.declareBinding(BindingBuilder.bind(dlxQueue).to(dlxDirectExchange).withQueueName());
            log.info("创建死信队[{}]列成功", dlxQueue);
        }
        DirectExchange dlxDirectExchange = new DirectExchange(delayExchange);
        rabbitAdmin.declareQueue(queue);
        rabbitAdmin.declareExchange(dlxDirectExchange);
        rabbitAdmin.declareBinding(BindingBuilder.bind(queue).to(dlxDirectExchange).withQueueName());
    }

    public void createDelayExchangeOfPlugin(String delayExchange, String delayQueue) {
        QueueInformation information = rabbitAdmin.getQueueInfo(delayQueue);
        if (information != null) {
            return;
        }
        Queue queue = new Queue(delayQueue, true, false, false);
        rabbitAdmin.declareQueue(queue);
        DirectExchange exchange = new DirectExchange(delayExchange);
        exchange.setDelayed(true);
        rabbitAdmin.declareExchange(exchange);
        rabbitAdmin.declareBinding(BindingBuilder.bind(queue).to(exchange).withQueueName());
    }

    public void sendDelayMessage(String uniqueId, Duration delayTime, String exchange, String routingKey, Object obj) {
        CorrelationData correlationData = new CorrelationData(uniqueId);
        rabbitTemplate.convertAndSend(exchange, routingKey, obj,
                message -> {
                    //设置消息持久化
                    message.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT);
                    message.getMessageProperties().setExpiration(Convert.toStr(delayTime.toMillis()));
                    message.getMessageProperties().setContentType(ContentType.JSON.getValue());
                    return message;
                }, correlationData);
    }

    public void sendDelayMessage(Boolean dlx, String uniqueId, Duration delayTime, String exchange, String routingKey, Object obj) {
        if (dlx) {
            CorrelationData correlationData = new CorrelationData(uniqueId);
            rabbitTemplate.convertAndSend(exchange, routingKey, obj,
                    message -> {
                        //设置消息持久化
                        message.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT);
                        message.getMessageProperties().setExpiration(Convert.toStr(delayTime.toMillis()));
                        message.getMessageProperties().setContentType(ContentType.JSON.getValue());
                        return message;
                    }, correlationData);
            return;
        }
        sendDelayPluginMessage(uniqueId, delayTime, exchange, routingKey, obj);
    }

    public void sendDelayPluginMessage(String uniqueId, Duration delayTime, String exchange, String routingKey, Object obj) {
        CorrelationData correlationData = new CorrelationData(uniqueId);
        rabbitTemplate.convertAndSend(exchange, routingKey, obj,
                message -> {
                    //设置消息持久化
                    message.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT);
                    message.getMessageProperties().setDelay(Convert.toInt(delayTime.toMillis()));
                    message.getMessageProperties().setContentType(ContentType.JSON.getValue());
                    return message;
                }, correlationData);

    }

    public void sendMessage(String exchange, String routingKey, Object message) {
        rabbitTemplate.convertAndSend(exchange, routingKey, message);
    }
}
