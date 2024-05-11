package com.linkcircle.mq.transaction;

import org.springframework.messaging.Message;

/**
 * @author yang.yonglian
 * @version 1.0.0
 * @Description rocketmq本地事务消息执行器
 * @createTime 2024/2/1 18:08
 */

public interface RocketMQLocalTransactionService {
    /**
     * 主题名称
     * @return
     */
    String getTopic();
    /**
     * 执行本地事务消息
     * @param msg
     * @param arg
     */
    void executeLocalTransaction(Message msg, Object arg);

    /**
     * 检查本地事务
     * @param msg
     */
    boolean checkLocalTransaction(Message msg);
}
