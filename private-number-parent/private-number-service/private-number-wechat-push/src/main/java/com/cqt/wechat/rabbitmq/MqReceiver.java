package com.cqt.wechat.rabbitmq;

import com.alibaba.fastjson.JSON;
import com.cqt.common.enums.PushTypeEnum;
import com.cqt.model.push.entity.PrivateBillInfo;
import com.cqt.model.push.entity.PrivateFailMessage;
import com.cqt.model.push.entity.PrivateStatusInfo;
import com.cqt.model.sms.dto.CommonSmsBillPushDTO;
import com.cqt.wechat.service.PushService;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.text.ParseException;

/**
 * @author hlx
 * @date 2021-09-14
 */
@Component
@Slf4j
public class MqReceiver {

    @Autowired
    private PushService pushService;

    public void rabbitHandler(Message msg, Channel channel) throws IOException, ParseException {
        // msg.getMessageProperties().getHeaders().get("spring_returned_message_correlation"); 获取唯一id
        long deliveryTag = msg.getMessageProperties().getDeliveryTag();
        channel.basicAck(deliveryTag, true);
        PrivateFailMessage failMessage = JSON.parseObject(new String(msg.getBody()), PrivateFailMessage.class);
        // 判断信息类型
        if (PushTypeEnum.BILL.name().equals(failMessage.getType())) {
            PrivateBillInfo billInfo = JSON.parseObject(failMessage.getBody(), PrivateBillInfo.class);
            log.info("接收延时队列话单，重推次数=>{}，RecordId=>{}",
                    failMessage.getNum(), billInfo.getRecordId());
            pushService.pushBillStart(failMessage);
        } else if (PushTypeEnum.STATUS.name().equals(failMessage.getType())) {
            PrivateStatusInfo statusInfo = JSON.parseObject(failMessage.getBody(), PrivateStatusInfo.class);
            log.info("接收延时队列通话状态，重推次数=>{}，RecordId=>{}",
                    failMessage.getNum(), statusInfo.getRecordId());
            pushService.pushStatusStart(failMessage);
        } else if (PushTypeEnum.SMS.name().equals(failMessage.getType())) {
            CommonSmsBillPushDTO statusInfo = JSON.parseObject(failMessage.getBody(), CommonSmsBillPushDTO.class);
            log.info("接收延时队列短信话单，重推次数=>{}，SmsId=>{}",
                    failMessage.getNum(), statusInfo.getSmsId());
            pushService.pushMsg(failMessage);
        }
    }

}
