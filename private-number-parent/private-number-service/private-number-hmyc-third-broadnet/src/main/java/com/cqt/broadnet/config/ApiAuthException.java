package com.cqt.broadnet.config;

import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.util.StrUtil;

/**
 * @author linshiqiang
 * @since 2022-10-24 14:33
 * 接口鉴权异常
 */
public class ApiAuthException extends RuntimeException {

    private static final long serialVersionUID = -7304314115297216409L;

    public ApiAuthException(Throwable e) {
        super(ExceptionUtil.getMessage(e), e);
    }

    public ApiAuthException(String message) {
        super(message);
    }

    public ApiAuthException(String messageTemplate, Object... params) {
        super(StrUtil.format(messageTemplate, params));
    }

    public ApiAuthException(String message, Throwable throwable) {
        super(message, throwable);
    }

    public ApiAuthException(Throwable throwable, String messageTemplate, Object... params) {
        super(StrUtil.format(messageTemplate, params), throwable);
    }

    @Override
    public synchronized Throwable fillInStackTrace() {
        return this;
    }
}
