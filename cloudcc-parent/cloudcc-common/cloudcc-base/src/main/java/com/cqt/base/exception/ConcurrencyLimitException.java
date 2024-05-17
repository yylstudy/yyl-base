package com.cqt.base.exception;

import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.util.StrUtil;

/**
 * @author linshiqiang
 * date 2022/2/22 17:10
 * 话务并发异常
 */
public class ConcurrencyLimitException extends RuntimeException {

    public ConcurrencyLimitException(Throwable e) {
        super(ExceptionUtil.getMessage(e), e);
    }

    public ConcurrencyLimitException() {
    }

    public ConcurrencyLimitException(String message) {
        super(message);
    }

    public ConcurrencyLimitException(String messageTemplate, Object... params) {
        super(StrUtil.format(messageTemplate, params));
    }

    public ConcurrencyLimitException(String message, Throwable throwable) {
        super(message, throwable);
    }

    public ConcurrencyLimitException(Throwable throwable, String messageTemplate, Object... params) {
        super(StrUtil.format(messageTemplate, params), throwable);
    }

    @Override
    public synchronized Throwable fillInStackTrace() {
        return this;
    }
}
