package com.cqt.rabbitmq.config;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.connection.RabbitConnectionFactoryBean;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.RabbitListenerContainerFactory;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.PropertyMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * @author linshiqiang
 * @since 2022/8/8 10:44
 */
@ComponentScan(basePackages = "com.cqt")
@Configuration
@ConditionalOnClass(EnableRabbit.class)
@RequiredArgsConstructor
public class MultiRabbitmqConfig {

    private final BindRabbitProperties bindRabbitProperties;

    @Bean
    public Jackson2JsonMessageConverter jackson2MessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * 业务mq
     */
    @Bean
    public RabbitTemplate rabbitTemplate(final ConnectionFactory connectionFactory) {
        CachingConnectionFactory cachingConnectionFactory = (CachingConnectionFactory) connectionFactory;
        cachingConnectionFactory.getRabbitConnectionFactory().setRequestedChannelMax(65535);
        final RabbitTemplate rabbitTemplate = new RabbitTemplate(cachingConnectionFactory);
        rabbitTemplate.setMessageConverter(jackson2MessageConverter());
        return rabbitTemplate;
    }

    /**
     * 绑定mq
     */
    @Bean("bindRabbitTemplate")
    @ConditionalOnProperty(prefix = "spring.rabbitmq-bind", name = "active", havingValue = "true")
    public RabbitTemplate bindRabbitTemplate() throws Exception {
        CachingConnectionFactory cachingConnectionFactory = bindCachingConnectionFactory();
        cachingConnectionFactory.getRabbitConnectionFactory().setRequestedChannelMax(65535);
        final RabbitTemplate rabbitTemplate = new RabbitTemplate(cachingConnectionFactory);
        rabbitTemplate.setMessageConverter(jackson2MessageConverter());
        return rabbitTemplate;
    }

    /**
     * 绑定mq 连接工厂
     */
    @SuppressWarnings("all")
    public CachingConnectionFactory bindCachingConnectionFactory() throws Exception {
        PropertyMapper map = PropertyMapper.get();
        CachingConnectionFactory factory = new CachingConnectionFactory(getRabbitConnectionFactoryBean().getObject());
        map.from(bindRabbitProperties::determineAddresses).to(factory::setAddresses);
        map.from(bindRabbitProperties::isPublisherReturns).to(factory::setPublisherReturns);
        map.from(bindRabbitProperties::getPublisherConfirmType).whenNonNull().to(factory::setPublisherConfirmType);
        BindRabbitProperties.Cache.Channel channel = bindRabbitProperties.getCache().getChannel();
        map.from(channel::getSize).whenNonNull().to(factory::setChannelCacheSize);
        map.from(channel::getCheckoutTimeout).whenNonNull().as(Duration::toMillis)
                .to(factory::setChannelCheckoutTimeout);
        BindRabbitProperties.Cache.Connection connection = bindRabbitProperties.getCache().getConnection();
        map.from(connection::getMode).whenNonNull().to(factory::setCacheMode);
        map.from(connection::getSize).whenNonNull().to(factory::setConnectionCacheSize);
        return factory;
    }

    /**
     * 绑定mq 监听工厂
     * <p>
     *
     * @RabbitListener(containerFactory = "bindRabbitListenerContainerFactory", queues = "xxx")
     * public void message(Channel channel, Message message) throws IOException {
     * log.info("{}", new String(message.getBody()));
     * channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
     * }
     * </p>
     */
    @SuppressWarnings("all")
    @Bean("bindRabbitListenerContainerFactory")
    @ConditionalOnProperty(prefix = "spring.rabbitmq-bind", name = "active", havingValue = "true")
    public RabbitListenerContainerFactory rabbitListenerContainerFactory() throws Exception {
        CachingConnectionFactory cachingConnectionFactory = bindCachingConnectionFactory();
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(cachingConnectionFactory);
        BindRabbitProperties.Listener listener = bindRabbitProperties.getListener();
        BindRabbitProperties.SimpleContainer simple = listener.getSimple();
        factory.setBatchSize(simple.getBatchSize());
        factory.setConcurrentConsumers(simple.getConcurrency());
        factory.setMaxConcurrentConsumers(simple.getMaxConcurrency());
        factory.setAcknowledgeMode(AcknowledgeMode.MANUAL);
        return factory;
    }

    /**
     * 绑定mq连接工厂bean
     */
    private RabbitConnectionFactoryBean getRabbitConnectionFactoryBean() {
        PropertyMapper map = PropertyMapper.get();
        RabbitConnectionFactoryBean factory = new RabbitConnectionFactoryBean();
        map.from(bindRabbitProperties::determineHost).whenNonNull().to(factory::setHost);
        map.from(bindRabbitProperties::determinePort).to(factory::setPort);
        map.from(bindRabbitProperties::determineUsername).whenNonNull().to(factory::setUsername);
        map.from(bindRabbitProperties::determinePassword).whenNonNull().to(factory::setPassword);
        map.from(bindRabbitProperties::determineVirtualHost).whenNonNull().to(factory::setVirtualHost);
        map.from(bindRabbitProperties::getRequestedHeartbeat).whenNonNull().asInt(Duration::getSeconds)
                .to(factory::setRequestedHeartbeat);
        map.from(bindRabbitProperties::getRequestedChannelMax).to(factory::setRequestedChannelMax);
        map.from(bindRabbitProperties::getConnectionTimeout).whenNonNull().asInt(Duration::toMillis)
                .to(factory::setConnectionTimeout);
        factory.afterPropertiesSet();
        return factory;
    }

}
