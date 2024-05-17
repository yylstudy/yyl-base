package com.cqt.sms.rabbitmq;

import com.cqt.model.sms.dto.SmsRetryDto;
import com.cqt.sms.config.rabbitmq.RabbitMqConfig;
import com.cqt.sms.service.SmsPushService;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * @Description: MQ消费者
 * @author: scott
 * @date: 2022年03月28日 10:17
 */
@Slf4j
@Component
public class MqConsumer {

    private final SmsPushService smsPushService;

    @Autowired
    public MqConsumer(SmsPushService smsPushService) {
        this.smsPushService = smsPushService;
    }

    /**
     * 企业短信延时队列消费, 消费者最大数量20
     * */
    @RabbitListener(queues = RabbitMqConfig.SMS_PUSH_DELAY_QUEUE, concurrency = "20")
    @RabbitHandler
    public void messageHandler(@Payload SmsRetryDto smsRetryDto, Message message, Channel channel) {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        try {
            log.info("接收到延时短信重推队列, msgId=>{}, 当前重推次数=>{}, 内容: {}", smsRetryDto.getMsgId(), smsRetryDto.getCurrentRetryCount(), smsRetryDto);
            //业务处理
            smsPushService.smsContentPush(smsRetryDto.getPushUrl(), smsRetryDto.getTelX(), smsRetryDto.getTelA(),
                    smsRetryDto.getSmsContent(), smsRetryDto.getRequestTime(), smsRetryDto.getMsgId(),
                    smsRetryDto.getVccId(), smsRetryDto.getCurrentRetryCount());
        }catch (Exception e) {
            log.error("短信重推延时队列消费失败: ", e);
        }finally {
            //手动设置ack
            try {
                channel.basicAck(deliveryTag, true);
            } catch (IOException e) {
                log.error("手动提交ACK失败: ", e);
            }
        }

    }

}
