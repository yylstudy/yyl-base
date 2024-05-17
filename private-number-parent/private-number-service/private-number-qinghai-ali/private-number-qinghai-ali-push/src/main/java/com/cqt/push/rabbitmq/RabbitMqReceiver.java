package com.cqt.push.rabbitmq;


import com.alibaba.fastjson.JSON;
import com.cqt.model.push.entity.AcrRecordOrg;
import com.cqt.model.push.entity.PrivateFailMessage;
import com.cqt.model.push.entity.PrivateStatusInfo;
import com.cqt.push.service.QingHaiAliPushService;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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

public class RabbitMqReceiver {

    @Autowired
    @Qualifier("qingHaiAliPushServiceImpl")
    private QingHaiAliPushService qingHaiAliPushService;

    @RabbitHandler
    public void rabbitHandler(Message message, Channel channel) throws IOException {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        channel.basicAck(deliveryTag, true);
        PrivateFailMessage failMessage = JSON.parseObject(message.getBody(), PrivateFailMessage.class);
        log.info("接收延时队列话单，重推次数=>{},body=>{}", failMessage.getNum(),failMessage.getBody());
        if ("list".equals(failMessage.getType())) {
            AcrRecordOrg customerReceivesDataInfo = JSON.parseObject(failMessage.getBody(), AcrRecordOrg.class);
            qingHaiAliPushService.toAliEndCallRequest(customerReceivesDataInfo, failMessage.getNum());
        } else {
            PrivateStatusInfo privateStatusInfo = JSON.parseObject(failMessage.getBody(), PrivateStatusInfo.class);
            privateStatusInfo.setVccId(failMessage.getVccid());
            qingHaiAliPushService.toAliCallStatusReceiver(privateStatusInfo, failMessage.getNum());
        }
    }

}



