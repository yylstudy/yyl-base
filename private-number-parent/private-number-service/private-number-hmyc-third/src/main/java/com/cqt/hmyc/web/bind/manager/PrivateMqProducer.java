package com.cqt.hmyc.web.bind.manager;

import cn.hutool.core.util.IdUtil;
import cn.hutool.http.ContentType;
import com.cqt.hmyc.config.rabbitmq.DelayRabbitMqConfig;
import com.cqt.hmyc.config.rabbitmq.RabbitMqConfig;
import com.cqt.model.bind.dto.BindRecycleDTO;
import com.cqt.model.common.properties.HideProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.Optional;

/**
 * @author linshiqiang
 * @date 2021/9/12 14:57
 * mq 延时队列操作
 */
@Component
@Slf4j
public class PrivateMqProducer {

    @Resource
    private RabbitTemplate rabbitTemplate;

    @Resource
    private RabbitTemplate bindRabbitTemplate;

    @Resource
    private HideProperties hideProperties;

    @Resource(name = "saveExecutor")
    private ThreadPoolTaskExecutor saveExecutor;

    public void sendMessage(String exchange, String queue, Object message) {
        try {
            bindRabbitTemplate.convertAndSend(exchange, queue, message);
        } catch (Exception e) {
            log.error("队列: {}, 发送消息: {}, 到mq异常: ", queue, message, e);
        }
    }


    public void sendLazy(Optional<BindRecycleDTO> bindRecycleDtoOptional, Integer expiration) {

        saveExecutor.execute(() -> {
            try {
                if (bindRecycleDtoOptional.isPresent()) {
                    BindRecycleDTO bindRecycleDTO = bindRecycleDtoOptional.get();
                    // 小写 类型 @See com.cqt.common.enums.BusinessTypeEnum
                    String numType = bindRecycleDTO.getNumType().toLowerCase();
                    // 发生到号码回收延时队列
                    sendLazy(bindRecycleDTO, expiration,
                            String.format(RabbitMqConfig.BIND_RECYCLE_EXCHANGE, numType),
                            String.format(RabbitMqConfig.BIND_RECYCLE_DELAY_QUEUE, numType),
                            bindRecycleDTO.getBindId(),
                            bindRecycleDTO.getNumType());
                }

            } catch (Exception e) {
                log.error("data: {}, 推mq异常: ", bindRecycleDtoOptional, e);
            }
        });
    }

    /**
     * 延时队列发送消息
     *
     * @param bindRecycleDTO 消息体
     * @param delay          延时时间, 单位: 秒
     * @param exchange       延时插件 交换机名称
     * @param queue          延时插件 队列名称
     * @param bindId         绑定id
     * @param type           绑定关系类型 AXB AXE...
     */
    private void sendLazy(BindRecycleDTO bindRecycleDTO, Integer delay, String exchange, String queue, String bindId, String type) {
        if (delay >= hideProperties.getLongestExpiration()) {
            log.info("bindId: {}, delay too long, do not push mq", bindId);
            return;
        }

        // id + 时间戳 全局唯一
        String id = IdUtil.fastUUID();
        CorrelationData correlationData = new CorrelationData(id);

        // 有效期 命中 按秒分队列
        List<Integer> deadQueueSecondIndexList = hideProperties.getDeadQueueSecondIndexList();
        if (deadQueueSecondIndexList.contains(delay)) {
            // 发送延时 死信队列, 按秒
            bindRabbitTemplate.convertAndSend(String.format(DelayRabbitMqConfig.RECYCLE_EXCHANGE_DELAY_S, delay),
                    String.format(DelayRabbitMqConfig.RECYCLE_QUEUE_DELAY_S, delay), bindRecycleDTO,
                    correlationData);
            log.info("{} bindId: {}, expiration: {}, mq延时-死信: {}s, 消息发送完成.", type, bindId, delay, delay);
            return;
        }

        int hour = 3600;

        int index = delay % hour == 0 ? delay / hour : delay / hour + 1;
        List<Integer> queueIndexList = hideProperties.getDeadQueueIndexList();
        if (delay >= hour && queueIndexList.contains(index)) {
            // 发送延时 死信队列, 按小时
            bindRabbitTemplate.convertAndSend(String.format(DelayRabbitMqConfig.RECYCLE_EXCHANGE_DELAY_H, index),
                    String.format(DelayRabbitMqConfig.RECYCLE_QUEUE_DELAY_H, index), bindRecycleDTO,
                    correlationData);
            log.info("{} bindId: {}, expiration: {}, mq延时-死信: {}h, 消息发送完成.", type, bindId, delay, index);
            return;
        }

        //发送消息时指定 header 延迟时间
        bindRabbitTemplate.convertAndSend(exchange, queue, bindRecycleDTO,
                message -> {
                    //设置消息持久化
                    message.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT);
                    message.getMessageProperties().setDelay((delay + hideProperties.getDelayTimeout()) * 1000);
                    message.getMessageProperties().setContentType(ContentType.JSON.getValue());
                    return message;
                }, correlationData);
        log.info("{} bindId: {},  expiration: {}, mq延时插件消息发送完成.", type, bindId, delay);
    }


}
