package com.cqt.monitor.web.callevent.rabbitmq;

import com.cqt.monitor.web.callevent.entity.Callstat;
import com.cqt.monitor.web.callevent.entity.EventInMin;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SenderJob {

    @Autowired
    private AmqpTemplate rabbitTemplate;

    @Async("threadPool")
    public void send(EventInMin mesg) {

        rabbitTemplate.convertAndSend("private_warn_event_exchange", "private_warn_event_routing", mesg);
    }

    public void sendTest(Callstat mesg) {
        rabbitTemplate.convertAndSend("private_call_event_stats_queues", mesg);
    }
}
