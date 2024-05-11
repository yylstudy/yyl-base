package com.linkcircle.mq.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.linkcircle.mq.util.MqUtil;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.core.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

/**
 * @author yang.yonglian
 * @version 1.0.0
 * @Description TODO
 * @createTime 2024/4/30 16:18
 */

public interface MqListener<T> {
    ObjectMapper objectMapper = new ObjectMapper();
    /**
     * rabbitmq 默认消费方法，直接在方法中加参数T t不行，rabbitmq会当初Object去处理
     * @param channel
     * @param message
     * @throws Exception
     */
    default void listenerRabbitmq(Channel channel, Message message) throws Exception{
        Class<T> clazz = MqUtil.getGeneric(this.getClass());
        T t;
        if(clazz==String.class){
            t = (T)new String(message.getBody(),"utf-8");
        }else{
            t = objectMapper.readValue(message.getBody(),clazz);
        }
        AcknowledgeMode acknowledgeMode = MqUtil.getRabbitmqAckMode(this.getClass());
        onMessage(t);
        if(acknowledgeMode==AcknowledgeMode.MANUAL){
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
        }

    }
    /**
     * 业务处理消息方法
     * @param t 消息体
     */
    void onMessage(T t);


}
