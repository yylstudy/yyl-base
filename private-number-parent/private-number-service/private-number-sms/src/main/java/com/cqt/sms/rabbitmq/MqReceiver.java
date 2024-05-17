package com.cqt.sms.rabbitmq;

import com.alibaba.fastjson.JSON;
import com.cqt.model.push.entity.PrivateFailMessage;
import com.cqt.sms.controller.SmsController;
import com.cqt.sms.mqRend.MqSender;
import com.cqt.sms.service.SmsService;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;

/**
 * @author hlx
 * @date 2021-09-14
 */
@Component
@Slf4j
public class MqReceiver {

    @Resource
    private SmsService smsService;

    @Autowired
    private MqSender mqSender;

    @Autowired
    private SmsController smsController;

    /**
     * 消费短信账单
     *
     * @param channel
     * @param message
     */
    @RabbitListener(queues = "private_num_sms_queue")
    public void smsStateRepush(Channel channel, Message message) throws IOException {
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        PrivateFailMessage failMessage = JSON.parseObject(new String(message.getBody()), PrivateFailMessage.class);
        log.info("接收短信延时队列话单，重推次数=>{}",failMessage.getNum());
        smsController.catchPushException(failMessage);
//        String uuid = StringUtil.getUUID();
//        String msgRequest = "";
//        msgRequest = new String(message.getBody());
//        log.info(uuid + "从mq获取到状态推送" + msgRequest);
//        SmsStatePush smsStatePush = JsonUtil.jsonToPojo(msgRequest, SmsStatePush.class);
//        String potsResult = "";
//        try {
//            if ((Integer.parseInt(smsStatePush.getNum())>2)) {
//                log.info(uuid + "|超过最大重推次数,写入数据库不在重推");
//                smsStatePush.setErrMsg("超过最大重推次数");
//                smsStatePush.setId(uuid);
//                smsService.saveSmsFailedStatePush(smsStatePush);
//            } else {
//                potsResult = HttpClientUtil.doPostJson(smsStatePush.getUrl(), smsStatePush.getJson());
//                MeiTuanResp meiTuanResp = JsonUtil.jsonToPojo(potsResult, MeiTuanResp.class);
//                log.info(uuid + "|状态推送客户结果：" + potsResult);
//            }
//            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
//        } catch (Exception e) {
//            try {
//                //推送失败，写入mq
//                log.info(uuid + "|状态推送失败，写入mq进行重推");
//                smsStatePush.setErrMsg(potsResult);
//                smsStatePush.setNum(String.valueOf(Integer.parseInt(smsStatePush.getNum()) + 1));
//                mqSender.send(JsonUtil.objectToJson(smsStatePush), 3000);
//                channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
//            } catch (IOException ioException) {
//                ioException.printStackTrace();
//            }
//        }
    }



}
