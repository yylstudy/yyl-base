package com.cqt.hmyc.web.x.rabbitmq;

import com.cqt.hmyc.web.model.hdh.push.HdhPushIccpDTO;
import com.cqt.hmyc.web.model.hdh.push.HdhXPushIccpDTO;
import com.cqt.model.push.entity.PrivateBillInfo;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DelayProducer {

    @Autowired
    private AmqpTemplate rabbitTemplate;

    public void send(HdhXPushIccpDTO msg) {
        this.rabbitTemplate.convertAndSend(AxbBillDelayConfig.DELAY_EXCHANGE_NAME, AxbBillDelayConfig.DELAY_QUEUEB_ROUTING_KEY, msg, new MessagePostProcessor() {
            @Override
            public Message postProcessMessage(Message message) throws AmqpException {
                message.getMessageProperties().setDelay( 60 * 1000);
                return message;
            }
        });
    }
}
