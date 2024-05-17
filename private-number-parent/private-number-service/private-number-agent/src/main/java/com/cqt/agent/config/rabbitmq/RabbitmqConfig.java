package com.cqt.agent.config.rabbitmq;

import cn.hutool.core.net.NetUtil;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author linshiqiang
 * @date 2022/5/16 14:15
 */
@Configuration
public class RabbitmqConfig {

    /**
     * 媒体文件队列
     */
    public static final String PRIVATE_MEDIA_FILE_QUEUE = "private-media-file-queue_" + NetUtil.getLocalhostStr();

    /**
     * 媒体文件交换机
     */
    public static final String PRIVATE_MEDIA_FILE_EXCHANGE = "private-media-file-exchange";


    @Bean
    public FanoutExchange mediaFileExchange() {

        return new FanoutExchange(PRIVATE_MEDIA_FILE_EXCHANGE, true, false);
    }

    @Bean
    public Queue mediaFileQueue() {

        return new Queue(PRIVATE_MEDIA_FILE_QUEUE, true);
    }

    @Bean
    public Binding mediaFileBinding() {

        return BindingBuilder.bind(mediaFileQueue()).to(mediaFileExchange());
    }
}
