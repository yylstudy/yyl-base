package com.cqt.hmyc.config.exception;

import com.cqt.common.enums.ErrorCodeEnum;
import com.cqt.model.common.Result;
import lombok.extern.slf4j.Slf4j;
import org.redisson.client.RedisClusterDownException;
import org.redisson.client.RedisException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * @author linshiqiang
 * @date 2021/3/29 16:26
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(value = HttpMediaTypeNotSupportedException.class)
    public Result httpMediaTypeNotSupportedException(HttpMediaTypeNotSupportedException e, HttpServletResponse response) {
        log.error("参数 HttpMediaTypeNotSupportedException: ", e);
        return Result.fail(ErrorCodeEnum.OTHER_ERROR.getCode(), e.getMessage());
    }

    @ExceptionHandler(value = AuthException.class)
    public Result authException(AuthException e) {
        log.error("参数 异常捕获 authException: ", e);
        return Result.fail(ErrorCodeEnum.AUTH_FAIL.getCode(), e.getMessage());
    }

    @ExceptionHandler(value = ParamsException.class)
    public Result paramsException(ParamsException e) {
        log.error("参数 异常捕获 paramsException: {}", e.getMessage());
        return Result.fail(ErrorCodeEnum.OTHER_ERROR.getCode(), e.getMessage());
    }

    @ExceptionHandler(value = PoolLackException.class)
    public Result poolLackException(PoolLackException e) {
        log.error("参数 异常捕获 poolLackException: ", e);
        return Result.fail(ErrorCodeEnum.POOL_LACK.getCode(), ErrorCodeEnum.POOL_LACK.getMessage());
    }

    @ExceptionHandler(value = BindException.class)
    public Result bindException(BindException e, HttpServletResponse response) {
        BindingResult bindingResult = e.getBindingResult();
        log.error("参数 异常捕获bindException: {}",  getErrorMessage(bindingResult.getAllErrors()));
        return getError(bindingResult.getAllErrors());
    }

    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public Result methodArgumentNotValidException(MethodArgumentNotValidException e, HttpServletResponse response) {
        BindingResult bindingResult = e.getBindingResult();
        log.error("参数 异常捕获methodArgumentNotValidException: {}",  getErrorMessage(bindingResult.getAllErrors()));
        return getError(bindingResult.getAllErrors());
    }

    @ExceptionHandler(value = HttpMessageNotReadableException.class)
    public Result httpMessageNotReadableException(HttpMessageNotReadableException e, HttpServletResponse response) {
        log.error("请求body异常, 异常捕获 httpMessageNotReadableException: {}", e.getMessage());
        return Result.fail(ErrorCodeEnum.OTHER_ERROR.getCode(), "请求body异常");
    }

    @ExceptionHandler(value = HttpRequestMethodNotSupportedException.class)
    public Result httpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException e, HttpServletResponse response) {
        log.error("请求类型错误, 异常捕获 HttpRequestMethodNotSupportedException: {}", e.getMessage());
        return Result.fail(ErrorCodeEnum.OTHER_ERROR.getCode(), "请求类型错误");
    }

    private Result getError(List<ObjectError> allErrors) {
        StringBuilder message = new StringBuilder();
        for (ObjectError error : allErrors) {
            message.append(error.getDefaultMessage()).append(" && ");
        }
        // 因为&两边空格
        String msg = message.substring(0, message.length() - 3);
        log.error(msg);
        if (msg.contains("expiration must")) {
            return Result.fail(ErrorCodeEnum.OVER_BIND_TIME.getCode(), "超出绑定时长, 有效期范围为0-2147483640s!");
        }
        if (msg.contains("must number")) {
            return Result.fail(ErrorCodeEnum.FORMAT_ERROR.getCode(), ErrorCodeEnum.FORMAT_ERROR.getMessage());
        }
        return Result.fail(ErrorCodeEnum.OTHER_ERROR.getCode(), msg);
    }

    private String getErrorMessage(List<ObjectError> allErrors) {
        StringBuilder message = new StringBuilder();
        for (ObjectError error : allErrors) {
            message.append(error.getDefaultMessage()).append(" && ");
        }
        return message.substring(0, message.length() - 3);
    }

    @ExceptionHandler(value = RedisClusterDownException.class)
    public Result redisClusterDownException(RedisClusterDownException e, HttpServletResponse response) {
        log.error("RedisClusterDownException: ", e);
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        return Result.fail(500, "系统异常!");
    }

    @ExceptionHandler(value = RedisException.class)
    public Result redisException(RedisException e, HttpServletResponse response) {
        log.error("RedisException: ", e);
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        return Result.fail(500, "系统异常!");
    }

    @ExceptionHandler(value = Exception.class)
    public Result exception(Exception e, HttpServletResponse response) {
        log.error("参数 异常捕获exception: ", e);
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        return Result.fail(500, "Unknown Error!");
    }
}