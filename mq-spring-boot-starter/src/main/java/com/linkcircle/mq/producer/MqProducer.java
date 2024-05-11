package com.linkcircle.mq.producer;

import com.linkcircle.mq.common.RocketmqLocalTransactionState;
import com.linkcircle.mq.common.RocketmqSendCallback;

/**
 * @author yang.yonglian
 * @version 1.0.0
 * @Description TODO
 * @createTime 2024/5/6 14:25
 */

public interface MqProducer {
    /**
     * 异步发送消息方法
     * @param destination 消息投递地址 rocketmq格式：topicName:tags|||rabbitmq格式：exchange:routingkey
     * @param payload 消息体
     */
    void asyncSendMessage(String destination, Object payload);

    /**
     * 异步发送消息方法
     * @param destination 消息投递地址 rocketmq格式：topicName:tags|||rabbitmq格式：exchange:routingkey
     * @param payload 消息体
     * @param key 消息唯一标识 rocketmq：消息key，rabbitmq:CorrelationData的ID
     */
    void asyncSendMessage(String destination, Object payload,String key);

    /**
     * 异步发送延迟消息
     * @param destination 消息投递地址 rocketmq格式：topicName:tags|||rabbitmq格式：exchange:routingkey
     * @param payload 消息体
     * @param delay 延迟，rocketmq：延迟等级参考RocketmqDelayLevel，rabbitmq为实际延迟时间
     */
    void asyncSendDelayMessage(String destination, Object payload,int delay);

    /**
     * 异步发送延迟消息
     * @param destination
     * @param payload
     * @param delay
     * @param key
     */
    void asyncSendDelayMessage(String destination, Object payload,int delay,String key);

    /**
     * 发送带有过期时间的消息 仅rocketmq支持
     * @param destination
     * @param payload
     * @param expire
     */
    void asyncSendExpireMessage(String destination, Object payload,int expire);

    /**
     * 异步发送带有过期时间的消息 仅rocketmq支持
     * @param destination
     * @param payload
     * @param expire
     * @param key
     */
    void asyncSendExpireMessage(String destination, Object payload,int expire,String key);

    /**
     * 异步发送回调消息 仅rocketmq支持
     * @param destination destination 消息投递地址 topicName:tags
     * @param payload 消息体
     * @param delay 延迟级别 参考RocketmqDelayLevel，为空则为正常消息
     * @param key 消息key，可为空
     * @param sendCallback 回调方法
     */
    void asyncSendMessage(String destination, Object payload, Integer delay, String key, RocketmqSendCallback sendCallback);

    /**
     * 同步发送消息，仅rocketmq支持
     * @param destination 消息投递地址 rocketmq格式：topicName:tags
     * @param payload 消息体
     * @return
     */
    boolean syncSendMessage(String destination, Object payload);

    /**
     * 同步发送消息，仅rocketmq支持
     * @param destination 消息投递地址 rocketmq格式：topicName:tags
     * @param payload 消息体
     * @param key  rocketmq：消息key
     * @return
     */
    boolean syncSendMessage(String destination, Object payload,String key);


    /**
     * 同步发送延迟消息，仅rocketmq支持
     * @param destination 消息投递地址 rocketmq格式：topicName:tags
     * @param payload 消息体
     * @param delay 延迟等级参考RocketmqDelayLevel
     */
    boolean syncSendDelayMessage(String destination, Object payload,int delay);

    /**
     * 同步发送延迟消息，仅rocketmq支持
     * @param destination 消息投递地址 rocketmq格式：topicName:tags
     * @param payload 消息体
     * @param delay 延迟等级参考RocketmqDelayLevel
     * @param key 消息唯一标识
     */
    boolean syncSendDelayMessage(String destination, Object payload,int delay,String key);

    /**
     * 发送事务消息 仅rocketmq支持
     * @param destination 消息投递地址 rocketmq格式：topicName:tags
     * @param payload 消息体
     * @param key 消息唯一标识
     * @param args 参数
     * @return
     */
    RocketmqLocalTransactionState sendTransactionMessage(String destination, Object payload, String key, Object args);

    /**
     * 同步发送顺序消息 仅rocketmq支持
     * @param destination 消息投递地址 rocketmq格式：topicName:tags
     * @param payload 消息体
     * @param hashKey 选择队列hash，例如订单ID
     * @return
     */
    boolean syncSendOrderlyMessage(String destination, Object payload, String hashKey);

    /**
     * 同步发送顺序消息 仅rocketmq支持
     * @param destination 消息投递地址 rocketmq格式：topicName:tags
     * @param payload 消息体
     * @param hashKey 选择队列hash，例如订单ID
     * @param key 消息唯一标识
     * @return
     */
    boolean syncSendOrderlyMessage(String destination, Object payload, String hashKey,String key);

    /**
     * 异步发送顺序消息 仅rocketmq支持
     * @param destination 消息投递地址 rocketmq格式：topicName:tags
     * @param payload 消息体
     * @param hashKey 选择队列hash，例如订单ID
     */
    void asyncSendOrderlyMessage(String destination, Object payload, String hashKey);

    /**
     * 异步发送顺序消息 仅rocketmq支持
     * @param destination 消息投递地址 rocketmq格式：topicName:tags
     * @param payload 消息体
     * @param hashKey 选择队列hash，例如订单ID
     * @param key 消息唯一标识
     * @param rocketmqSendCallback 回调器
     */
    void asyncSendOrderlyMessage(String destination, Object payload, String hashKey,String key, RocketmqSendCallback rocketmqSendCallback);


}
