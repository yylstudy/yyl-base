package com.cqt.hmbc.service;

import com.cqt.hmbc.retry.BaseRetryInfo;

/**
 * DelayQueueRetryPolicy
 *
 * @author Xienx
 * @date 2023年02月24日 11:07
 */
public interface DelayQueueRetryPolicy<T extends BaseRetryInfo> {

    /**
     * 是否能进行重试
     *
     * @param retryCount  当前尝试次数
     * @param maxAttempts 最大重试次数
     * @return boolean 能否重试的结果
     */
    default boolean canRetry(int retryCount, int maxAttempts) {
        return retryCount <= maxAttempts;
    }

    /**
     * 重试执行的方法
     *
     * @param param 请求参数
     */
    void retryHandle(T param);

    /**
     * 重试丢弃的方法
     *
     * @param param 请求参数
     */
    void discardHandle(T param);
}
