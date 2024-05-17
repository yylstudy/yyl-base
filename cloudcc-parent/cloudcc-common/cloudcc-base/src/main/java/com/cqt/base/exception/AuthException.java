package com.cqt.base.exception;

import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.util.StrUtil;
import com.cqt.base.enums.SdkErrCode;
import lombok.Getter;

/**
 * @author linshiqiang
 * date 2022/2/22 17:10
 * 鉴权异常
 */
@Getter
public class AuthException extends RuntimeException {

    private SdkErrCode sdkErrCode;

    public AuthException(SdkErrCode sdkErrCode) {
        this.sdkErrCode = sdkErrCode;
    }

    public AuthException(SdkErrCode sdkErrCode, Throwable e) {
        super(sdkErrCode.getName(), e);
        this.sdkErrCode = sdkErrCode;
    }

    public AuthException(Throwable e) {
        super(ExceptionUtil.getMessage(e), e);
    }

    public AuthException() {
    }

    public AuthException(String message) {
        super(message);
    }

    public AuthException(String messageTemplate, Object... params) {
        super(StrUtil.format(messageTemplate, params));
    }

    public AuthException(String message, Throwable throwable) {
        super(message, throwable);
    }

    public AuthException(Throwable throwable, String messageTemplate, Object... params) {
        super(StrUtil.format(messageTemplate, params), throwable);
    }

    @Override
    public synchronized Throwable fillInStackTrace() {
        return this;
    }
}
