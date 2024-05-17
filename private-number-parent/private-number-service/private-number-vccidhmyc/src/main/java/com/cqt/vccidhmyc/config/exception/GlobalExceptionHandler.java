package com.cqt.vccidhmyc.config.exception;

import com.cqt.vccidhmyc.web.model.vo.CallDispatcherVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletResponse;

/**
 * @author linshiqiang
 * date:  2023-04-03 16:03
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(value = Exception.class)
    public CallDispatcherVO exception(Exception e, HttpServletResponse response) {
        log.error("参数 异常捕获exception: ", e);
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        return CallDispatcherVO.error("500", "异常: " + e.getMessage());
    }
}
