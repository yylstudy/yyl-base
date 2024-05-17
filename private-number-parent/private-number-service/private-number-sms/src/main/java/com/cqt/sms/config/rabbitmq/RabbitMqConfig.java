package com.cqt.sms.config.rabbitmq;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


/**
 * @author ffy
 */
@Slf4j
@Configuration
public class RabbitMqConfig {

    /**
     * 交换机
     */
    public static final String BILL_PUSH_EXCHANGE = "bill-sms-fail-exchange";

    /**
     * 话单推送失败延时队列
     */
    public static final String BILL_PUSH_DELAY_QUEUE = "bill-sms-fail-delay-queue";

    /**
     * 短信推送失败延时队列
     * */
    public static final String SMS_PUSH_DELAY_QUEUE = "sms-push-failed-delay-queue";

    /**
     * 短信推送失败交换机
     * */
    public static final String SMS_PUSH_EXCHANGE = "sms-push-failed-exchange";

    /**
     * 短信推送失败路由规则
     * */
    public static final String SMS_PUSH_ROUTING = "sms.push.retry.#";

    /**
     * 规则
     */
    public static final String ROUTING_KEY = "fail";



    @Bean("exchange")
    public DirectExchange bindExchange() {
        DirectExchange directExchange = new DirectExchange(BILL_PUSH_EXCHANGE, true, true);
        directExchange.setDelayed(true);
        return directExchange;
    }

    @Bean("queue")
    public Queue delayQueue() {
        return new Queue(BILL_PUSH_DELAY_QUEUE, true);
    }

    @Bean
    public Binding binding(@Qualifier("exchange") DirectExchange exchange, @Qualifier("queue") Queue queue) {
        return BindingBuilder.bind(queue).to(exchange).with(ROUTING_KEY);
    }

    @Bean("smsPushDelayQueue")
    public Queue smsPushDelayQueue() {
        return new Queue(SMS_PUSH_DELAY_QUEUE, true);
    }

    @Bean("bindSmsPushExchange")
    public DirectExchange bindSmsPushExchange() {
        DirectExchange directExchange = new DirectExchange(SMS_PUSH_EXCHANGE, true, true);
        directExchange.setDelayed(true);
        return directExchange;
    }

    @Bean
    public Binding bindingSmsPush(@Qualifier("bindSmsPushExchange") DirectExchange exchange, @Qualifier("smsPushDelayQueue") Queue queue) {
        return BindingBuilder.bind(queue).to(exchange).with(SMS_PUSH_ROUTING);
    }
}
