package com.linkcircle.mq.producer;

import com.linkcircle.mq.common.MqException;
import com.linkcircle.mq.common.RocketmqLocalTransactionState;
import com.linkcircle.mq.common.RocketmqSendCallback;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.client.producer.*;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.apache.rocketmq.spring.support.RocketMQHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

/**
 * @author yang.yonglian
 * @version 1.0.0
 * @Description TODO
 * @createTime 2024/5/6 14:27
 */

public class RocketMqProducer implements MqProducer{
    @Autowired
    private RocketMQTemplate rocketMQTemplate;
    @Value("${rocketmq.producer.send-message-timeout:3000}")
    private long sendMessageTimeout;
    @Override
    public void asyncSendMessage(String destination, Object payload) {
        rocketMQTemplate.asyncSend(destination,payload,defaultNullSendCallback());
    }
    @Override
    public void asyncSendMessage(String destination, Object payload, String key) {
        Message message = MessageBuilder.withPayload(payload).setHeader(RocketMQHeaders.KEYS,key).build();
        rocketMQTemplate.asyncSend(destination,message,defaultNullSendCallback());
    }

    @Override
    public void asyncSendDelayMessage(String destination, Object payload, int delay) {
        Message message = MessageBuilder.withPayload(payload).build();
        rocketMQTemplate.asyncSend(destination,message,defaultNullSendCallback(),sendMessageTimeout,delay);
    }

    @Override
    public void asyncSendDelayMessage(String destination, Object payload, int delay, String key) {
        Message message = MessageBuilder.withPayload(payload).setHeader(RocketMQHeaders.KEYS,key).build();
        rocketMQTemplate.asyncSend(destination,message,defaultNullSendCallback(),sendMessageTimeout,delay);
    }

    @Override
    public void asyncSendExpireMessage(String destination, Object payload, int expire) {
        throw new MqException("rocketmq不支持过期时间消息");
    }

    @Override
    public void asyncSendExpireMessage(String destination, Object payload, int expire, String key) {
        throw new MqException("rocketmq不支持过期时间消息");
    }

    @Override
    public void asyncSendMessage(String destination, Object payload, Integer delay, String key, RocketmqSendCallback rocketmqSendCallback) {
        MessageBuilder builder = MessageBuilder.withPayload(payload);
        if(StringUtils.isNotBlank(key)){
            builder.setHeader(RocketMQHeaders.KEYS,key);
        }
        Message message = builder.build();
        SendCallback sendCallback = convertSendCallback(rocketmqSendCallback);
        if(delay==null){
            rocketMQTemplate.asyncSend(destination, message,sendCallback, sendMessageTimeout);
        }else{
            rocketMQTemplate.asyncSend(destination, message,sendCallback, sendMessageTimeout,delay);
        }
    }


    @Override
    public boolean syncSendMessage(String destination, Object payload) {
        SendResult sendResult = rocketMQTemplate.syncSend(destination,payload);
        return SendStatus.SEND_OK==sendResult.getSendStatus();
    }

    @Override
    public boolean syncSendMessage(String destination, Object payload, String key) {
        Message message = MessageBuilder.withPayload(payload).setHeader(RocketMQHeaders.KEYS,key).build();
        SendResult sendResult = rocketMQTemplate.syncSend(destination,message);
        return SendStatus.SEND_OK==sendResult.getSendStatus();
    }

    @Override
    public boolean syncSendDelayMessage(String destination, Object payload, int delay) {
        Message message = MessageBuilder.withPayload(payload).build();
        SendResult sendResult = rocketMQTemplate.syncSend(destination,message,sendMessageTimeout,delay);
        return SendStatus.SEND_OK==sendResult.getSendStatus();
    }

    @Override
    public boolean syncSendDelayMessage(String destination, Object payload, int delay, String key) {
        Message message = MessageBuilder.withPayload(payload).setHeader(RocketMQHeaders.KEYS,key).build();
        SendResult sendResult = rocketMQTemplate.syncSend(destination,message,sendMessageTimeout,delay);
        return SendStatus.SEND_OK==sendResult.getSendStatus();
    }

    @Override
    public RocketmqLocalTransactionState sendTransactionMessage(String destination, Object payload, String key, Object args) {
        if(StringUtils.isEmpty(destination)){
            throw new MqException("消息投递失败，目的地为空");
        }
        String topicName = destination.split(":")[0];
        MessageBuilder builder = MessageBuilder.withPayload(payload);
        if(StringUtils.isNotBlank(key)){
            builder.setHeader(RocketMQHeaders.KEYS,key);
        }
        builder.setHeader(RocketMQHeaders.TOPIC,topicName).build();
        Message message = builder.build();
        TransactionSendResult transactionSendResult = rocketMQTemplate.sendMessageInTransaction(destination, message,args);
        LocalTransactionState localTransactionState = transactionSendResult.getLocalTransactionState();
        if(localTransactionState==LocalTransactionState.COMMIT_MESSAGE){
            return RocketmqLocalTransactionState.COMMIT_MESSAGE;
        }else if(localTransactionState==LocalTransactionState.ROLLBACK_MESSAGE){
            return RocketmqLocalTransactionState.ROLLBACK_MESSAGE;
        }else if(localTransactionState==LocalTransactionState.UNKNOW){
            return RocketmqLocalTransactionState.UNKNOW;
        }
        return null;
    }

    @Override
    public boolean syncSendOrderlyMessage(String destination, Object payload, String hashKey) {
        SendResult sendResult = rocketMQTemplate.syncSendOrderly(destination,payload,hashKey);
        return SendStatus.SEND_OK==sendResult.getSendStatus();
    }

    @Override
    public boolean syncSendOrderlyMessage(String destination, Object payload, String hashKey, String key) {
        Message message = MessageBuilder.withPayload(payload).setHeader(RocketMQHeaders.KEYS,key).build();
        SendResult sendResult = rocketMQTemplate.syncSendOrderly(destination,message,hashKey);
        return SendStatus.SEND_OK==sendResult.getSendStatus();
    }

    @Override
    public void asyncSendOrderlyMessage(String destination, Object payload, String hashKey) {
        rocketMQTemplate.asyncSendOrderly(destination,payload,hashKey,defaultNullSendCallback());
    }

    @Override
    public void asyncSendOrderlyMessage(String destination, Object payload, String hashKey, String key, RocketmqSendCallback rocketmqSendCallback) {
        Message message = MessageBuilder.withPayload(payload).setHeader(RocketMQHeaders.KEYS,key).build();
        SendCallback sendCallback = convertSendCallback(rocketmqSendCallback);
        rocketMQTemplate.asyncSendOrderly(destination,message,hashKey,sendCallback);
    }

    private SendCallback defaultNullSendCallback(){
        SendCallback sendCallback = new SendCallback() {
            @Override
            public void onSuccess(SendResult sendResult) {

            }
            @Override
            public void onException(Throwable e) {

            }
        };
        return sendCallback;
    }


    private SendCallback convertSendCallback(RocketmqSendCallback rocketmqSendCallback){
        if(rocketmqSendCallback==null){
            return null;
        }
        SendCallback sendCallback = new SendCallback() {
            @Override
            public void onSuccess(SendResult sendResult) {
                rocketmqSendCallback.onSuccess(sendResult);
            }
            @Override
            public void onException(Throwable e) {
                rocketmqSendCallback.onException(e);
            }
        };
        return sendCallback;
    }

}
