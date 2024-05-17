package com.cqt.base.exception;

import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.util.StrUtil;
import com.cqt.base.enums.SdkErrCode;
import lombok.Getter;

/**
 * @author linshiqiang
 * date 2022/2/22 17:10
 * 请求底层异常
 */
@Getter
public class BaseReqException extends RuntimeException {

    private SdkErrCode sdkErrCode;

    public BaseReqException(SdkErrCode sdkErrCode, Throwable e) {
        super(sdkErrCode.getName(), e);
        this.sdkErrCode = sdkErrCode;
    }

    public BaseReqException(SdkErrCode sdkErrCode) {
        this.sdkErrCode = sdkErrCode;
    }

    public BaseReqException(Throwable e) {
        super(ExceptionUtil.getMessage(e), e);
    }

    public BaseReqException() {
    }

    public BaseReqException(String message) {
        super(message);
    }

    public BaseReqException(String messageTemplate, Object... params) {
        super(StrUtil.format(messageTemplate, params));
    }

    public BaseReqException(String message, Throwable throwable) {
        super(message, throwable);
    }

    public BaseReqException(Throwable throwable, String messageTemplate, Object... params) {
        super(StrUtil.format(messageTemplate, params), throwable);
    }

    @Override
    public synchronized Throwable fillInStackTrace() {
        return this;
    }
}
