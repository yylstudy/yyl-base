package com.cqt.unicom.rabbitmq;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.cqt.common.enums.PushTypeEnum;
import com.cqt.model.push.entity.PrivateBillInfo;
import com.cqt.model.push.entity.PrivateFailMessage;
import com.cqt.unicom.config.RabbitMqConfig;
import com.cqt.unicom.dto.UnicomCdrDTO;
import com.cqt.unicom.dto.UnicomEventDTO;
import com.cqt.unicom.service.UnicomBindInfoService;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * @author hlx
 * @date 2021-09-14
 */
@Component
@Slf4j
public class MqReceiver {

    @Autowired
    private UnicomBindInfoService pushService;




    public void rabbitHandler(Message msg, Channel channel) throws IOException {
        long deliveryTag = msg.getMessageProperties().getDeliveryTag();
        channel.basicAck(deliveryTag, true);
        PrivateFailMessage failMessage = JSON.parseObject(new String(msg.getBody()), PrivateFailMessage.class);
        if (PushTypeEnum.BILL.name().equals(failMessage.getType())) {
            UnicomCdrDTO billInfo = JSON.parseObject(failMessage.getBody(), UnicomCdrDTO.class);
            log.info("接收延时队列话单，重推次数=>{}，Id=>{}",
                    failMessage.getNum(), failMessage.getId());
            pushService.push(failMessage);
        } else {
            log.info("接收延时队列通话状态，重推次数=>{}，Id=>{}",
                    failMessage.getNum(), failMessage.getId());
            pushService.pushEvent(failMessage);
        }
    }

}
