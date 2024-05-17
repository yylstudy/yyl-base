package com.cqt.monitor.web.callevent.rabbitmq;


import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;



@Component
@Slf4j
public class PrivateWarnEventConfig {



    //队列名称-每分钟话单数据入库队列
    private static final String PRIVATE_WARN_EVENT_QUEUES="private_warn_event_queues";

    //交换机名称
    private static final String PRIVATE_WARN_EVENT_EXCHANGE="private_warn_event_exchange";

    //路由键
    private static final String PRIVATE_WARN_EVENT_ROUTEKEY="private_warn_event_routing";

    //话单队列
    private static final String PRIVATE_CALL_EVENT_STATS_QUEUES = "private_call_event_stats_queues";
    //话单队列交换机
    private static final String ICCP_CDR_SAVE_EXCHANGE = "iccp_cdr_save_exchange";




    /**
     * 队列
     */
    @Bean
    @Qualifier(PRIVATE_CALL_EVENT_STATS_QUEUES)
    Queue statsQueue() {
        return new Queue(PRIVATE_CALL_EVENT_STATS_QUEUES, true);
    }

    /**
     * 交换器
     */
    @Bean
    @Qualifier(ICCP_CDR_SAVE_EXCHANGE)
    FanoutExchange statsExchange() {

        return new FanoutExchange(ICCP_CDR_SAVE_EXCHANGE, true, false);
    }
    /**
     * 声明绑定关系
     */
    @Bean
    Binding statsBinding(@Qualifier(ICCP_CDR_SAVE_EXCHANGE) FanoutExchange exchange,
                         @Qualifier(PRIVATE_CALL_EVENT_STATS_QUEUES) Queue queue) {
        return BindingBuilder.bind(queue).to(exchange);
    }

    /**
     * 队列
     *
     */
    @Bean
    @Qualifier(PRIVATE_WARN_EVENT_QUEUES)
    Queue queue() {
        return new Queue(PRIVATE_WARN_EVENT_QUEUES, true);
    }

    /**
     * 交换器(直连类型)
     *
     */
    @Bean
    @Qualifier(PRIVATE_WARN_EVENT_EXCHANGE)
    DirectExchange exchange() {
        return new DirectExchange(PRIVATE_WARN_EVENT_EXCHANGE, true, false);
    }
    /**
     * 声明绑定关系
     *
     */
    @Bean
    Binding binding(@Qualifier(PRIVATE_WARN_EVENT_EXCHANGE) DirectExchange exchange,
                    @Qualifier(PRIVATE_WARN_EVENT_QUEUES) Queue queue) {
        return BindingBuilder.bind(queue).to(exchange).with(PRIVATE_WARN_EVENT_ROUTEKEY);
    }



}
