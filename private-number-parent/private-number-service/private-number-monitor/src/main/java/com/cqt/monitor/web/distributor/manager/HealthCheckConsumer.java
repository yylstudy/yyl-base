package com.cqt.monitor.web.distributor.manager;

import com.cqt.monitor.config.TestRabbitmqConfig;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * @author linshiqiang
 * @since 2022-12-05 16:26
 * 消费测试消息
 */
@Slf4j
@Component
public class HealthCheckConsumer {

    @RabbitListener(queues = TestRabbitmqConfig.HEALTH_QUEUE)
    public void check(Message msg, Channel channel) throws IOException {
        long deliveryTag = msg.getMessageProperties().getDeliveryTag();
        log.info("mq health check");
        channel.basicAck(deliveryTag, false);
    }
}
