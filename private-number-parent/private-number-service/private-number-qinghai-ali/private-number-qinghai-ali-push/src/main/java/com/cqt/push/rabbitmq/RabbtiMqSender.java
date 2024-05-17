package com.cqt.push.rabbitmq;

import com.cqt.push.config.RabbitMqConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * @author zhengsuhao
 * @date 2023-02-15
 */
@Component
@Slf4j
public class RabbtiMqSender {

    @Autowired
    private RabbitTemplate rabbitTemplate;


    final RabbitTemplate.ConfirmCallback confirmCallback = new RabbitTemplate.ConfirmCallback() {
        @Override
        public void confirm(CorrelationData correlationData, boolean ack, String cause) {
            log.info("correlationData: {}, ack: {}", correlationData, ack);
            if (!ack) {
                log.info("需要异常处理。。。");
            }
        }
    };

    final RabbitTemplate.ReturnCallback returnCallback = new RabbitTemplate.ReturnCallback() {
        @Override
        public void returnedMessage(Message message, int replyCode, String replyText, String exchange, String routingKey) {
            log.info("return exchange: {}, routingKey: {}, replyCode: {}, replyText: {}"
                    , exchange, routingKey, replyCode, replyText);
        }
    };

    public void sendLazy(Object message, String queueName,Integer rePushTime) {
        rabbitTemplate.setMandatory(false);
        rabbitTemplate.setConfirmCallback(confirmCallback);
        rabbitTemplate.setReturnCallback(returnCallback);
        //uuid 全局唯一
        CorrelationData correlationData = new CorrelationData(UUID.randomUUID().toString());
        String pushExchange;
        if (queueName.contains("ali-call-list-push-fail-delay-queue")) {
            pushExchange = RabbitMqConfig.ALI_CALL_LIST_PUSH_EXCHANGE;
        } else {
            pushExchange = RabbitMqConfig.ALI_CALL_EVENT_PUSH_EXCHANGE;
        }
        //发送消息时指定 header 延迟时间
        rabbitTemplate.convertAndSend(pushExchange, queueName, message,
                new MessagePostProcessor() {
                    @Override
                    public Message postProcessMessage(Message message) throws AmqpException {
                        //设置消息持久化
                        message.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT);
                        //message.getMessageProperties().setHeader("x-delay", "6000");
                        message.getMessageProperties().setDelay(rePushTime * 60 * 1000);
                        return message;
                    }
                }, correlationData);
    }
}
