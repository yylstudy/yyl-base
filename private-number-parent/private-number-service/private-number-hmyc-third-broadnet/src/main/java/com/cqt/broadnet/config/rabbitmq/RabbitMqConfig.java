package com.cqt.broadnet.config.rabbitmq;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author linshiqiang
 * @date 2021/9/12 14:35
 */
@Configuration
public class RabbitMqConfig {

    /**
     * 延时交换机
     */
    public static final String BIND_AXB_RECYCLE_EXCHANGE = "private-axb-bind-recycle-exchange";

    public static final String BIND_AX_RECYCLE_EXCHANGE = "private-ax-bind-recycle-exchange";

    public static final String BIND_AXBN_RECYCLE_EXCHANGE = "private-axbn-bind-recycle-exchange";


    public static final String BIND_AXE_RECYCLE_EXCHANGE = "private-axe-bind-recycle-exchange";

    public static final String BIND_AXEBN_RECYCLE_EXCHANGE = "private-axebn-bind-recycle-exchange";

    public static final String BIND_RECYCLE_EXCHANGE = "private-%s-bind-recycle-exchange";

    public static final String BIND_RECYCLE_DELAY_QUEUE = "private-bind-recycle-%s-delay-queue";

    /**
     * AXB 回收延时队列
     */
    public static final String BIND_RECYCLE_AXB_DELAY_QUEUE = "private-bind-recycle-axb-delay-queue";

    /**
     * AXE 回收延时队列
     */
    public static final String BIND_RECYCLE_AXE_DELAY_QUEUE = "private-bind-recycle-axe-delay-queue";

    /**
     * AX 回收延时队列
     */
    public static final String BIND_RECYCLE_AX_DELAY_QUEUE = "private-bind-recycle-ax-delay-queue";

    /**
     * AXBN 回收延时队列
     */
    public static final String BIND_RECYCLE_AXBN_DELAY_QUEUE = "private-bind-recycle-axbn-delay-queue";

    /**
     * AXEBN 回收延时队列
     */
    public static final String BIND_RECYCLE_AXEBN_DELAY_QUEUE = "private-bind-recycle-axebn-delay-queue";

    @Bean
    public DirectExchange bindAxbRecycleCustomerExchange() {
        DirectExchange directExchange = new DirectExchange(BIND_AXB_RECYCLE_EXCHANGE, true, false);
        directExchange.setDelayed(true);
        return directExchange;
    }

    @Bean
    public DirectExchange bindAxRecycleCustomerExchange() {
        DirectExchange directExchange = new DirectExchange(BIND_AX_RECYCLE_EXCHANGE, true, false);
        directExchange.setDelayed(true);
        return directExchange;
    }

    @Bean
    public DirectExchange bindAxbnRecycleCustomerExchange() {
        DirectExchange directExchange = new DirectExchange(BIND_AXBN_RECYCLE_EXCHANGE, true, false);
        directExchange.setDelayed(true);
        return directExchange;
    }

    @Bean
    public DirectExchange bindAxeRecycleCustomerExchange() {
        DirectExchange directExchange = new DirectExchange(BIND_AXE_RECYCLE_EXCHANGE, true, false);
        directExchange.setDelayed(true);
        return directExchange;
    }

    @Bean
    public DirectExchange bindAxebnRecycleCustomerExchange() {
        DirectExchange directExchange = new DirectExchange(BIND_AXEBN_RECYCLE_EXCHANGE, true, false);
        directExchange.setDelayed(true);
        return directExchange;
    }

    @Bean
    public Queue delayAxbQueueCustomer() {

        return new Queue(BIND_RECYCLE_AXB_DELAY_QUEUE, true);
    }

    @Bean
    public Queue delayAxeQueueCustomer() {

        return new Queue(BIND_RECYCLE_AXE_DELAY_QUEUE, true);
    }

    @Bean
    public Queue delayAxebnQueueCustomer() {

        return new Queue(BIND_RECYCLE_AXEBN_DELAY_QUEUE, true);
    }

    @Bean
    public Queue delayAxQueueCustomer() {

        return new Queue(BIND_RECYCLE_AX_DELAY_QUEUE, true);
    }

    @Bean
    public Queue delayAxbnQueueCustomer() {

        return new Queue(BIND_RECYCLE_AXBN_DELAY_QUEUE, true);
    }

    @Bean
    public Binding bindingAxbCustomer() {

        return BindingBuilder.bind(delayAxbQueueCustomer()).to(bindAxbRecycleCustomerExchange()).withQueueName();
    }

    @Bean
    public Binding bindingAxeCustomer() {

        return BindingBuilder.bind(delayAxeQueueCustomer()).to(bindAxeRecycleCustomerExchange()).withQueueName();
    }

    @Bean
    public Binding bindingAxCustomer() {

        return BindingBuilder.bind(delayAxQueueCustomer()).to(bindAxRecycleCustomerExchange()).withQueueName();
    }

    @Bean
    public Binding bindingAxbnCustomer() {

        return BindingBuilder.bind(delayAxbnQueueCustomer()).to(bindAxbnRecycleCustomerExchange()).withQueueName();
    }

    @Bean
    public Binding bindingAxebnCustomer() {

        return BindingBuilder.bind(delayAxebnQueueCustomer()).to(bindAxebnRecycleCustomerExchange()).withQueueName();
    }
}
