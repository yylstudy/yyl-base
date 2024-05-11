package com.linkcircle.mq.common;

/**
 * @author yang.yonglian
 * @version 1.0.0
 * @Description TODO
 * @createTime 2024/5/8 13:35
 */

public enum  RocketmqLocalTransactionState {
    COMMIT_MESSAGE,
    ROLLBACK_MESSAGE,
    UNKNOW,
}
