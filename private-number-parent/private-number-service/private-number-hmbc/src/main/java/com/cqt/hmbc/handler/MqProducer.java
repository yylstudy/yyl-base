package com.cqt.hmbc.handler;

import cn.hutool.core.util.IdUtil;
import com.alibaba.fastjson.JSON;
import com.cqt.hmbc.retry.BaseRetryInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/**
 * MqProducer
 *
 * @author Xienx
 * @date 2023年02月24日 15:15
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MqProducer {

    private final RabbitTemplate rabbitTemplate;

    /**
     * 将信息推给延时队列
     *
     * @param pushParam  推送信息
     * @param delayTime  消息延迟时长, 单位毫秒
     * @param exchange   延时交换机名称
     * @param routingKey 路由键
     */
    public <T extends BaseRetryInfo> void sendDelayMsg(T pushParam, int delayTime, String exchange, String routingKey) {
        // 将当前的计数加一
        pushParam.incrRetryCount();
        String jsonStr = JSON.toJSONString(pushParam);
        log.info("exchange: {}, delayTime: {} ms", exchange, delayTime);
        rabbitTemplate.convertAndSend(exchange, routingKey, jsonStr,
                message -> {
                    //设置消息持久化
                    message.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT);
                    //设置消息延迟时间, 单位ms
                    message.getMessageProperties().setDelay(delayTime);
                    message.getMessageProperties().setMessageId(IdUtil.objectId());
                    return message;
                }
        );
    }
}
