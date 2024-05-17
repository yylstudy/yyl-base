package com.cqt.call.manager;

import com.cqt.base.contants.RocketMqConstant;
import com.cqt.call.strategy.event.impl.ExtStatusEventStrategyImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

/**
 * @author linshiqiang
 * date:  2023-07-05 17:58
 * 分机注册状态
 */
@Slf4j
@RequiredArgsConstructor
@Component
@RocketMQMessageListener(topic = RocketMqConstant.CLOUD_CC_EXT_STATUS_TOPIC,
        consumerGroup = RocketMqConstant.EXT_STATUS_GROUP)
public class ExtStatusRegMessageConsumer implements RocketMQListener<String> {

    private final ExtStatusEventStrategyImpl extStatusEventStrategyImpl;

    @Override
    public void onMessage(String message) {
        try {
            extStatusEventStrategyImpl.deal(message);
        } catch (Exception e) {
            log.error("分机注册状态, message: {}, consumer error: ", message, e);
        }
    }

}
