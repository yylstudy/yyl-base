package com.cqt.recycle.config;

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
 * date:  2022-12-20 14:42
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(value = HttpMediaTypeNotSupportedException.class)
    public Result httpMediaTypeNotSupportedException(HttpMediaTypeNotSupportedException e, HttpServletResponse response) {
        log.error("参数 HttpMediaTypeNotSupportedException: ", e);
        return Result.fail(ErrorCodeEnum.OTHER_ERROR.getCode(), e.getMessage());
    }

    @ExceptionHandler(value = BindException.class)
    public Result bindException(BindException e, HttpServletResponse response) {
        BindingResult bindingResult = e.getBindingResult();
        log.error("参数 异常捕获bindException: {}", getErrorMessage(bindingResult.getAllErrors()));
        return getError(bindingResult.getAllErrors());
    }

    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public Result methodArgumentNotValidException(MethodArgumentNotValidException e, HttpServletResponse response) {
        BindingResult bindingResult = e.getBindingResult();
        log.error("参数 异常捕获methodArgumentNotValidException: {}", getErrorMessage(bindingResult.getAllErrors()));
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
        return Result.fail(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "系统异常!");
    }

    @ExceptionHandler(value = RedisException.class)
    public Result redisException(RedisException e, HttpServletResponse response) {
        log.error("RedisException: ", e);
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        return Result.fail(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "系统异常!");
    }

    @ExceptionHandler(value = Exception.class)
    public Result exception(Exception e, HttpServletResponse response) {
        log.error("参数 异常捕获exception: ", e);
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        return Result.fail(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unknown Error!");
    }
}
