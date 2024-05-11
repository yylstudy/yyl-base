package com.linkcircle.mq.producer;

import com.linkcircle.mq.common.MqException;
import com.linkcircle.mq.common.RabbitDestination;
import com.linkcircle.mq.common.RocketmqLocalTransactionState;
import com.linkcircle.mq.common.RocketmqSendCallback;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author yang.yonglian
 * @version 1.0.0
 * @Description TODO
 * @createTime 2024/5/6 14:27
 */
@Slf4j
public class RabbitMqProducer implements MqProducer{
    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Override
    public void asyncSendMessage(String destination, Object payload) {
        RabbitDestination rabbitDestination = checkAndGetDestination(destination);
        rabbitTemplate.convertAndSend(rabbitDestination.getExchange(),rabbitDestination.getRoutingKey(),payload);
    }

    @Override
    public void asyncSendMessage(String destination, Object payload, String key) {
        RabbitDestination rabbitDestination = checkAndGetDestination(destination);
        CorrelationData correlationData = new CorrelationData(key);
        rabbitTemplate.convertAndSend(rabbitDestination.getExchange(),rabbitDestination.getRoutingKey(),payload,correlationData);
    }

    @Override
    public void asyncSendDelayMessage(String destination, Object payload, int delay) {
        RabbitDestination rabbitDestination = checkAndGetDestination(destination);
        rabbitTemplate.convertAndSend(rabbitDestination.getExchange(), rabbitDestination.getRoutingKey(), payload,
                message -> {
                    message.getMessageProperties().setDelay(delay);
                    return message;
                }
        );
    }

    @Override
    public void asyncSendDelayMessage(String destination, Object payload, int delay, String key) {
        RabbitDestination rabbitDestination = checkAndGetDestination(destination);
        CorrelationData correlationData = new CorrelationData(key);
        rabbitTemplate.convertAndSend(rabbitDestination.getExchange(), rabbitDestination.getRoutingKey(), payload,
                message -> {
                    message.getMessageProperties().setDelay(delay);
                    return message;
                }
        ,correlationData);
    }

    @Override
    public void asyncSendExpireMessage(String destination, Object payload, int expire) {
        RabbitDestination rabbitDestination = checkAndGetDestination(destination);
        rabbitTemplate.convertAndSend(rabbitDestination.getExchange(), rabbitDestination.getRoutingKey(), payload,
                message -> {
                    message.getMessageProperties().setExpiration(String.valueOf(expire));
                    return message;
                }
        );
    }

    @Override
    public void asyncSendExpireMessage(String destination, Object payload, int expire, String key) {
        RabbitDestination rabbitDestination = checkAndGetDestination(destination);
        CorrelationData correlationData = new CorrelationData(key);
        rabbitTemplate.convertAndSend(rabbitDestination.getExchange(), rabbitDestination.getRoutingKey(), payload,
                message -> {
                    message.getMessageProperties().setExpiration(String.valueOf(expire));
                    return message;
                }
                ,correlationData);
    }

    @Override
    public void asyncSendMessage(String destination, Object payload, Integer delay, String key, RocketmqSendCallback sendCallback) {
        throw new MqException("rabbitmq暂时不支持自定义回调");
    }

    @Override
    public boolean syncSendMessage(String destination, Object payload) {
        throw new MqException("rabbitmq暂时不支持同步发送");
    }

    @Override
    public boolean syncSendMessage(String destination, Object payload, String key) {
        throw new MqException("rabbitmq暂时不支持同步发送");
    }


    @Override
    public boolean syncSendDelayMessage(String destination, Object payload, int delay) {
        throw new MqException("rabbitmq暂时不支持同步发送");
    }

    @Override
    public boolean syncSendDelayMessage(String destination, Object payload, int delay, String key) {
        throw new MqException("rabbitmq暂时不支持同步发送");
    }


    @Override
    public RocketmqLocalTransactionState sendTransactionMessage(String destination, Object payload, String key, Object args) {
        throw new MqException("rabbitmq暂时不支持事务消息");
    }

    @Override
    public boolean syncSendOrderlyMessage(String destination, Object payload, String hashKey) {
        throw new MqException("rabbitmq暂时不支持顺序消息");
    }

    @Override
    public boolean syncSendOrderlyMessage(String destination, Object payload, String hashKey, String key) {
        throw new MqException("rabbitmq暂时不支持顺序消息");
    }

    @Override
    public void asyncSendOrderlyMessage(String destination, Object payload, String hashKey) {
        throw new MqException("rabbitmq暂时不支持顺序消息");
    }

    @Override
    public void asyncSendOrderlyMessage(String destination, Object payload, String hashKey, String key, RocketmqSendCallback rocketmqSendCallback) {
        throw new MqException("rabbitmq暂时不支持顺序消息");
    }

    private RabbitDestination checkAndGetDestination(String destination){
        if(StringUtils.isEmpty(destination)){
            throw new MqException("消息投递地址为空");
        }
        String[] destinations = destination.split(":");
        if(destinations.length>2){
            throw new MqException("消息投递地址不合法");
        }
        RabbitDestination rabbitDestination = new RabbitDestination();
        rabbitDestination.setExchange(destinations[0]);
        if(destinations.length>1){
            rabbitDestination.setRoutingKey(destinations[1]);
        }
        return rabbitDestination;
    }
}
