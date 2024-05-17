package com.cqt.hmyc.web.bind.manager;

import com.cqt.cloud.api.sms.SmsPushBillFeignClient;
import com.cqt.hmyc.config.rabbitmq.DelayedPushRabbitConfig;
import com.cqt.model.push.entity.CdrResult;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
/**
 * 消费sms数据
 *
 * @author dingsh
 * @date 2022/08/01
 */
@Component
@Slf4j
public class SmsDelayedReceiver {

    private final SmsPushBillFeignClient smsPushBillFeignClient;

    public SmsDelayedReceiver(SmsPushBillFeignClient smsPushBillFeignClient) {
        this.smsPushBillFeignClient = smsPushBillFeignClient;
    }

    @RabbitListener(queues = DelayedPushRabbitConfig.CDR_SMS_PUSH_DEAD_LETTER_QUEUE)
    @RabbitHandler
    public void deadLetterMsg(Message message, Channel channel) throws IOException {
        String msgRequest = new String(message.getBody());
        log.info("收到第三方短信话单重推：{}", msgRequest);

        try {
            CdrResult cdrResult = smsPushBillFeignClient.smsThirdPush(msgRequest);
            log.info("第三方短信话单重推结果：{}", cdrResult);
        } catch (Exception e) {
            log.error("smsPushBillFeignClient.smsThirdPush error: ", e);
            channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, true);
            return;
        }
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
    }

}
