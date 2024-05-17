package com.cqt.wechat.config;

import com.cqt.wechat.rabbitmq.MqReceiver;
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
 * @author hlx
 * @date 2021-09-14
 */
@Configuration
public class RabbitMqConfig {

    /**
     * 交换机
     */
    public static final String PRIVATE_PUSH_EXCHANGE = "private-wechat-push-fail-exchange";

    /**
     * 话单推送失败延时队列
     * private-push-fail-delay-queue-{vccid}
     */
    public static final String PRIVATE_PUSH_DELAY_QUEUE = "private-wechat-push-fail-delay-queue-%s";

    @Autowired
    private MqReceiver mqReceiver;

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
