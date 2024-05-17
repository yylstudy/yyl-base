package com.cqt.broadnet.web.axb.rabbitmq;

import com.cqt.model.push.entity.PrivateBillInfo;
import com.cqt.model.sms.save.SmsRequest;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class DelayProducer {

    @Autowired
    private AmqpTemplate rabbitTemplate;

    public void send(PrivateBillInfo msg) {
        this.rabbitTemplate.convertAndSend(AxbBillDelayConfig.DELAY_EXCHANGE_NAME, AxbBillDelayConfig.DELAY_QUEUEB_ROUTING_KEY, msg, new MessagePostProcessor() {
            @Override
            public Message postProcessMessage(Message message) throws AmqpException {
                message.getMessageProperties().setDelay( 5 * 60 * 1000);
                return message;
            }
        });
    }

    @Async
    public void send(SmsRequest mesg) {

        this.rabbitTemplate.convertAndSend("iccp_sms_sdr_exchange", "iccp_sms_sdr_routing", mesg);
    }
}
