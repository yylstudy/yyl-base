package com.cqt.rabbitmq.dynamic;

import com.cqt.rabbitmq.util.RabbitmqUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.MessageListener;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.boot.autoconfigure.amqp.RabbitProperties;

/**
 * @author linshiqiang
 * date:  2023-02-16 10:33
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DynamicContainerFactoryBean implements FactoryBean<SimpleMessageListenerContainer> {

    private AmqpAdmin amqpAdmin;

    private RabbitmqUtil rabbitmqUtil;

    private MessageListener listener;

    private RabbitProperties rabbitProperties;

    private ConnectionFactory connectionFactory;

    /**
     * 监听队列名称
     */
    private String queueName;

    /**
     * 设置容器初始化后是否自动启动。 默认为“true”;将其设置为“false”以允许通过start()方法手动启动。
     */
    private Boolean autoStartup;

    private AcknowledgeMode acknowledgeMode;

    @Override
    public SimpleMessageListenerContainer getObject() throws Exception {
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        container.setQueueNames(queueName);
        container.setAutoStartup(autoStartup);
        container.setMessageListener(listener);
        container.setAmqpAdmin(amqpAdmin);
        container.setAutoDeclare(true);
        container.setAcknowledgeMode(acknowledgeMode);
        container.setConnectionFactory(connectionFactory);
        RabbitProperties.SimpleContainer simpleContainer = rabbitProperties.getListener().getSimple();
        container.setPrefetchCount(simpleContainer.getPrefetch());
        container.setConcurrency(simpleContainer.getConcurrency().toString());
        return container;
    }

    @Override
    public Class<?> getObjectType() {
        return SimpleMessageListenerContainer.class;
    }

}
