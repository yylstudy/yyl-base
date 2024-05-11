package com.linkcircle.mq.common;

import org.apache.rocketmq.client.producer.SendResult;

/**
 * @author yang.yonglian
 * @version 1.0.0
 * @Description rocketmq回调
 * @createTime 2024/5/8 10:52
 */

public interface RocketmqSendCallback {
    /**
     * 成功回调
     * @param sendResult
     */
    void onSuccess(SendResult sendResult);

    /**
     * 失败回调
     * @param e
     */
    void onException(Throwable e);
}
