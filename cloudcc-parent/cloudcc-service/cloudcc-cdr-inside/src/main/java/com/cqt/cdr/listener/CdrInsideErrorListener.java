package com.cqt.cdr.listener;

import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.UUID;


/**
 * @author xinson
 * date:  2023-08-09 19:14
 * 内部话单生成
 */
@Slf4j
//@Component
//@RocketMQMessageListener(topic = "cloudcc_insidecdr_error_topic", consumerGroup = "cloudcc_insidecdr-error-group")
@Deprecated
public class CdrInsideErrorListener implements RocketMQListener<String> {

    @Resource
    private CdrInsideListener cdrInsideListener;

    @Override
    public void onMessage(String message) {
        String LOG_TAG = UUID.randomUUID().toString() + " | 异常队列-内部话单消费 | ";
        log.info(LOG_TAG + "接收到MQ消息: " + message);
        cdrInsideListener.processMessage(message, LOG_TAG);
    }
}