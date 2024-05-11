package com.yyl;

import com.linkcircle.mq.annotation.MqMessageListener;
import com.linkcircle.mq.listener.MqListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.stereotype.Component;

/**
 * @author yang.yonglian
 * @version 1.0.0
 * @Description TODO
 * @createTime 2024/5/7 19:48
 */
@MqMessageListener(target = "normal_queue")
@Slf4j
@Component
@ConditionalOnClass(name = "org.springframework.amqp.rabbit.core.RabbitTemplate")
public class RabbitMqListener3 implements MqListener<SysUser> {
    @Override
    public void onMessage(SysUser sysUser) {
        log.info("normal_queue 接收到消息：{}",sysUser);
    }
}
