package com.cqt.broadnet.config;

import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.util.StrUtil;

/**
 * @author linshiqiang
 * @since 2022-10-24 14:33
 * 业务异常
 */
public class BizException extends RuntimeException {

    public BizException(Throwable e) {
        super(ExceptionUtil.getMessage(e), e);
    }

    public BizException(String message) {
        super(message);
    }

    public BizException(String messageTemplate, Object... params) {
        super(StrUtil.format(messageTemplate, params));
    }

    public BizException(String message, Throwable throwable) {
        super(message, throwable);
    }

    public BizException(Throwable throwable, String messageTemplate, Object... params) {
        super(StrUtil.format(messageTemplate, params), throwable);
    }

    @Override
    public synchronized Throwable fillInStackTrace() {
        return this;
    }
}
