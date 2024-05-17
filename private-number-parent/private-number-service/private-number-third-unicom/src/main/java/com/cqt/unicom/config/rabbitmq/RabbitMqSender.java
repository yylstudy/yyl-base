package com.cqt.unicom.config.rabbitmq;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/**
 * 往队列发送数据
 *
 * @author zhengsuhao
 * @date 2022/12/07
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class RabbitMqSender {


    private final RabbitTemplate rabbitTemplate;


    public void send(Object msg, String exchangeName, String routingKey, long time) {
        this.rabbitTemplate.convertAndSend(exchangeName, routingKey, msg, message -> {
            return message;
        });
    }

}
