package com.cqt.sms.mqRend;


import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.CustomExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * @program:
 * @description: 美团状态推送要延迟mq队列初始化
 * @author: yy
 * @create: 2019-07-01 15:42
 **/


@Slf4j
@Component
public class MqConfig {

    final static String DELAY_ROUTING_KEY_XDELAY = "delay_private_num_sms_routing";
    final static String DELAYED_EXCHANGE_XDELAY = "delay_private_num_sms_exchange";
    //延迟队列，延迟时间结束后，写入该队列
    final static String IMMEDIATE_QUEUE_XDELAY = "private_num_sms_queue";

    // 创建一个立即消费队列
    @Bean
    public Queue immediateQueue() {
        // 第一个参数是创建的queue的名字，第二个参数是是否支持持久化
        return new Queue(IMMEDIATE_QUEUE_XDELAY, true);
    }

    @Bean
    public CustomExchange delayExchange() {
        Map<String, Object> args = new HashMap<String, Object>();
        args.put("x-delayed-type", "direct");
        return new CustomExchange(DELAYED_EXCHANGE_XDELAY, "x-delayed-message", true, false, args);
    }

    @Bean
    public Binding bindingNotify() {
        return BindingBuilder.bind(immediateQueue()).to(delayExchange()).with(DELAY_ROUTING_KEY_XDELAY).noargs();
    }

}
