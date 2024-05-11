//package com.yyl;
//
//import lombok.extern.slf4j.Slf4j;
//import org.apache.curator.framework.recipes.queue.QueueBuilder;
//import org.springframework.amqp.core.*;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
//import java.util.HashMap;
//
///**
// * @author yang.yonglian
// * @version 1.0.0
// * @Description TODO
// * @createTime 2024/5/7 13:59
// */
//@Configuration
//@Slf4j
//public class RabbitmqConfig {
//    /**
//     * 声明正常队列
//     * @return
//     */
//    @Bean
//    public Queue normalQueue() {
//        return new Queue("normal_queue");
//    }
//    /**
//     * 声明正常交换机
//     * @return
//     */
//    @Bean
//    public DirectExchange normalExchange() {
//        return new DirectExchange("normal_exchange");
//    }
//    /**
//     * 绑定正常交换机和队列
//     */
//    @Bean
//    public Binding bindNormalExchangeAndQueue() {
//        return BindingBuilder.bind(normalQueue()).to(normalExchange()).with("aaa");
//    }
//
//    /**
//     * 声明死信队列
//     * @return
//     */
//    @Bean
//    public Queue deadLetterQueue() {
//        return new Queue("dead_letter_queue");
//    }
//    /**
//     * 声明死信交换机
//     * @return
//     */
//    @Bean
//    public DirectExchange deadLetterExchange() {
//        return new DirectExchange("dead_letter_exchange");
//    }
//    /**
//     * 绑定死信交换机和队列
//     */
//    @Bean
//    public Binding bindDeadLetterExchangeAndQueue() {
//        return BindingBuilder.bind(deadLetterQueue()).to(deadLetterExchange()).with("aaa");
//    }
//
//    /**
//     * 声明过期队列
//     * @return
//     */
//    @Bean
//    public Queue expireQueue() {
//        HashMap<String, Object> params = new HashMap<>();
//        params.put("x-dead-letter-exchange", "dead_letter_exchange");
//        params.put("x-dead-letter-routing-key", "aaa");
//        return QueueBuilder
//                .durable("expire_queue")
//                .withArguments(params)
//                .build();
//    }
//    /**
//     * 声明过期交换器
//     * @return
//     */
//    @Bean
//    public DirectExchange expireExchange() {
//        return new DirectExchange("expire_exchange");
//    }
//
//    /**
//     * 绑定过期交换器和队列
//     * @return
//     */
//    @Bean
//    public Binding bindExpireExchangeAndQueue() {
//        log.info(">>>> 队列与交换器绑定");
//        return BindingBuilder.bind(expireQueue()).to(expireExchange()).with("aaa");
//    }
//
//    /**
//     * 声明延迟交换器
//     * @return
//     */
//    @Bean
//    public DirectExchange delayExchange() {
//        return ExchangeBuilder.directExchange("delay_exchange").delayed()
//                .build();
//    }
//
//    /**
//     * 绑定延迟交换器和队列
//     * @return
//     */
//    @Bean
//    public Binding delayBind() {
//        return BindingBuilder.bind(delayQueue()).to(delayExchange()).with("aaa");
//    }
//
//    /**
//     * 声明延迟队列
//     * @return
//     */
//    @Bean
//    public Queue delayQueue() {
//        return new Queue("delay_queue");
//    }
//}
