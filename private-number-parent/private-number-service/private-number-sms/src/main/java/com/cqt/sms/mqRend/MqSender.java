package com.cqt.sms.mqRend;

import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;


@Component
public class MqSender {
    final static String DELAY_ROUTING_KEY_XDELAY = "delay_private_num_sms_routing";
    final static String DELAYED_EXCHANGE_XDELAY = "delay_private_num_sms_exchange";
    @Autowired
    private RabbitTemplate rabbitTemplate;

    public void send(String msg, int delayTime) {
        this.rabbitTemplate.convertAndSend(DELAYED_EXCHANGE_XDELAY, DELAY_ROUTING_KEY_XDELAY, msg, new MessagePostProcessor() {
            @Override
            public Message postProcessMessage(Message message) throws AmqpException {
                message.getMessageProperties().setDelay(delayTime* 60 * 1000);
                return message;
            }
        });
    }



}







