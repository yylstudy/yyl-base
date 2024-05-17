package com.cqt.thirdchinanet.rabbitmq;

import com.cqt.model.push.properties.PushProperties;
import com.cqt.thirdchinanet.config.RabbitMqConfig;
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
 * @author hlx
 */
@Component
@Slf4j
public class MqSender {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private PushProperties pushProperties;

    /**
     * confirmCallback returnCallback
     */
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

        //发送消息时指定 header 延迟时间
        rabbitTemplate.convertAndSend(RabbitMqConfig.PRIVATE_PUSH_EXCHANGE, queueName, message,
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

    public void send(Object msg,String exchangeName,String routingKey,long time) {

        this.rabbitTemplate.convertAndSend(exchangeName, routingKey, msg, message -> {
            // TODO 如果配置了 params.put("x-message-ttl", 5 * 1000); 那么这一句也可以省略,具体根据业务需要是声明 Queue 的时候就指定好延迟时间还是在发送自己控制时间
            // message.getMessageProperties().setExpiration(time * 1000+"");
            return message;
        });

    }

}
