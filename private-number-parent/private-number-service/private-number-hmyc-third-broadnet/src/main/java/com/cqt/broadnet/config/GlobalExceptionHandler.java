package com.cqt.broadnet.config;

import com.cqt.broadnet.common.model.x.vo.CallControlResponseVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * @author linshiqiang
 * date:  2023-02-15 15:39
 */
@RestControllerAdvice(basePackages = "com.cqt.broadnet.web.x.controller")
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(value = ApiAuthException.class)
    public CallControlResponseVO exception(ApiAuthException e) {
        log.error("参数 异常捕获 ApiAuthException: ", e);
        return CallControlResponseVO.fail(e.getMessage());
    }
    
    @ExceptionHandler(value = BizException.class)
    public CallControlResponseVO exception(BizException e) {
        log.error("参数 异常捕获 BizException: ", e);
        return CallControlResponseVO.fail(e.getMessage());
    }

    @ExceptionHandler(value = Exception.class)
    public CallControlResponseVO exception(Exception e) {
        log.error("参数 异常捕获exception: ", e);
        return CallControlResponseVO.fail("请求失败!");
    }
}
