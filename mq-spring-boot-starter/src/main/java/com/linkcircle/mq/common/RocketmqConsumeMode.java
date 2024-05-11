package com.linkcircle.mq.common;

/**
 * @author yang.yonglian
 * @version 1.0.0
 * @Description TODO
 * @createTime 2024/5/8 11:41
 */

public enum RocketmqConsumeMode {
    /**
     * Receive asynchronously delivered messages concurrently
     */
    CONCURRENTLY,

    /**
     * Receive asynchronously delivered messages orderly. one queue, one thread
     */
    ORDERLY;
}
