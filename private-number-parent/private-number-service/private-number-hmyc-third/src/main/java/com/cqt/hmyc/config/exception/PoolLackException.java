package com.cqt.hmyc.config.exception;

import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.util.StrUtil;

/**
 * @author linshiqiang
 * @date 2022/2/22 17:10
 * 号码池不足异常
 */
public class PoolLackException extends RuntimeException{

    public PoolLackException(Throwable e) {
        super(ExceptionUtil.getMessage(e), e);
    }

    public PoolLackException() {
    }

    public PoolLackException(String message) {
        super(message);
    }

    public PoolLackException(String messageTemplate, Object... params) {
        super(StrUtil.format(messageTemplate, params));
    }

    public PoolLackException(String message, Throwable throwable) {
        super(message, throwable);
    }

    public PoolLackException(Throwable throwable, String messageTemplate, Object... params) {
        super(StrUtil.format(messageTemplate, params), throwable);
    }

    @Override
    public synchronized Throwable fillInStackTrace() {
        return this;
    }
}
