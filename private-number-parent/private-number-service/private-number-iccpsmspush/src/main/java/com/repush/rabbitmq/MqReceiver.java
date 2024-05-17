package com.repush.rabbitmq;//package com.cqt.sms.rabbitmq;
//
//import com.alibaba.fastjson.JSONObject;
//import com.cqt.sms.config.rabbitmq.RabbitMqConfig;
//import com.cqt.sms.model.entity.BillMessage;
//import com.cqt.sms.service.PushService;
//import com.rabbitmq.client.Channel;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.amqp.core.Message;
//import org.springframework.amqp.rabbit.annotation.RabbitHandler;
//import org.springframework.amqp.rabbit.annotation.RabbitListener;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Component;
//
//import java.io.IOException;
//
///**
// * @author hlx
// * @date 2021-09-14
// */
//@Component
//@Slf4j
//public class MqReceiver {
//
//    @Autowired
//    private PushService pushService;
//
//    @RabbitListener(queues = RabbitMqConfig.BILL_PUSH_DELAY_QUEUE)
//    @RabbitHandler
//    public void onLazyMessage(Message msg, Channel channel) throws IOException {
//        // msg.getMessageProperties().getHeaders().get("spring_returned_message_correlation"); 获取唯一id
//        long deliveryTag = msg.getMessageProperties().getDeliveryTag();
//        channel.basicAck(deliveryTag, true);
//        JSONObject jsonObject = JSONObject.parseObject(new String(msg.getBody()));
//        BillMessage billMessage = jsonObject.toJavaObject(BillMessage.class);
//        log.info("接收延时队列话单，重推次数=>{}，RecordId=>{}", billMessage.getNum(), billMessage.getBill().getRecordId());
//        pushService.pushStart(billMessage);
//    }
//
//}
