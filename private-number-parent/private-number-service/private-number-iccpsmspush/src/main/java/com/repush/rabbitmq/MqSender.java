package com.repush.rabbitmq;//package com.cqt.sms.rabbitmq;
//
//import com.cqt.sms.config.rabbitmq.RabbitMqConfig;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.amqp.AmqpException;
//import org.springframework.amqp.core.Message;
//import org.springframework.amqp.core.MessageDeliveryMode;
//import org.springframework.amqp.core.MessagePostProcessor;
//import org.springframework.amqp.rabbit.connection.CorrelationData;
//import org.springframework.amqp.rabbit.core.RabbitTemplate;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Component;
//
//import java.util.UUID;
//
///**
// * @author hlx
// */
//@Component
//@Slf4j
//public class MqSender {
//
//    @Autowired
//    private RabbitTemplate rabbitTemplate;
//
//    /**
//     * confirmCallback returnCallback
//     */
//    final RabbitTemplate.ConfirmCallback confirmCallback = new RabbitTemplate.ConfirmCallback() {
//        @Override
//        public void confirm(CorrelationData correlationData, boolean ack, String cause) {
//            log.info("correlationData: {}", correlationData);
//            log.info("ack: {}", ack);
//            if (!ack) {
//                log.info("需要异常处理。。。");
//            }
//        }
//    };
//
//    final RabbitTemplate.ReturnCallback returnCallback = new RabbitTemplate.ReturnCallback() {
//        @Override
//        public void returnedMessage(Message message, int replyCode, String replyText, String exchange, String routingKey) {
//            log.info("return exchange: {}, routingKey: {}, replyCode: {}, replyText: {}"
//                    , exchange, routingKey, replyCode, replyText);
//        }
//    };
//
//    public void sendLazy(Object message) {
//        rabbitTemplate.setMandatory(true);
//        rabbitTemplate.setConfirmCallback(confirmCallback);
//        rabbitTemplate.setReturnCallback(returnCallback);
//        //id + 时间戳 全局唯一
//        CorrelationData correlationData = new CorrelationData(UUID.randomUUID().toString());
//
//        //发送消息时指定 header 延迟时间
//        rabbitTemplate.convertAndSend(RabbitMqConfig.BILL_PUSH_EXCHANGE, RabbitMqConfig.ROUTING_KEY, message,
//                new MessagePostProcessor() {
//                    @Override
//                    public Message postProcessMessage(Message message) throws AmqpException {
//                        //设置消息持久化
//                        message.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT);
//                        //message.getMessageProperties().setHeader("x-delay", "6000");
//                        message.getMessageProperties().setDelay(5 * 60 * 1000);
//                        return message;
//                    }
//                }, correlationData);
//    }
//}