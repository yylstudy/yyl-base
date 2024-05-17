package com.cqt.hmyc.web.bind.manager;

import com.cqt.hmyc.config.rabbitmq.RabbitMqConfig;
import com.cqt.hmyc.web.bind.service.recycle.NumberRecycleService;
import com.cqt.model.bind.bo.MqBindInfoBO;
import com.cqt.rabbitmq.util.RabbitmqUtil;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/**
 * @author linshiqiang
 * date:  2023-03-06 17:09
 * 绑定关系db异常, 操作入库失败, 重试
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class BindInfoDbOperateErrorRetryConsumer extends AbstractBindInfoDbOperate {

    private final NumberRecycleService numberRecycleService;

    private final RabbitmqUtil rabbitmqUtil;

    @RabbitListener(queues = {RabbitMqConfig.BIND_DB_ERROR_DEAD_QUEUE})
    @RabbitHandler
    public void onMessage(@Payload MqBindInfoBO mqBindInfoBO, Message msg, Channel channel) {
        doConsumer(mqBindInfoBO, msg, channel);
    }

    @Override
    public NumberRecycleService getNumberRecycleService() {
        return numberRecycleService;
    }

    @Override
    public RabbitmqUtil getRabbitmqUtil() {
        return rabbitmqUtil;
    }
}
