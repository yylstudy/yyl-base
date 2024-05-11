package com.yyl;

import com.linkcircle.mq.annotation.MqMessageListener;
import com.linkcircle.mq.common.RocketmqConsumeMode;
import com.linkcircle.mq.listener.MqListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.stereotype.Component;

/**
 * @author yang.yonglian
 * @version 1.0.0
 * @Description TODO
 * @createTime 2024/5/8 17:24
 */
@MqMessageListener(target = "order_topic",consumerGroup = "yyl_consumerGroup4",consumeMode = RocketmqConsumeMode.ORDERLY)
@Slf4j
@Component
@ConditionalOnClass(name = "org.apache.rocketmq.client.MQAdmin")
public class RocketMqOrderlyListener implements MqListener<SysUser> {
    @Override
    public void onMessage(SysUser sysUser) {
        log.info("order_topic 接收到消息：{}",sysUser);
        try {
            Thread.sleep(30000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

    }
}
