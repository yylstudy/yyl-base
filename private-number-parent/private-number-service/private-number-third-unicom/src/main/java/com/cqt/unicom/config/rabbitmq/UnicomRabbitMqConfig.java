package com.cqt.unicom.config.rabbitmq;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;

/**
 * @author zhengsuhao
 * @date 2022/12/7
 */
@Configuration
public class UnicomRabbitMqConfig {

    /**
     * COMM延迟队列
     */
    public static final String COMM_PUSH_DELAYED_QUEUE = "comm_push_delayed_queue";

    public static final String COMM_PUSH_DELAYED_EXCHANGE = "comm_push_delayed_exchange";

    public static final String COMM_PUSH_DELAYED_ROUTING = "comm_push_delayed_routing";

    /**
     * COMM死信队列
     */
    public static final String COMM_PUSH_DEAD_LETTER_QUEUE = "comm_push_dead_letter_queue";

    public static final String COMM_PUSH_DEAD_LETTER_EXCHANGE = "comm_push_dead_letter_exchange";

    public static final String COMM_PUSH_DEAD_LETTER_ROUTING_KEY = "comm_push_dead_letter_routingKey";

    /**
     * 延迟队列绑定死信队列
     */
    @Bean
    public Queue commPushDelayedQueue() {
        HashMap<String, Object> hashMap = new HashMap<>(2);
        //这里声明当前队列绑定的死信交换机
        hashMap.put("x-dead-letter-exchange", COMM_PUSH_DEAD_LETTER_EXCHANGE);
        //这里声明当前队列的死信路由key
        hashMap.put("x-dead-letter-routing-key", COMM_PUSH_DEAD_LETTER_ROUTING_KEY);
        // x-message-ttl  声明队列的TTL
        hashMap.put("x-message-ttl", 300000);
        return QueueBuilder.durable(COMM_PUSH_DELAYED_QUEUE).withArguments(hashMap).build();
    }

    /**
     * 延迟交换机
     */
    @Bean
    public DirectExchange commPushDelayedExchange() {
        return new DirectExchange(COMM_PUSH_DELAYED_EXCHANGE);
    }

    /**
     * 延迟队列绑定
     */
    @Bean
    public Binding commPushDelayedBinding() {
        return BindingBuilder.bind(commPushDelayedQueue()).to(commPushDelayedExchange()).with(COMM_PUSH_DELAYED_ROUTING);
    }


    /**
     * 死信队列
     */
    @Bean
    public Queue commPushDeadLetterQueue() {
        return new Queue(COMM_PUSH_DEAD_LETTER_QUEUE);
    }

    /**
     * 死信交换机
     */
    @Bean
    public DirectExchange commPushDeadLetterExchange() {
        return new DirectExchange(COMM_PUSH_DEAD_LETTER_EXCHANGE);
    }

    /**
     * 死信队列绑定
     */
    @Bean
    public Binding commPushDeadLetterBinding() {
        return BindingBuilder.bind(commPushDeadLetterQueue()).to(commPushDeadLetterExchange()).with(COMM_PUSH_DEAD_LETTER_ROUTING_KEY);
    }


    /**
     * num_push延迟队列
     */
    public static final String NUM_PUSH_DELAYED_QUEUE = "num_push_delayed_queue";

    public static final String NUM_PUSH_DELAYED_EXCHANGE = "num_push_delayed_exchange";

    public static final String NUM_PUSH_DELAYED_ROUTING = "num_push_delayed_routing";


    /**
     * NUM_PUSH死信队列
     */
    public static final String NUM_PUSH_DEAD_LETTER_QUEUE = "num_push_dead_letter_queue";

    public static final String NUM_PUSH_DEAD_LETTER_EXCHANGE = "num_push_dead_letter_exchange";

    public static final String NUM_PUSH_DEAD_LETTER_ROUTING_KEY = "num_push_dead_letter_routingKey";

    /**
     * 延迟队列绑定死信队列
     */
    @Bean
    public Queue numPushDelayedQueue() {
        HashMap<String, Object> hashMap = new HashMap<>(2);
        //这里声明当前队列绑定的死信交换机
        hashMap.put("x-dead-letter-exchange", NUM_PUSH_DEAD_LETTER_EXCHANGE);
        //这里声明当前队列的死信路由key
        hashMap.put("x-dead-letter-routing-key", NUM_PUSH_DEAD_LETTER_ROUTING_KEY);
        // x-message-ttl  声明队列的TTL
        hashMap.put("x-message-ttl", 300000);
        return QueueBuilder.durable(NUM_PUSH_DELAYED_QUEUE).withArguments(hashMap).build();
    }

    /**
     * 延迟交换机
     */
    @Bean
    public DirectExchange numPushDelayedExchange() {
        return new DirectExchange(NUM_PUSH_DELAYED_EXCHANGE);
    }

    /**
     * 延迟队列绑定
     */
    @Bean
    public Binding numPushDelayedBinding() {
        return BindingBuilder.bind(numPushDelayedQueue()).to(numPushDelayedExchange()).with(NUM_PUSH_DELAYED_ROUTING);
    }


    /**
     * 死信队列
     */
    @Bean
    public Queue numPushDeadLetterQueue() {
        return new Queue(NUM_PUSH_DEAD_LETTER_QUEUE);
    }

    /**
     * 死信交换机
     */
    @Bean
    public DirectExchange numPushDeadLetterExchange() {
        return new DirectExchange(NUM_PUSH_DEAD_LETTER_EXCHANGE);
    }

    /**
     * 死信队列绑定
     */
    @Bean
    public Binding numPushDeadLetterBinding() {
        return BindingBuilder.bind(numPushDeadLetterQueue()).to(numPushDeadLetterExchange()).with(NUM_PUSH_DEAD_LETTER_ROUTING_KEY);
    }

    /**
     * sms_PUSH延迟队列
     */
    public static final String SMS_PUSH_DELAYED_QUEUE = "sms_push_delayed_queue";

    public static final String SMS_PUSH_DELAYED_EXCHANGE = "sms_push_delayed_exchange";

    public static final String SMS_PUSH_DELAYED_ROUTING = "sms_push_delayed_routing";


    /**
     * sms_PUSH死信队列
     */
    public static final String SMS_PUSH_DEAD_LETTER_QUEUE = "sms_push_dead_letter_queue";

    public static final String SMS_PUSH_DEAD_LETTER_EXCHANGE = "sms_push_dead_letter_exchange";

    public static final String SMS_PUSH_DEAD_LETTER_ROUTING_KEY = "sms_push_dead_letter_routingKey";

    /**
     * 延迟队列绑定死信队列
     */
    @Bean
    public Queue smsPushDelayedQueue() {
        HashMap<String, Object> hashMap = new HashMap<>(2);
        //这里声明当前队列绑定的死信交换机
        hashMap.put("x-dead-letter-exchange", SMS_PUSH_DEAD_LETTER_EXCHANGE);
        //这里声明当前队列的死信路由key
        hashMap.put("x-dead-letter-routing-key", SMS_PUSH_DEAD_LETTER_ROUTING_KEY);
        // x-message-ttl  声明队列的TTL
        hashMap.put("x-message-ttl", 300000);
        return QueueBuilder.durable(SMS_PUSH_DELAYED_QUEUE).withArguments(hashMap).build();
    }

    /**
     * 延迟交换机
     */
    @Bean
    public DirectExchange smsPushDelayedExchange() {
        return new DirectExchange(SMS_PUSH_DELAYED_EXCHANGE);
    }

    /**
     * 延迟队列绑定
     */
    @Bean
    public Binding smsPushDelayedBinding() {
        return BindingBuilder.bind(smsPushDelayedQueue()).to(smsPushDelayedExchange()).with(SMS_PUSH_DELAYED_ROUTING);
    }


    /**
     * 死信队列
     */
    @Bean
    public Queue smsPushDeadLetterQueue() {
        return new Queue(SMS_PUSH_DEAD_LETTER_QUEUE);
    }

    /**
     * 死信交换机
     */
    @Bean
    public DirectExchange smsPushDeadLetterExchange() {
        return new DirectExchange(SMS_PUSH_DEAD_LETTER_EXCHANGE);
    }

    /**
     * 死信队列绑定
     */
    @Bean
    public Binding smsPushDeadLetterBinding() {
        return BindingBuilder.bind(smsPushDeadLetterQueue()).to(smsPushDeadLetterExchange()).with(SMS_PUSH_DEAD_LETTER_ROUTING_KEY);
    }


//    public static final String TEST_QUEUE = "test_queue";
//
//    public static final String TEST_EXCHANGE = "test_exchange";
//
//    public static final String TEST_KEY = "test_routingKey";
//
//    /**
//     * 死信队列
//     */
//    @Bean
//    public Queue testQueue() {
//        return new Queue(TEST_QUEUE);
//    }
//
//    /**
//     * 死信交换机
//     */
//    @Bean
//    public DirectExchange testExchange() {
//        return new DirectExchange(TEST_EXCHANGE);
//    }
//
//    /**
//     * 死信队列绑定
//     */
//    @Bean
//    public Binding testBinding() {
//        return BindingBuilder.bind(testQueue()).to(testExchange()).with(TEST_KEY);
//    }
}
