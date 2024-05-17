package com.cqt.hmyc.web.bind.manager;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
/**
 * 往队列发送数据
 *
 * @author dingsh
 * @date 2022/08/01
 */
@Component
public class MqSender {
    @Autowired
    private RabbitTemplate rabbitTemplate;


    public void send(Object msg,String exchangeName,String routingKey,long time) {

        this.rabbitTemplate.convertAndSend(exchangeName, routingKey, msg, message -> {
            // TODO 如果配置了 params.put("x-message-ttl", 5 * 1000); 那么这一句也可以省略,具体根据业务需要是声明 Queue 的时候就指定好延迟时间还是在发送自己控制时间
           // message.getMessageProperties().setExpiration(time * 1000+"");
            return message;
        });

    }
}
