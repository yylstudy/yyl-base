package com.cqt.hmyc.web.bind.manager;

import com.cqt.cloud.api.push.BindPushFeignClient;
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
 * 消费数据数据
 *
 * @author dingsh
 * date 2022/08/01
 */
@Component
@Slf4j
public class DelayedReceiver {

    private final BindPushFeignClient bindPushFeignClient;

    public DelayedReceiver(BindPushFeignClient bindPushFeignClient) {
        this.bindPushFeignClient = bindPushFeignClient;
    }

    @RabbitListener(queues = DelayedPushRabbitConfig.CDR_PUSH_DEAD_LETTER_QUEUE)
    @RabbitHandler
    public void deadLetterMsg(Message message, Channel channel) throws IOException {
        String msgRequest = new String(message.getBody());
        log.info("收到第三方话单重推：{}", msgRequest);
        try {
            CdrResult cdrResult = bindPushFeignClient.thirdBillReceiver(msgRequest);
            log.info("第三方话单重推结果：{}", cdrResult);
        } catch (Exception e) {
            log.error("bindPushFeignClient.thirdBillReceiver error: ", e);
            channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, true);
            return;
        }
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
    }

}
