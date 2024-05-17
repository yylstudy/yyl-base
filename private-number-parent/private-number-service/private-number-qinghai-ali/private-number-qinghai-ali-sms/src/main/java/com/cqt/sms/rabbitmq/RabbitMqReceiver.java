package com.cqt.sms.rabbitmq;


import com.alibaba.fastjson.JSON;
import com.cqt.model.push.entity.PrivateFailMessage;
import com.cqt.model.unicom.entity.SmsRequest;
import com.cqt.sms.service.impl.QingHaiAliSmsInterfaceImpl;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;


/**
 * 消费重送数据
 *
 * @author zhengsuhao
 * @date 2023-02-11
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class RabbitMqReceiver {


    private final QingHaiAliSmsInterfaceImpl qingHaiAliSmsInterface;

    @RabbitHandler
    public void rabbitHandler(Message message, Channel channel) throws IOException {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        channel.basicAck(deliveryTag, true);
        PrivateFailMessage privateFailMessage = JSON.parseObject(message.getBody(), PrivateFailMessage.class);
        log.info("接收延时队列话单，重推次数=>{}", privateFailMessage.getNum());
        SmsRequest smsRequest = JSON.parseObject(privateFailMessage.getBody(), SmsRequest.class);
        if ("list".equals(privateFailMessage.getType())) {
            qingHaiAliSmsInterface.generateEndCallRequest(smsRequest, privateFailMessage.getNum());
        } else {
            qingHaiAliSmsInterface.toAliSendSmsIntercept(smsRequest, privateFailMessage.getNum());
        }
    }

}



