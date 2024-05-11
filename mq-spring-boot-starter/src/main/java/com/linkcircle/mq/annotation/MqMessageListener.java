package com.linkcircle.mq.annotation;

import com.linkcircle.mq.common.RocketmqConsumeMode;
import org.springframework.messaging.handler.annotation.MessageMapping;

import java.lang.annotation.*;

/**
 * @author yang.yonglian
 * @version 1.0.0
 * @Description 消息消费监听
 * @createTime 2024/4/30 15:36
 */
@Target({ ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@MessageMapping
@Documented
public @interface MqMessageListener {

    /**
     * 监听目标
     * rabbitmq是queue
     * rocketmq是topic
     * @return
     */
    String target() default "";

    /**
     * rocketmq下才有意义
     * @return
     */
    String consumerGroup() default "";

    /**
     * tag，rocketmq下才有意义
     * @return
     */
    String tags() default "*";

    /**
     * ack模式，rabbitmq有效
     */
    String ackMode() default "";

    /**
     * 消费模式 rocketmq下才有意义
     */
    RocketmqConsumeMode consumeMode() default RocketmqConsumeMode.CONCURRENTLY;

}
