package com.cqt.base.exception;

import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.util.StrUtil;

/**
 * @author linshiqiang
 * date 2022/2/22 17:10
 * 呼叫异常
 */
public class CallException extends RuntimeException {

    public CallException(Throwable e) {
        super(ExceptionUtil.getMessage(e), e);
    }

    public CallException() {
    }

    public CallException(String message) {
        super(message);
    }

    public CallException(String messageTemplate, Object... params) {
        super(StrUtil.format(messageTemplate, params));
    }

    public CallException(String message, Throwable throwable) {
        super(message, throwable);
    }

    public CallException(Throwable throwable, String messageTemplate, Object... params) {
        super(StrUtil.format(messageTemplate, params), throwable);
    }

    @Override
    public synchronized Throwable fillInStackTrace() {
        return this;
    }
}
