package com.cqt.sms.rabbitmq;

import cn.hutool.core.util.IdUtil;
import com.cqt.model.push.properties.PushProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @Description: mq生产者
 * @author: scott
 * @date: 2022年03月28日 13:34
 */
@Slf4j
@Component
public class MqProducer {

    private final RabbitTemplate rabbitTemplate;
    private final PushProperties pushProperties;
    @Autowired
    public MqProducer(RabbitTemplate rabbitTemplate, PushProperties pushProperties) {
        this.rabbitTemplate = rabbitTemplate;
        this.pushProperties = pushProperties;
    }

    /**
     * confirmCallback returnCallback
     */
    final RabbitTemplate.ConfirmCallback confirmCallback = (correlationData, ack, cause) -> {
        log.info("correlationData: {}, ack: {}", correlationData, ack);
        if (!ack) {
            log.info("需要异常处理。。。");
        }
    };

    final RabbitTemplate.ReturnCallback returnCallback = (message, replyCode, replyText, exchange, routingKey)
            -> log.info("return exchange: {}, routingKey: {}, replyCode: {}, replyText: {}"
            , exchange, routingKey, replyCode, replyText);

    /**
     * 延时队列发送
     * @param exchange mq交换机
     * @param routingKey mq路由
     * @param msg 消息内容
     * */
    public void sendLazy(String exchange, String routingKey, Object msg) {
        rabbitTemplate.setMandatory(false);
        rabbitTemplate.setConfirmCallback(confirmCallback);
        rabbitTemplate.setReturnCallback(returnCallback);
        //uuid 全局唯一
        CorrelationData correlationData = new CorrelationData(IdUtil.objectId());

        //发送消息时指定 header 延迟时间
        rabbitTemplate.convertAndSend(exchange, routingKey, msg,
                message -> {
                    //设置消息持久化
                    message.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT);
                    //设置消息延迟时间, 单位s
                    message.getMessageProperties().setDelay(pushProperties.getRetryMinute() * 60 * 1000);
                    return message;
                }, correlationData);
    }
}
