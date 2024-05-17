package com.cqt.hmbc.retry;

import lombok.Getter;
import lombok.Setter;

/**
 * BaseRetry
 *
 * @author Xienx
 * @date 2023年02月24日 9:59
 */
@Getter
@Setter
public abstract class BaseRetryInfo {

    private Integer retryCount = 1;

    public void incrRetryCount() {
        this.retryCount += 1;
    }

    /**
     * 返回当前业务重试标识
     *
     * @return 返回当前业务标识
     */
    public abstract String getBizId();

}
