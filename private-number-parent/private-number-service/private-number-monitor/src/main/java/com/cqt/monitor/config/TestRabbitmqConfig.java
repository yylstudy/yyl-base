package com.cqt.monitor.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author linshiqiang
 * @since 2022-12-05 16:19
 */
@Configuration
public class TestRabbitmqConfig {

    public final static String HEALTH_EXCHANGE = "private_health_check_exchange";

    public final static String HEALTH_QUEUE = "private_health_check_queue";

    @Bean(HEALTH_EXCHANGE)
    public DirectExchange healthExchange() {

        return ExchangeBuilder.directExchange(HEALTH_EXCHANGE).build();
    }

    @Bean(HEALTH_QUEUE)
    public Queue healthQueue() {

        return QueueBuilder.durable(HEALTH_QUEUE).build();
    }

    @Bean
    public Binding healthBinding() {

        return BindingBuilder.bind(healthQueue()).to(healthExchange()).withQueueName();
    }

}
