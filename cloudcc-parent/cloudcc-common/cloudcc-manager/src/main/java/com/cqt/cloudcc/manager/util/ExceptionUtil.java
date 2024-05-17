package com.cqt.cloudcc.manager.util;

import com.cqt.base.enums.SdkErrCode;
import com.cqt.base.exception.BaseReqException;
import com.cqt.base.exception.BizException;
import com.cqt.base.exception.ConcurrencyLimitException;
import com.cqt.base.exception.ParamsException;
import com.cqt.model.client.base.ClientResponseBaseVO;

import java.util.Objects;

/**
 * @author linshiqiang
 * date:  2023-11-29 14:43
 */
public class ExceptionUtil {

    /**
     * 异常处理
     *
     * @param e 异常
     * @return 结果
     */
    public static ClientResponseBaseVO exceptionControl(Exception e) {
        if (e instanceof ConcurrencyLimitException) {
            return ClientResponseBaseVO.fail(SdkErrCode.CONCURRENCY_LIMIT_MAX.getCode(), e.getMessage());
        }
        if (e instanceof ParamsException) {
            return ClientResponseBaseVO.fail(SdkErrCode.PARAM_ERROR.getCode(), e.getMessage());
        }
        if (e instanceof BizException) {
            SdkErrCode sdkErrCode = ((BizException) e).getSdkErrCode();
            if (Objects.nonNull(sdkErrCode)) {
                return ClientResponseBaseVO.fail(sdkErrCode);
            }
            return ClientResponseBaseVO.fail(SdkErrCode.SYSTEM_EXCEPTION);
        }
        if (e instanceof BaseReqException) {
            SdkErrCode sdkErrCode = ((BaseReqException) e).getSdkErrCode();
            if (Objects.nonNull(sdkErrCode)) {
                return ClientResponseBaseVO.fail(sdkErrCode);
            }
            return ClientResponseBaseVO.fail(SdkErrCode.SYSTEM_EXCEPTION);
        }
        return ClientResponseBaseVO.fail(SdkErrCode.REQUEST_ERROR);
    }
}
