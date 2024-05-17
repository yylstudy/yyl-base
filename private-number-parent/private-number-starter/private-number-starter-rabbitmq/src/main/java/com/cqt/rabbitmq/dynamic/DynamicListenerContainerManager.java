package com.cqt.rabbitmq.dynamic;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.MessageListener;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author linshiqiang
 * date:  2023-02-16 10:47
 */
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "spring.rabbitmq", name = "dynamic", havingValue = "true")
public class DynamicListenerContainerManager {

    private final List<AbstractDynamicMessageListener> abstractDynamicMessageListenerList;

    private final Map<String, SimpleMessageListenerContainer> map = new ConcurrentHashMap<>();

    /**
     * 消息监控处理方法缓存,
     * key 容器名称
     * value MessageListener
     */
    private final Map<String, AbstractDynamicMessageListener> messageListenerMap = new ConcurrentHashMap<>();

    public void add(String name, SimpleMessageListenerContainer container) {
        map.put(name, container);
    }

    public void start() {
        for (SimpleMessageListenerContainer container : map.values()) {
            container.start();
        }
    }

    public void stop() {
        for (SimpleMessageListenerContainer container : map.values()) {
            container.stop();
        }
    }

    @PostConstruct
    public void cacheMessageListener() {
        for (AbstractDynamicMessageListener messageListener : abstractDynamicMessageListenerList) {
            messageListenerMap.put(messageListener.getContainerName(), messageListener);
        }
    }

    public Optional<MessageListener> getMessageListener(String containerName) {
        return Optional.ofNullable(messageListenerMap.get(containerName));
    }

}
