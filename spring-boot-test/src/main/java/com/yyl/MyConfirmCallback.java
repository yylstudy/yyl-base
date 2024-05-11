//package com.yyl;
//
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.amqp.rabbit.connection.CorrelationData;
//import org.springframework.amqp.rabbit.core.RabbitTemplate;
//import org.springframework.stereotype.Component;
//
///**
// * @author yang.yonglian
// * @version 1.0.0
// * @Description TODO
// * @createTime 2024/5/7 11:42
// */
//@Component
//@Slf4j
//public class MyConfirmCallback implements RabbitTemplate.ConfirmCallback{
//    @Override
//    public void confirm(CorrelationData correlationData, boolean ack, String cause) {
//        if(correlationData==null){
//            log.info("消息接收到ack：{}",ack);
//        }else{
//            log.info("消息id:{}接收到ack：{}",correlationData.getId(),ack);
//        }
//    }
//}
