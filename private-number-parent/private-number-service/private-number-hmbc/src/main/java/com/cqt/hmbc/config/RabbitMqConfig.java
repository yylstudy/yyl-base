package com.cqt.hmbc.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


/**
 * 话单数据库查询延迟队列声明配置
 *
 * @author xienx
 */
@Configuration(proxyBeanMethods = false)
public class RabbitMqConfig {

    /**
     * 号码拨测话单查询延时队列
     */
    public static final String HMBC_CDR_QUERY_DELAY_QUEUE = "hmbc-cdr-query-delay-queue";

    /**
     * 号码拨测话单查询延时交换机
     */
    public static final String HMBC_CDR_QUERY_DELAY_EXCHANGE = "hmbc-cdr-query-delay-exchange";

    /**
     * 路由规则
     */
    public static final String ROUTING_KEY = "hmbc.#";

    /**
     * 号码拨测结果推送延时队列
     */
    public static final String HMBC_RESULT_PUSH_DELAY_QUEUE = "hmbc-result-push-delay-queue";

    /**
     * 号码拨测结果推送延时队列
     */
    public static final String HMBC_RESULT_PUSH_DELAY_EXCHANGE = "hmbc-result-push-delay-exchange";

    /**
     * 号码拨测路由键
     */
    public static final String PUSH_ROUTING_KEY = "hmbc.push.#";

    @Bean(HMBC_CDR_QUERY_DELAY_QUEUE)
    public Queue delayQueue() {
        return new Queue(HMBC_CDR_QUERY_DELAY_QUEUE, true);
    }

    @Bean(HMBC_CDR_QUERY_DELAY_EXCHANGE)
    public DirectExchange delayExchange() {
        DirectExchange directExchange = new DirectExchange(HMBC_CDR_QUERY_DELAY_EXCHANGE, true, false);
        directExchange.setDelayed(true);
        return directExchange;
    }

    @Bean
    public Binding binging(@Qualifier(HMBC_CDR_QUERY_DELAY_EXCHANGE) DirectExchange exchange,
                           @Qualifier(HMBC_CDR_QUERY_DELAY_QUEUE) Queue queue) {
        return BindingBuilder.bind(queue).to(exchange).with(ROUTING_KEY);
    }

    @Bean(HMBC_RESULT_PUSH_DELAY_QUEUE)
    public Queue pushDelayQueue() {
        return new Queue(HMBC_RESULT_PUSH_DELAY_QUEUE, true);
    }

    @Bean(HMBC_RESULT_PUSH_DELAY_EXCHANGE)
    public DirectExchange pushDelayExchange() {
        DirectExchange directExchange = new DirectExchange(HMBC_RESULT_PUSH_DELAY_EXCHANGE, true, false);
        directExchange.setDelayed(true);
        return directExchange;
    }

    @Bean
    public Binding pushbinding(@Qualifier(HMBC_RESULT_PUSH_DELAY_EXCHANGE) DirectExchange exchange,
                               @Qualifier(HMBC_RESULT_PUSH_DELAY_QUEUE) Queue queue) {
        return BindingBuilder.bind(queue).to(exchange).with(PUSH_ROUTING_KEY);
    }

}
