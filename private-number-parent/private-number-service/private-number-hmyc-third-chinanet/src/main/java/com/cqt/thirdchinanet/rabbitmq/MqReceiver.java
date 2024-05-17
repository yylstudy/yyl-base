package com.cqt.thirdchinanet.rabbitmq;

import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.cqt.common.enums.PushTypeEnum;
import com.cqt.model.push.dto.AybBindPushDTO;
import com.cqt.model.push.dto.UnbindPushDTO;
import com.cqt.model.push.entity.PrivateBillInfo;
import com.cqt.model.push.entity.PrivateFailMessage;
import com.cqt.model.push.entity.PrivateStatusInfo;

import com.cqt.thirdchinanet.entity.sms.IccpSmsStatePush;
import com.cqt.thirdchinanet.service.PushService;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
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
    private PushService pushService;

    public void rabbitHandler(Message msg, Channel channel) throws IOException {
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
            IccpSmsStatePush smsRequest = JSON.parseObject(JSONUtil.toJsonStr(failMessage.getBody()), IccpSmsStatePush.class);
            log.info("接收延时队列ayb绑定事件，重推次数=>{}，BindIdId=>{}",
                    failMessage.getNum(), smsRequest.getBind_id());
            pushService.smsThirdPush(failMessage);
        }
    }

}
