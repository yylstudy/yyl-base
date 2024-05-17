package com.cqt.hmbc.service;

import com.cqt.hmbc.retry.RetryQueryDTO;
import com.cqt.model.hmbc.dto.CdrRecordSimpleEntity;

/**
 * DialTestResultService
 *
 * @author Xienx
 * @date 2023年02月09日 17:46
 */
public interface DialTestResultService extends DelayQueueRetryPolicy<RetryQueryDTO> {

    /**
     * 外呼拨测结果处理
     *
     * @param cdrRecord 原始话单数据
     */
    void dialTestResult(CdrRecordSimpleEntity cdrRecord);
}
