package com.linkcircle.mq.util;

import com.linkcircle.mq.annotation.MqMessageListener;
import com.linkcircle.mq.config.MqApplicationContext;
import com.linkcircle.mq.config.MqConfiguration;
import com.linkcircle.mq.common.MqException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.rabbit.listener.AbstractMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.MessageListenerContainer;
import org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistry;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author yang.yonglian
 * @version 1.0.0
 * @Description TODO
 * @createTime 2024/5/7 17:05
 */

public class MqUtil {
    private static Map<Class,Class> genericMap = new ConcurrentHashMap<>();
    private static Map<Class,AcknowledgeMode> rabbitmqAckModeMap = new ConcurrentHashMap<>();

    /**
     * 获取MqListener泛型类型
     * @param clazz
     * @return
     */
    public static Class getGeneric(Class clazz){
        return genericMap.computeIfAbsent(clazz,key->{
            Type[] genericInterfaces = clazz.getGenericInterfaces();
            if(genericInterfaces.length>1){
                throw new MqException("mqListener中存在多个泛型");
            }
            if(genericInterfaces.length==0){
                return String.class;
            }
            if (!(genericInterfaces[0] instanceof ParameterizedType)) {
                return String.class;
            }
            ParameterizedType parameterizedType = (ParameterizedType) genericInterfaces[0];
            Type[] types = parameterizedType.getActualTypeArguments();
            if(types.length>1){
                throw new MqException("mqListener中actualTypeArguments存在多个泛型");
            }
            return (Class) types[0];
        });
    }

    /**
     * 获取rabbitmq的队列名称
     * @param clazz
     * @return
     */
    public static AcknowledgeMode getRabbitmqAckMode(Class clazz){
        return rabbitmqAckModeMap.computeIfAbsent(clazz,key->{
            MqMessageListener messageListener = (MqMessageListener)clazz.getAnnotation(MqMessageListener.class);
            String queueName = messageListener.target();
            AbstractMessageListenerContainer messageListenerContainer = getMessageListenerContainerByQueueName(queueName);
            return messageListenerContainer.getAcknowledgeMode();
        });
    }

    private static AbstractMessageListenerContainer getMessageListenerContainerByQueueName(String queueName){
        RabbitListenerEndpointRegistry registry = MqApplicationContext.getBean(RabbitListenerEndpointRegistry.class);
        Collection<MessageListenerContainer> messageListenerContainers = registry.getListenerContainers();
        AbstractMessageListenerContainer messageListenerContainer = (AbstractMessageListenerContainer)messageListenerContainers.stream()
                .filter(container-> Arrays.stream(((AbstractMessageListenerContainer) container)
                .getQueueNames()).anyMatch(queue-> StringUtils.equals(queue,queueName))).findFirst().get();
        return messageListenerContainer;
    }




}
