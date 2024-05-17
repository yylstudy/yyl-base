package com.cqt.hmyc.config.rabbitmq;


import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;

/**
 * @author dingsh
 * Description:
 */
@Configuration
public class DelayedPushRabbitConfig {

    /**
     * 通话话单
     */
    public static final String CDR_PUSH_DELAYED_QUEUE = "cdr_push_delayed_queue";

    public static final String CDR_PUSH_DELAYED_EXCHANGE = "cdr_push_delayed_exchange";

    public static final String CDR_PUSH_DELAYED_ROUTING = "cdr_push_delayed_routing";


    public static final String CDR_PUSH_DEAD_LETTER_QUEUE = "cdr_push_dead_letter_queue";

    public static final String CDR_PUSH_DEAD_LETTER_EXCHANGE = "cdr_push_dead_letter_exchange";

    public static final String CDR_PUSH_DEAD_LETTER_ROUTING_KEY = "cdr_push_dead_letter_routingKey";

    /**
     * 短信通话话单
     */

    public static final String CDR_SMS_PUSH_DELAYED_QUEUE = "cdr_sms_push_delayed_queue";

    public static final String CDR_SMS_PUSH_DELAYED_EXCHANGE = "cdr_sms_push_delayed_exchange";

    public static final String CDR_SMS_PUSH_DELAYED_ROUTING = "cdr_sms_push_delayed_routing";


    public static final String CDR_SMS_PUSH_DEAD_LETTER_QUEUE = "cdr_sms_push_dead_letter_queue";

    public static final String CDR_SMS_PUSH_DEAD_LETTER_EXCHANGE = "cdr_sms_push_dead_letter_exchange";

    public static final String CDR_SMS_PUSH_DEAD_LETTER_ROUTING_KEY = "cdr_sms_push_dead_letter_routingKey";

    /**
     * 死信队列
     */
    @Bean
    public Queue cdrPushDeadLetterQueue(){
        return new Queue(CDR_PUSH_DEAD_LETTER_QUEUE);
    }

    /**
     * 死信交换机
     */
    @Bean
    public DirectExchange cdrPushDeadLetterExchange(){
        return new DirectExchange(CDR_PUSH_DEAD_LETTER_EXCHANGE);
    }

    /**
     * 死信队列绑定
     */
    @Bean
    public Binding cdrPushDeadLetterBinding(){
        return BindingBuilder.bind(cdrPushDeadLetterQueue()).to(cdrPushDeadLetterExchange()).with(CDR_PUSH_DEAD_LETTER_ROUTING_KEY);
    }


    /**
     * 延迟队列绑定死信队列
     */
    @Bean
    public Queue cdrPushDelayedQueue(){
        HashMap<String, Object> hashMap = new HashMap<>(2);
        //这里声明当前队列绑定的死信交换机
        hashMap.put("x-dead-letter-exchange",CDR_PUSH_DEAD_LETTER_EXCHANGE);
        //这里声明当前队列的死信路由key
        hashMap.put("x-dead-letter-routing-key", CDR_PUSH_DEAD_LETTER_ROUTING_KEY);
        // x-message-ttl  声明队列的TTL
        hashMap.put("x-message-ttl",300000);
        return QueueBuilder.durable(CDR_PUSH_DELAYED_QUEUE).withArguments(hashMap).build();
    }

    /**
     * 延迟交换机
     */
    @Bean
    public DirectExchange cdrPushDelayedExchange(){
        return new DirectExchange(CDR_PUSH_DELAYED_EXCHANGE);
    }

    /**
     * 延迟队列绑定
     */
    @Bean
    public Binding cdrPushDelayedBinding(){
        return BindingBuilder.bind(cdrPushDelayedQueue()).to(cdrPushDelayedExchange()).with(CDR_PUSH_DELAYED_ROUTING);
    }




    /**
     * (短信)死信队列
     */
    @Bean
    public Queue cdrSmsPushDeadLetterQueue(){
        return new Queue(CDR_SMS_PUSH_DEAD_LETTER_QUEUE);
    }

    /**
     * 死信交换机
     */
    @Bean
    public DirectExchange cdrSmsPushDeadLetterExchange(){
        return new DirectExchange(CDR_SMS_PUSH_DEAD_LETTER_EXCHANGE);
    }

    /**
     * 死信队列绑定
     */
    @Bean
    public Binding cdrSmsPushDeadLetterBinding(){
        return BindingBuilder.bind(cdrSmsPushDeadLetterQueue()).to(cdrSmsPushDeadLetterExchange()).with(CDR_SMS_PUSH_DEAD_LETTER_ROUTING_KEY);
    }


    /**
     * 延迟队列绑定死信队列
     */
    @Bean
    public Queue cdrSmsPushDelayedQueue(){
        HashMap<String, Object> hashMap = new HashMap<>(2);
        //这里声明当前队列绑定的死信交换机
        hashMap.put("x-dead-letter-exchange",CDR_SMS_PUSH_DEAD_LETTER_EXCHANGE);
        //这里声明当前队列的死信路由key
        hashMap.put("x-dead-letter-routing-key", CDR_SMS_PUSH_DEAD_LETTER_ROUTING_KEY);
        // x-message-ttl  声明队列的TTL
        hashMap.put("x-message-ttl",300000);
        return QueueBuilder.durable(CDR_SMS_PUSH_DELAYED_QUEUE).withArguments(hashMap).build();
    }

    /**
     * 延迟交换机
     */
    @Bean
    public DirectExchange cdrSmsPushDelayedExchange(){
        return new DirectExchange(CDR_SMS_PUSH_DELAYED_EXCHANGE);
    }

    /**
     * 延迟队列绑定
     */
    @Bean
    public Binding cdrSmsPushDelayedBinding(){
        return BindingBuilder.bind(cdrSmsPushDelayedQueue()).to(cdrSmsPushDelayedExchange()).with(CDR_SMS_PUSH_DELAYED_ROUTING);
    }

}