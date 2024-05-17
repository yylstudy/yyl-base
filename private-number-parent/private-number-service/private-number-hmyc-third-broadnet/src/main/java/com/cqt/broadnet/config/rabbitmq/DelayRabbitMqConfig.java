package com.cqt.broadnet.config.rabbitmq;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * ClassName: DelayRabbitMqConfig
 *
 * @author linshiqiang@linewell.com
 * Date: 2021-11-27 21:51
 * Description:
 */
@Configuration
public class DelayRabbitMqConfig {

    public static final String RECYCLE_EXCHANGE_DELAY_H = "private_number_bind_recycle_exchange_delay_%sh";

    public static final String RECYCLE_QUEUE_DELAY_H = "private_number_bind_recycle_queue_delay_%sh";

    public static final String RECYCLE_EXCHANGE_DEAD_H = "private_number_bind_recycle_exchange_dead_%sh";

    public static final String RECYCLE_QUEUE_DEAD_H = "private_number_bind_recycle_queue_dead_%sh";

    public static final String RECYCLE_QUEUE_DEAD = "private_number_bind_recycle_queue_dead_#";

    /**
     * 按秒s
     */
    public static final String RECYCLE_EXCHANGE_DELAY_S = "private_number_bind_recycle_exchange_delay_%ss";

    public static final String RECYCLE_QUEUE_DELAY_S = "private_number_bind_recycle_queue_delay_%ss";

    public static final String RECYCLE_EXCHANGE_DEAD_S = "private_number_bind_recycle_exchange_dead_%ss";

    public static final String RECYCLE_QUEUE_DEAD_S = "private_number_bind_recycle_queue_dead_%ss";

    /**
     * 24h延时交换机
     */
    @Bean
    public DirectExchange delayExchange24h() {
        return new DirectExchange(String.format(RECYCLE_EXCHANGE_DELAY_H, 24));
    }

    /**
     * 24h死信交换机
     */
    @Bean
    public DirectExchange deadExchange24h() {
        return new DirectExchange(String.format(RECYCLE_EXCHANGE_DEAD_H, 24));
    }

    /**
     * 声明延时队列24h 不设置TTL
     * 并绑定到对应的死信交换机
     */
    @Bean
    public Queue delayQueue24h() {
        Map<String, Object> args = new HashMap<>(3);
        // x-dead-letter-exchange    这里声明当前队列绑定的死信交换机
        args.put("x-dead-letter-exchange", String.format(RECYCLE_EXCHANGE_DEAD_H, 24));
        // x-dead-letter-routing-key  这里声明当前队列的死信路由key
        args.put("x-dead-letter-routing-key", String.format(RECYCLE_QUEUE_DEAD_H, 24));
        //声明队列消息过期时间
        args.put("x-message-ttl", 24 * 60 * 60 * 1000);
        return QueueBuilder.durable(String.format(RECYCLE_QUEUE_DELAY_H, 24)).withArguments(args).build();
    }

    /**
     * 24h死信队列 用于接收延时24h时长处理的消息
     */
    @Bean
    public Queue deadQueue24h() {
        return QueueBuilder.durable(String.format(RECYCLE_QUEUE_DEAD_H, 24)).build();
    }

    @Bean
    public Binding delayBinding24h() {
        return BindingBuilder.bind(delayQueue24h()).to(delayExchange24h()).withQueueName();
    }

    @Bean
    public Binding deadBinding24h() {
        return BindingBuilder.bind(deadQueue24h()).to(deadExchange24h()).withQueueName();
    }

    /**
     * 4h延时交换机
     */
    @Bean
    public DirectExchange delayExchange4h() {
        return new DirectExchange(String.format(RECYCLE_EXCHANGE_DELAY_H, 4));
    }

    /**
     * 4h死信交换机
     */
    @Bean
    public DirectExchange deadExchange4h() {
        return new DirectExchange(String.format(RECYCLE_EXCHANGE_DEAD_H, 4));
    }

    /**
     * 声明延时队列4h 不设置TTL
     * 并绑定到对应的死信交换机
     */
    @Bean
    public Queue delayQueue4h() {
        Map<String, Object> args = new HashMap<>(3);
        // x-dead-letter-exchange    这里声明当前队列绑定的死信交换机
        args.put("x-dead-letter-exchange", String.format(RECYCLE_EXCHANGE_DEAD_H, 4));
        // x-dead-letter-routing-key  这里声明当前队列的死信路由key
        args.put("x-dead-letter-routing-key", String.format(RECYCLE_QUEUE_DEAD_H, 4));
        //声明队列消息过期时间
        args.put("x-message-ttl", 4 * 60 * 60 * 1000);
        return QueueBuilder.durable(String.format(RECYCLE_QUEUE_DELAY_H, 4)).withArguments(args).build();
    }

    /**
     * 4h死信队列 用于接收延时4h时长处理的消息
     */
    @Bean
    public Queue deadQueue4h() {
        return QueueBuilder.durable(String.format(RECYCLE_QUEUE_DEAD_H, 4)).build();
    }

    @Bean
    public Binding delayBinding4h() {
        return BindingBuilder.bind(delayQueue4h()).to(delayExchange4h()).withQueueName();
    }

    @Bean
    public Binding deadBinding4h() {
        return BindingBuilder.bind(deadQueue4h()).to(deadExchange4h()).withQueueName();
    }

    /**
     * 3h延时交换机
     */
    @Bean
    public DirectExchange delayExchange3h() {
        return new DirectExchange(String.format(RECYCLE_EXCHANGE_DELAY_H, 3));
    }

    /**
     * 3h死信交换机
     */
    @Bean
    public DirectExchange deadExchange3h() {
        return new DirectExchange(String.format(RECYCLE_EXCHANGE_DEAD_H, 3));
    }

    /**
     * 声明延时队列3h 不设置TTL
     * 并绑定到对应的死信交换机
     */
    @Bean
    public Queue delayQueue3h() {
        Map<String, Object> args = new HashMap<>(3);
        // x-dead-letter-exchange    这里声明当前队列绑定的死信交换机
        args.put("x-dead-letter-exchange", String.format(RECYCLE_EXCHANGE_DEAD_H, 3));
        // x-dead-letter-routing-key  这里声明当前队列的死信路由key
        args.put("x-dead-letter-routing-key", String.format(RECYCLE_QUEUE_DEAD_H, 3));
        //声明队列消息过期时间
        args.put("x-message-ttl", 3 * 60 * 60 * 1000);
        return QueueBuilder.durable(String.format(RECYCLE_QUEUE_DELAY_H, 3)).withArguments(args).build();
    }

    /**
     * 4h死信队列 用于接收延时4h时长处理的消息
     */
    @Bean
    public Queue deadQueue3h() {
        return QueueBuilder.durable(String.format(RECYCLE_QUEUE_DEAD_H, 3)).build();
    }

    @Bean
    public Binding delayBinding3h() {
        return BindingBuilder.bind(delayQueue3h()).to(delayExchange3h()).withQueueName();
    }

    @Bean
    public Binding deadBinding3h() {
        return BindingBuilder.bind(deadQueue3h()).to(deadExchange3h()).withQueueName();
    }

    /**
     * 3h延时交换机
     */
    @Bean
    public DirectExchange delayExchange1h() {
        return new DirectExchange(String.format(RECYCLE_EXCHANGE_DELAY_H, 1));
    }

    /**
     * 3h死信交换机
     */
    @Bean
    public DirectExchange deadExchange1h() {
        return new DirectExchange(String.format(RECYCLE_EXCHANGE_DEAD_H, 1));
    }

    /**
     * 声明延时队列3h 不设置TTL
     * 并绑定到对应的死信交换机
     */
    @Bean
    public Queue delayQueue1h() {
        Map<String, Object> args = new HashMap<>(3);
        // x-dead-letter-exchange    这里声明当前队列绑定的死信交换机
        args.put("x-dead-letter-exchange", String.format(RECYCLE_EXCHANGE_DEAD_H, 1));
        // x-dead-letter-routing-key  这里声明当前队列的死信路由key
        args.put("x-dead-letter-routing-key", String.format(RECYCLE_QUEUE_DEAD_H, 1));
        //声明队列消息过期时间
        args.put("x-message-ttl", 60 * 60 * 1000);
        return QueueBuilder.durable(String.format(RECYCLE_QUEUE_DELAY_H, 1)).withArguments(args).build();
    }

    /**
     * 1h死信队列 用于接收延时1h时长处理的消息
     */
    @Bean
    public Queue deadQueue1h() {
        return QueueBuilder.durable(String.format(RECYCLE_QUEUE_DEAD_H, 1)).build();
    }

    @Bean
    public Binding delayBinding1h() {
        return BindingBuilder.bind(delayQueue1h()).to(delayExchange1h()).withQueueName();
    }

    @Bean
    public Binding deadBinding1h() {
        return BindingBuilder.bind(deadQueue1h()).to(deadExchange1h()).withQueueName();
    }

}
