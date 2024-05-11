package com.linkcircle.mq.config;

import com.linkcircle.mq.annotation.MqMessageListener;
import com.linkcircle.mq.common.MqConstant;
import com.linkcircle.mq.listener.MqListener;
import org.apache.rocketmq.client.AccessChannel;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.MessageModel;
import org.apache.rocketmq.spring.autoconfigure.ListenerContainerConfiguration;
import org.apache.rocketmq.spring.autoconfigure.RocketMQProperties;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.apache.rocketmq.spring.core.RocketMQReplyListener;
import org.apache.rocketmq.spring.support.DefaultRocketMQListenerContainer;
import org.apache.rocketmq.spring.support.RocketMQMessageConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.aop.scope.ScopedProxyUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.support.BeanDefinitionValidationException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * @author yang.yonglian
 * @version 1.0.0
 * @Description TODO
 * @createTime 2024/5/6 10:48
 */
@Configuration
@ConditionalOnClass(name={"org.apache.rocketmq.client.MQAdmin"})
public class CustomListenerContainerConfiguration implements ApplicationContextAware, SmartInitializingSingleton {
    private final static Logger log = LoggerFactory.getLogger(CustomListenerContainerConfiguration.class);

    private ConfigurableApplicationContext applicationContext;

    private AtomicLong counter = new AtomicLong(0);

    private StandardEnvironment environment;

    private RocketMQProperties rocketMQProperties;

    private RocketMQMessageConverter rocketMQMessageConverter;

    public CustomListenerContainerConfiguration(RocketMQMessageConverter rocketMQMessageConverter,
                                          StandardEnvironment environment, RocketMQProperties rocketMQProperties) {
        this.rocketMQMessageConverter = rocketMQMessageConverter;
        this.environment = environment;
        this.rocketMQProperties = rocketMQProperties;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = (ConfigurableApplicationContext) applicationContext;
    }

    @Override
    public void afterSingletonsInstantiated() {
        Map<String, Object> beans = this.applicationContext.getBeansWithAnnotation(MqMessageListener.class)
                .entrySet().stream().filter(entry -> !ScopedProxyUtils.isScopedTarget(entry.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        beans.forEach(this::registerContainer);
    }

    private void registerContainer(String beanName, Object bean) {
        Class<?> clazz = AopProxyUtils.ultimateTargetClass(bean);

        if (MqListener.class.isAssignableFrom(bean.getClass()) && RocketMQReplyListener.class.isAssignableFrom(bean.getClass())) {
            throw new IllegalStateException(clazz + " cannot be both instance of " + RocketMQListener.class.getName() + " and " + RocketMQReplyListener.class.getName());
        }

        if (!MqListener.class.isAssignableFrom(bean.getClass()) && !RocketMQReplyListener.class.isAssignableFrom(bean.getClass())) {
            throw new IllegalStateException(clazz + " is not instance of " + RocketMQListener.class.getName() + " or " + RocketMQReplyListener.class.getName());
        }

        MqMessageListener annotation = clazz.getAnnotation(MqMessageListener.class);

        String consumerGroup = this.environment.resolvePlaceholders(annotation.consumerGroup());
        String topic = this.environment.resolvePlaceholders(annotation.target());

        boolean listenerEnabled =
                (boolean) rocketMQProperties.getConsumer().getListeners().getOrDefault(consumerGroup, Collections.EMPTY_MAP)
                        .getOrDefault(topic, true);

        if (!listenerEnabled) {
            log.debug(
                    "Consumer Listener (group:{},topic:{}) is not enabled by configuration, will ignore initialization.",
                    consumerGroup, topic);
            return;
        }
        validate(annotation);

        String containerBeanName = String.format("%s_%s", DefaultRocketMQListenerContainer.class.getName(),
                counter.incrementAndGet());
        GenericApplicationContext genericApplicationContext = (GenericApplicationContext) applicationContext;

        genericApplicationContext.registerBean(containerBeanName, CustomDefaultRocketMQListenerContainer.class,
                () -> createRocketMQListenerContainer(containerBeanName, bean, annotation));
        CustomDefaultRocketMQListenerContainer container = genericApplicationContext.getBean(containerBeanName,
                CustomDefaultRocketMQListenerContainer.class);
        if (!container.isRunning()) {
            try {
                container.start();
            } catch (Exception e) {
                log.error("Started container failed. {}", container, e);
                throw new RuntimeException(e);
            }
        }

        log.info("Register the listener to container, listenerBeanName:{}, containerBeanName:{}", beanName, containerBeanName);
    }

    private CustomDefaultRocketMQListenerContainer createRocketMQListenerContainer(String name, Object bean,
                                                                             MqMessageListener annotation) {
        CustomDefaultRocketMQListenerContainer container = new CustomDefaultRocketMQListenerContainer();

        container.setRocketMQMessageListener(annotation);

        String nameServer = environment.resolvePlaceholders(MqConstant.NAME_SERVER_PLACEHOLDER);
        nameServer = StringUtils.isEmpty(nameServer) ? rocketMQProperties.getNameServer() : nameServer;
        String accessChannel = environment.resolvePlaceholders(MqConstant.ACCESS_CHANNEL_PLACEHOLDER);
        container.setNameServer(nameServer);
        if (!StringUtils.isEmpty(accessChannel)) {
            container.setAccessChannel(AccessChannel.valueOf(accessChannel));
        }
        container.setTopic(environment.resolvePlaceholders(annotation.target()));
        String tags = environment.resolvePlaceholders(annotation.tags());
        if (!StringUtils.isEmpty(tags)) {
            container.setSelectorExpression(tags);
        }
        container.setConsumerGroup(environment.resolvePlaceholders(annotation.consumerGroup()));
        if (MqListener.class.isAssignableFrom(bean.getClass())) {
            container.setRocketMQListener((MqListener) bean);
        } else if (RocketMQReplyListener.class.isAssignableFrom(bean.getClass())) {
            container.setRocketMQReplyListener((RocketMQReplyListener) bean);
        }
        container.setMessageConverter(rocketMQMessageConverter.getMessageConverter());
        container.setName(name);

        return container;
    }

    private void validate(MqMessageListener annotation) {
//        if (annotation.consumeMode() == ConsumeMode.ORDERLY &&
//                MessageModel.CLUSTERING == MessageModel.BROADCASTING) {
//            throw new BeanDefinitionValidationException(
//                    "Bad annotation definition in @RocketMQMessageListener, messageModel BROADCASTING does not support ORDERLY message!");
//        }
    }
}
