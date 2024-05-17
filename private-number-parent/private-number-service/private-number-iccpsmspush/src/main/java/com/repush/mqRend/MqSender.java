package com.repush.mqRend;

import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
public class MqSender {
    final static String DELAY_ROUTING_KEY_XDELAY = "delay_meituan_sms_routing";
    final static String DELAYED_EXCHANGE_XDELAY = "delay_meituan_sms_exchange";
    @Autowired
    private RabbitTemplate rabbitTemplate;

    public void send(String msg, int delayTime) {
        this.rabbitTemplate.convertAndSend(DELAYED_EXCHANGE_XDELAY, DELAY_ROUTING_KEY_XDELAY, msg, new MessagePostProcessor() {
            @Override
            public Message postProcessMessage(Message message) throws AmqpException {
                message.getMessageProperties().setDelay(delayTime);
                return message;
            }
        });
    }


}







