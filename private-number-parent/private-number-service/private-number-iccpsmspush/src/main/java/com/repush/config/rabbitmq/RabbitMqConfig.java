package com.repush.config.rabbitmq;


import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 *
 **/


@Slf4j
@Component
public class RabbitMqConfig {

    final static String DELAY_ROUTING_KEY_XDELAY = "delay_private_num_sms_routing";
    final static String DELAYED_EXCHANGE_XDELAY = "delay_private_num_sms_exchange";
    //延迟队列，延迟时间结束后，写入该队列
    final static String IMMEDIATE_QUEUE_XDELAY = "private_num_sms_queue";

    //队列名称
    private static final String ICCPCDRSAVEQUEUES = "iccp_sms_sdr_queues";

    //交换机名称
    private static final String ICCPCDRSAVEEXCHANGE = "iccp_sms_sdr_exchange";

    //路由键
    private static final String ICCPCDRSAVEROUTEKEY = "iccp_sms_sdr_routing";

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

    /**
     * 队列
     *
     * @return
     */
    @Bean
    @Qualifier(ICCPCDRSAVEQUEUES)
    Queue queue() {
        return new Queue(ICCPCDRSAVEQUEUES, true);
    }

    /**
     * 交换器(直连类型)
     *
     * @return
     */
    @Bean
    @Qualifier(ICCPCDRSAVEEXCHANGE)
    DirectExchange exchange() {
        return new DirectExchange(ICCPCDRSAVEEXCHANGE, true, false);
    }

    /**
     * 声明绑定关系
     *
     * @return
     */
    @Bean
    Binding binding(@Qualifier(ICCPCDRSAVEEXCHANGE) DirectExchange exchange, @Qualifier(ICCPCDRSAVEQUEUES) Queue queue) {
        return BindingBuilder.bind(queue).to(exchange).with(ICCPCDRSAVEROUTEKEY);
    }
}
