package com.cqt.call.manager;

import com.cqt.model.common.CloudCallCenterProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.acl.common.AclClientRPCHook;
import org.apache.rocketmq.acl.common.SessionCredentials;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.consumer.ConsumeFromWhere;
import org.apache.rocketmq.common.protocol.heartbeat.MessageModel;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Objects;

/**
 * @author linshiqiang
 * date:  2023-07-03 13:59
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EventMessageConsumerConfig implements CommandLineRunner, DisposableBean {

    private final EventMessageListenerOrderly eventMessageListenerOrderly;

    private final CloudCallCenterProperties cloudCallCenterProperties;

    private DefaultMQPushConsumer consumer;

    @Override
    public void run(String... args) throws Exception {
        consumer = eventMessageConsumer();
        consumer.start();
        log.info("EventMessageConsumer start...");
    }

    @Override
    public void destroy() {
        if (Objects.nonNull(consumer)) {
            consumer.shutdown();
        }
        log.info("EventMessageConsumer destroyed.");
    }


    private DefaultMQPushConsumer eventMessageConsumer() throws MQClientException {
        String topic = cloudCallCenterProperties.getBase().getRocketmq().getTopic();
        String tag = cloudCallCenterProperties.getBase().getRocketmq().getTag();
        DefaultMQPushConsumer consumer = getDefaultMQPushConsumer();

        consumer.setNamesrvAddr(cloudCallCenterProperties.getBase().getRocketmq().getNameServer());
        consumer.setConsumerGroup(cloudCallCenterProperties.getBase().getRocketmq().getGroup());
        consumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_LAST_OFFSET);
        consumer.subscribe(topic, tag);
        consumer.registerMessageListener(eventMessageListenerOrderly);
        consumer.setConsumeThreadMin(cloudCallCenterProperties.getBase().getRocketmq().getConsumeThreadMin());
        consumer.setConsumeThreadMax(cloudCallCenterProperties.getBase().getRocketmq().getConsumeThreadMax());
        consumer.setConsumeMessageBatchMaxSize(cloudCallCenterProperties.getBase().getRocketmq().getConsumeMessageBatchMaxSize());
        consumer.setPullBatchSize(cloudCallCenterProperties.getBase().getRocketmq().getPullBatchSize());
        consumer.setMessageModel(MessageModel.CLUSTERING);
        return consumer;
    }

    private DefaultMQPushConsumer getDefaultMQPushConsumer() {
        String accessKey = cloudCallCenterProperties.getBase().getRocketmq().getAccessKey();
        String secretKey = cloudCallCenterProperties.getBase().getRocketmq().getSecretKey();

        DefaultMQPushConsumer consumer;
        if (StringUtils.hasLength(accessKey) && StringUtils.hasLength(secretKey)) {
            AclClientRPCHook hook = new AclClientRPCHook(new SessionCredentials(accessKey, secretKey));
            consumer = new DefaultMQPushConsumer(hook);
        } else {
            consumer = new DefaultMQPushConsumer();
        }
        return consumer;
    }

}
