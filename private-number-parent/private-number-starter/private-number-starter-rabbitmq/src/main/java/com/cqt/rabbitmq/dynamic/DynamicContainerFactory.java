package com.cqt.rabbitmq.dynamic;

import com.cqt.rabbitmq.util.RabbitmqUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.MessageListener;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.boot.autoconfigure.amqp.RabbitProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author linshiqiang
 * date:  2023-02-16 11:05
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "spring.rabbitmq", name = "dynamic", havingValue = "true")
public class DynamicContainerFactory {

    private final RabbitmqUtil rabbitmqUtil;

    private final AmqpAdmin amqpAdmin;

    private final ConnectionFactory connectionFactory;

    private final RabbitProperties rabbitProperties;

    private final DynamicListenerContainerManager dynamicListenerContainerManager;

    private final Map<String, DynamicContainerFactoryBean> dynamicContainerFactoryBeanMap = new ConcurrentHashMap<>();

    /**
     * 创建监听容器
     * 已知交换机和队列已创建
     *
     * @param containerName 容器名称
     * @param queueName     队列名称
     * @param autoStartup   是否项目自启
     * @throws Exception 异常
     */
    public void createListenerContainer(String containerName, String queueName, Boolean autoStartup) throws Exception {
        Optional<MessageListener> messageListenerOptional = dynamicListenerContainerManager.getMessageListener(containerName);
        if (!messageListenerOptional.isPresent()) {
            return;
        }
        DynamicContainerFactoryBean dynamicContainerFactoryBean = DynamicContainerFactoryBean.builder()
                .rabbitmqUtil(rabbitmqUtil)
                .amqpAdmin(amqpAdmin)
                .listener(messageListenerOptional.get())
                .queueName(queueName)
                .connectionFactory(connectionFactory)
                .acknowledgeMode(AcknowledgeMode.MANUAL)
                .autoStartup(autoStartup)
                .rabbitProperties(rabbitProperties)
                .build();
        SimpleMessageListenerContainer container = dynamicContainerFactoryBean.getObject();
        dynamicListenerContainerManager.add(containerName, container);
        dynamicContainerFactoryBeanMap.put(containerName, dynamicContainerFactoryBean);
    }

    public void start() {
        dynamicListenerContainerManager.start();
    }

    public void stop() {
        dynamicListenerContainerManager.stop();
    }
}
