package com.yyl;

import com.linkcircle.mq.transaction.CommonRocketMQLocalTransactionListener;
import com.linkcircle.mq.transaction.RocketMQLocalTransactionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

/**
 * @author yang.yonglian
 * @version 1.0.0
 * @Description TODO
 * @createTime 2024/5/8 11:21
 */
@Component
@Slf4j
@ConditionalOnClass(name = "org.apache.rocketmq.client.MQAdmin")
public class MyRocketMQLocalTransactionService implements RocketMQLocalTransactionService {
    @Override
    public String getTopic() {
        return "transaction_exchange";
    }

    @Override
    public void executeLocalTransaction(Message msg, Object arg) {
        log.info("agr:{}",arg);
        log.info("执行本地事务");
    }

    @Override
    public boolean checkLocalTransaction(Message msg) {
        log.info("agr:{}",msg.getPayload());
        log.info("检查本地事务");
        return true;
    }
}
