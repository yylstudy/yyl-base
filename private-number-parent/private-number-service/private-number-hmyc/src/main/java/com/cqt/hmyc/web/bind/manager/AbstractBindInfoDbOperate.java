package com.cqt.hmyc.web.bind.manager;

import cn.hutool.core.util.IdUtil;
import com.cqt.common.enums.AckActionEnum;
import com.cqt.hmyc.config.rabbitmq.RabbitMqConfig;
import com.cqt.hmyc.config.rabbitmq.RabbitmqAck;
import com.cqt.hmyc.web.bind.service.recycle.NumberRecycleService;
import com.cqt.model.bind.bo.MqBindInfoBO;
import com.cqt.rabbitmq.util.RabbitmqUtil;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.dao.TransientDataAccessResourceException;

import java.time.Duration;

/**
 * @author linshiqiang
 * date:  2023-03-10 13:49
 * 绑定关系入库 模板方法
 */
@Slf4j
public abstract class AbstractBindInfoDbOperate {

    public void doConsumer(MqBindInfoBO mqBindInfoBO, Message msg, Channel channel) {
        long deliveryTag = msg.getMessageProperties().getDeliveryTag();
        AckActionEnum action = AckActionEnum.ACCEPT;
        try {
            getNumberRecycleService().saveBindInfo(mqBindInfoBO);
        } catch (Exception e) {
            action = RabbitmqAck.getAckActionEnum(e);
            // TransientDataAccessResourceException异常 mysql挂了, 发送死信队列+ttl
            log.error("mq新增绑定关系失败, 数据: {}, 异常: ", mqBindInfoBO, e);
            if (e instanceof TransientDataAccessResourceException) {
                getRabbitmqUtil().sendDelayMessage(IdUtil.fastSimpleUUID(),
                        Duration.ofMinutes(10),
                        RabbitMqConfig.BIND_DB_ERROR_DELAY_EXCHANGE,
                        RabbitMqConfig.BIND_DB_ERROR_DELAY_QUEUE,
                        mqBindInfoBO);
            }
        } finally {
            // ack处理
            RabbitmqAck.ackDeal(channel, deliveryTag, action);
        }
    }

    /**
     * 号码回收服务
     *
     * @return 号码回收服务
     */
    public abstract NumberRecycleService getNumberRecycleService();

    /**
     * Rabbitmq操作工具类
     *
     * @return Rabbitmq操作工具类
     */
    public abstract RabbitmqUtil getRabbitmqUtil();
}
