package com.linkcircle.mq.common;

/**
 * @author yang.yonglian
 * @version 1.0.0
 * @Description TODO
 * @createTime 2024/4/30 16:06
 */

public class MqConstant {
    public static final String DEFAULT_LISTENER_METHOD = "listenerRabbitmq";
    public static final String NAME_SERVER_PLACEHOLDER = "${rocketmq.name-server:}";
    public static final String ACCESS_KEY_PLACEHOLDER = "${rocketmq.consumer.access-key:}";
    public static final String SECRET_KEY_PLACEHOLDER = "${rocketmq.consumer.secret-key:}";
    public static final String TRACE_TOPIC_PLACEHOLDER = "${rocketmq.consumer.customized-trace-topic:}";
    public static final String ACCESS_CHANNEL_PLACEHOLDER = "${rocketmq.access-channel:}";
}
