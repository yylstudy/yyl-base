package com.cqt.hmbc.service;

import com.cqt.hmbc.config.ThreadPoolConfig;
import com.cqt.hmbc.retry.RetryPushDTO;
import org.springframework.scheduling.annotation.Async;

import java.util.List;

/**
 * 推送企业接口信息
 *
 * @author Xienx
 * @date 2023年02月10日 14:15
 */
public interface CorpPushService extends DelayQueueRetryPolicy<RetryPushDTO> {

    /**
     * 向企业推送拨测结果
     *
     * @param retryPushDTOList 重推参数
     */
    @Async(ThreadPoolConfig.COMMON_EXECUTOR)
    void pushBatch(List<RetryPushDTO> retryPushDTOList);

    /**
     * 向企业重新推送拨测结果
     *
     * @param retryPushDTO 重推参数
     */
    void pushWithRetry(RetryPushDTO retryPushDTO);
}
