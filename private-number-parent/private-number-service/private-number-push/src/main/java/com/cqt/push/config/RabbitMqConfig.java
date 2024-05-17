package com.cqt.push.config;

import com.cqt.push.rabbitmq.MqReceiver;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.api.ChannelAwareMessageListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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
    public static final String PRIVATE_PUSH_EXCHANGE = "private-push-fail-exchange";

    /**
     * 话单推送失败延时队列
     * private-push-fail-delay-queue-{vccid}
     */
    public static final String PRIVATE_PUSH_DELAY_QUEUE = "private-push-fail-delay-queue-%s";

    /**
     * 规则
     */
    public static final String ROUTING_KEY = "fail";

    /**
     * 拨测交换机
     */
    public static final String CHECK_BILL_PUSH_EXCHANGE = "check-bill-push-fail-exchange";

    /**
     * 拨测话单推送失败延时队列
     */
    public static final String CHECK_BILL_PUSH_DELAY_QUEUE = "check-bill-push-fail-delay-queue";

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

    @Bean("check_exchange")
    public DirectExchange checkBindExchange() {
        DirectExchange directExchange = new DirectExchange(CHECK_BILL_PUSH_EXCHANGE, true, true);
        directExchange.setDelayed(true);
        return directExchange;
    }

    @Bean("check_queue")
    public Queue checkDelayQueue() {
        return new Queue(CHECK_BILL_PUSH_DELAY_QUEUE, true);
    }

    @Bean
    public Binding checkBinding(@Qualifier("check_exchange") DirectExchange exchange,
                                @Qualifier("check_queue") Queue queue) {
        return BindingBuilder.bind(queue).to(exchange).with(ROUTING_KEY);
    }

}
