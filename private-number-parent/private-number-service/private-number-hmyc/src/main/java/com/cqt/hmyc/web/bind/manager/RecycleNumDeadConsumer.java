package com.cqt.hmyc.web.bind.manager;

import com.alibaba.fastjson.JSON;
import com.cqt.common.enums.AckActionEnum;
import com.cqt.hmyc.config.rabbitmq.RabbitMqConfig;
import com.cqt.hmyc.config.rabbitmq.RabbitmqAck;
import com.cqt.hmyc.web.bind.service.recycle.NumberRecycleService;
import com.cqt.model.bind.dto.BindRecycleDTO;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * Created with IntelliJ IDEA.
 * ClassName: RecycleNumConsumer
 *
 * @author linshiqiang
 * Date: 2021-09-19 20:38
 * Description:
 */
@Component
@Slf4j
public class RecycleNumDeadConsumer {

    @Resource
    private NumberRecycleService numberRecycleService;

    @RabbitListener(queues = {RabbitMqConfig.BIND_RECYCLE_AXB_DELAY_QUEUE,
            RabbitMqConfig.BIND_RECYCLE_AXE_DELAY_QUEUE,
            RabbitMqConfig.BIND_RECYCLE_AXEBN_DELAY_QUEUE,
            RabbitMqConfig.BIND_RECYCLE_AX_DELAY_QUEUE,
            RabbitMqConfig.BIND_RECYCLE_AXBN_DELAY_QUEUE})
    @RabbitHandler
    public void onLazyMessage(Message msg, Channel channel) {
        long deliveryTag = msg.getMessageProperties().getDeliveryTag();
        String data = new String(msg.getBody());
        AckActionEnum action = AckActionEnum.ACCEPT;
        String routingKey = msg.getMessageProperties().getReceivedRoutingKey();
        try {
            BindRecycleDTO bindRecycleDTO = JSON.parseObject(data, BindRecycleDTO.class);
            String numType = bindRecycleDTO.getNumType();
            log.info("延时队列: {}, 接收到消息, vccId: {}, bindId: {}, type: {}", routingKey, bindRecycleDTO.getVccId(),
                    bindRecycleDTO.getBindId(), numType);
            numberRecycleService.recycleNumber(bindRecycleDTO);
        } catch (Exception e) {
            action = RabbitmqAck.getAckActionEnum(e);
            log.error("回收号码, 数据: {}, 消费异常: ", data, e);
        } finally {
            // ack处理
            RabbitmqAck.ackDeal(channel, deliveryTag, action);
        }
    }


}
