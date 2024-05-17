package com.cqt.sms.config;

import com.cqt.sms.rabbitmq.RabbitMqReceiver;
import org.springframework.amqp.core.AcknowledgeMode;
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
    public static final String ALI_SMS_PUSH_EXCHANGE = "ali-sms-push-fail-exchange";
    public static final String ALI_SMS_INTERCEPT_EXCHANGE = "ali-sms-intercept-fail-exchange";

    /**
     * 话单推送失败延时队列
     * private-push-fail-delay-queue-{vccid}
     */
    public static final String ALI_SMS_PUSH_DELAY_QUEUE = "ali-sms-push-fail-delay-queue-%s";
    public static final String ALI_SMS_INTERCEPT_DELAY_QUEUE = "ali-sms-intercept-fail-delay-queue-%s";
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

        simpleMessageListenerContainer.setMessageListener((ChannelAwareMessageListener) (message, channel) -> mqReceiver.rabbitHandler(message, channel));
        return simpleMessageListenerContainer;
    }

    @Bean
    public RabbitAdmin rabbitAdmin(CachingConnectionFactory cachingConnectionFactory) {
        return new RabbitAdmin(cachingConnectionFactory);
    }


}
