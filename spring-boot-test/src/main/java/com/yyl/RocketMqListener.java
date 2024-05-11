package com.yyl;

import com.linkcircle.mq.annotation.MqMessageListener;
import com.linkcircle.mq.listener.MqListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicLong;

/**
 * @author yang.yonglian
 * @version 1.0.0
 * @Description TODO
 * @createTime 2024/5/7 19:48
 */
@MqMessageListener(target = "normal_exchange",consumerGroup = "yyl_consumerGroup2")
@Slf4j
@Component
@ConditionalOnClass(name = "org.apache.rocketmq.client.MQAdmin")
public class RocketMqListener implements MqListener<SysUser> {
    AtomicLong atomicLong = new AtomicLong();
    @Override
    public void onMessage(SysUser sysUser) {
        if(atomicLong.incrementAndGet()%20000==0){
            log.info("normal_exchange 接收到消息：{}",sysUser);
        }

    }
}
