package com.cqt.push.config;

import com.cqt.push.rabbitmq.RabbitMqReceiver;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.api.ChannelAwareMessageListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author zhengsuhao
 * @date 2023-02-11
 */
@Configuration
public class RabbitMqConfig {
    /**
     * 交换机
     */
    public static final String ALI_CALL_LIST_PUSH_EXCHANGE = "ali-call-list-push-fail-exchange";

    /**
     * 话单推送失败延时队列
     * private-push-fail-delay-queue-{vccid}
     */
    public static final String ALI_CALL_LIST_PUSH_DELAY_QUEUE = "ali-call-list-push-fail-delay-queue-%s";

    public static final String ALI_CALL_EVENT_PUSH_EXCHANGE = "ali-call-event-push-fail-exchange";

    /**
     * 话单推送失败延时队列
     * private-push-fail-delay-queue-{vccid}
     */
    public static final String ALI_CALL_EVENT_PUSH_DELAY_QUEUE = "ali-call-event-push-fail-delay-queue-%s";

    @Autowired
    private RabbitMqReceiver mqReceiver;

    @Bean
    public RabbitTemplate rabbitTemplate(CachingConnectionFactory cachingConnectionFactory) {
        return new RabbitTemplate(cachingConnectionFactory);
    }

    @Bean
    public SimpleMessageListenerContainer simpleMessageListenerContainer(CachingConnectionFactory cachingConnectionFactory) {
        SimpleMessageListenerContainer simpleMessageListenerContainer =
                new SimpleMessageListenerContainer(cachingConnectionFactory);

        simpleMessageListenerContainer.setAcknowledgeMode(AcknowledgeMode.MANUAL);

        simpleMessageListenerContainer.setMessageListener(new ChannelAwareMessageListener() {
            @Override
            public void onMessage(Message message, Channel channel) throws Exception {
                mqReceiver.rabbitHandler(message, channel);
            }
        });
        return simpleMessageListenerContainer;
    }

    @Bean
    public RabbitAdmin rabbitAdmin(CachingConnectionFactory cachingConnectionFactory) {
        return new RabbitAdmin(cachingConnectionFactory);
    }


}
