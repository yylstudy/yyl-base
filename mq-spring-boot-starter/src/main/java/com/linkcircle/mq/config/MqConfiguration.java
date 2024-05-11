package com.linkcircle.mq.config;

import com.linkcircle.mq.producer.RabbitMqProducer;
import com.linkcircle.mq.producer.RocketMqProducer;
import com.linkcircle.mq.transaction.CommonRocketMQLocalTransactionListener;
import org.apache.rocketmq.client.MQAdmin;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.amqp.RabbitTemplateConfigurer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author yang.yonglian
 * @version 1.0.0
 * @Description TODO
 * @createTime 2024/4/30 14:38
 */
@Configuration
public class MqConfiguration {
    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(RabbitTemplate.class)
    static class RabbitMqConfiguration{
        @Autowired(required = false)
        private RabbitTemplate.ConfirmCallback confirmCallback;
        @Autowired(required = false)
        private RabbitTemplate.ReturnsCallback returnsCallback;
        @Bean
        public Jackson2JsonMessageConverter jackson2MessageConverter() {
            return new Jackson2JsonMessageConverter();
        }
        @Bean
        public RabbitTemplate rabbitTemplate(RabbitTemplateConfigurer configurer,ConnectionFactory connectionFactory){
            RabbitTemplate template = new RabbitTemplate();
            configurer.configure(template, connectionFactory);
            if(confirmCallback!=null){
                template.setConfirmCallback(confirmCallback);
            }
            if(returnsCallback!=null){
                template.setReturnsCallback(returnsCallback);
            }
            return template;
        }
        @Bean
        public RabbitMqProducer rabbitMqProducer() {
            return new RabbitMqProducer();
        }
    }

    @Configuration
    @ConditionalOnClass(MQAdmin.class)
    static class RocketMqConfiguration{
        @Bean
        public RocketMqProducer rocketMqProducer() {
            return new RocketMqProducer();
        }
        @Bean
        public CommonRocketMQLocalTransactionListener commonRocketMQLocalTransactionListener() {
            return new CommonRocketMQLocalTransactionListener();
        }

    }
}
