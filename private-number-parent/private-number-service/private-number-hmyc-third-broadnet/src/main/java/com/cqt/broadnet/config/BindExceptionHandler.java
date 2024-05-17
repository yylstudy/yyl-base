package com.cqt.broadnet.config;

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
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

/**
 * 绑定接口异常处理
 *
 * @author Xienx
 * @date 2023-05-30 10:54:10:54
 */
@Slf4j
@RestControllerAdvice(basePackages = "com.cqt.broadnet.web.bind.controller")
public class BindExceptionHandler {

    @ExceptionHandler(value = HttpMediaTypeNotSupportedException.class)
    public Result httpMediaTypeNotSupportedException(HttpMediaTypeNotSupportedException e) {
        log.error("参数 HttpMediaTypeNotSupportedException: ", e);
        return Result.fail(ErrorCodeEnum.OTHER_ERROR.getCode(), e.getMessage());
    }

    @ExceptionHandler(value = BindException.class)
    public Result bindException(BindException e) {
        BindingResult bindingResult = e.getBindingResult();
        log.error("参数 异常捕获bindException: {}", getErrorMessage(bindingResult.getAllErrors()));
        return getError(bindingResult.getAllErrors());
    }

    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public Result methodArgumentNotValidException(MethodArgumentNotValidException e) {
        BindingResult bindingResult = e.getBindingResult();
        log.error("参数 异常捕获methodArgumentNotValidException: {}", getErrorMessage(bindingResult.getAllErrors()));
        return getError(bindingResult.getAllErrors());
    }

    @ExceptionHandler(value = HttpMessageNotReadableException.class)
    public Result httpMessageNotReadableException(HttpMessageNotReadableException e) {
        log.error("请求body异常, 异常捕获 httpMessageNotReadableException: {}", e.getMessage());
        return Result.fail(ErrorCodeEnum.OTHER_ERROR.getCode(), "请求body异常");
    }

    @ExceptionHandler(value = HttpRequestMethodNotSupportedException.class)
    public Result httpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException e) {
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

    @ResponseStatus
    @ExceptionHandler(value = RedisClusterDownException.class)
    public Result redisClusterDownException(RedisClusterDownException e) {
        log.error("RedisClusterDownException: ", e);
        return Result.fail(500, "系统异常!");
    }

    @ResponseStatus
    @ExceptionHandler(value = RedisException.class)
    public Result redisException(RedisException e) {
        log.error("RedisException: ", e);
        return Result.fail(500, "系统异常!");
    }

    @ResponseStatus
    @ExceptionHandler(value = Exception.class)
    public Result exception(Exception e) {
        log.error("参数 异常捕获exception: ", e);
        return Result.fail(500, "Unknown Error!");
    }
}
