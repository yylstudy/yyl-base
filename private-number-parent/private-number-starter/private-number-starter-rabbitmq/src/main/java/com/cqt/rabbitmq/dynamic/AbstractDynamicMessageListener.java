package com.cqt.rabbitmq.dynamic;

import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.listener.api.ChannelAwareMessageListener;

/**
 * @author linshiqiang
 * date:  2023-02-16 10:48
 */
@Slf4j
public abstract class AbstractDynamicMessageListener implements ChannelAwareMessageListener {

    @Override
    public void onMessage(Message message, Channel channel) throws Exception {
        String body = new String(message.getBody());
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        try {
            doMessage(body);
        } catch (Exception e) {
            log.error("message: {}, onMessage error: ", body, e);
            errorCatch(body, e);
        } finally {
            channel.basicAck(deliveryTag, false);
        }
    }


    /**
     * 处理消息 由子类实现
     *
     * @param body 消息内容
     */
    public abstract void doMessage(String body);

    /**
     * 异常处理
     *
     * @param body 消息体
     * @param e    异常信息
     */
    public abstract void errorCatch(String body, Exception e);

    /**
     * 获取监听容器名
     *
     * @return 监听容器名
     */
    public abstract String getContainerName();
}
