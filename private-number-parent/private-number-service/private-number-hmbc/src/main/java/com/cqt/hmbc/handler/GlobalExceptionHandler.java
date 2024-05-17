package com.cqt.hmbc.handler;

import com.baomidou.mybatisplus.core.exceptions.MybatisPlusException;
import com.cqt.model.common.Result;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.exceptions.PersistenceException;
import org.mybatis.spring.MyBatisSystemException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.connection.PoolException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.NoHandlerFoundException;

import javax.transaction.SystemException;
import java.util.List;

/**
 * 异常处理器
 *
 * @author scott
 * @date 2019
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 处理自定义异常
     */
    @ExceptionHandler(SystemException.class)
    public Result systemException(SystemException e) {
        log.error(e.getMessage(), e);
        return Result.fail(500, e.getMessage());
    }

    @ExceptionHandler(BadSqlGrammarException.class)
    public Result badSqlGrammarException(BadSqlGrammarException e) {
        log.error(e.getMessage(), e);
        return Result.fail(500, "数据不存在!");
    }

    @ExceptionHandler(MybatisPlusException.class)
    public Result mybatisPlusException(MybatisPlusException e) {
        log.error(e.getMessage(), e);
        return Result.fail(500, "数据不存在!");
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public Result illegalArgumentException(IllegalArgumentException e) {
        log.error("illegalArgumentException: ", e);
        return Result.fail(500, "参数非法!");
    }

    @ExceptionHandler(MyBatisSystemException.class)
    public Result myBatisSystemException(MyBatisSystemException e) {
        log.error(e.getMessage(), e);
        return Result.fail(500, "数据异常!");
    }

    @ExceptionHandler(NullPointerException.class)
    public Result nullPointerException(NullPointerException e) {
        log.error(e.getMessage(), e);
        return Result.fail(500, "系统繁忙!");
    }

    @ExceptionHandler(PersistenceException.class)
    public Result persistenceException(PersistenceException e) {
        log.error(e.getMessage(), e);
        return Result.fail(500, "数据不存在!");
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
        return Result.fail(500, "请求体异常");
    }

    private Result getError(List<ObjectError> allErrors) {
        StringBuilder message = new StringBuilder();
        for (ObjectError error : allErrors) {
            message.append(error.getDefaultMessage()).append(" && ");
        }
        // 因为&两边空格
        String msg = message.substring(0, message.length() - 3);
        log.error(msg);
        return Result.fail(500, msg);
    }

    private String getErrorMessage(List<ObjectError> allErrors) {
        StringBuilder message = new StringBuilder();
        for (ObjectError error : allErrors) {
            message.append(error.getDefaultMessage()).append(" && ");
        }
        return message.substring(0, message.length() - 3);
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public Result handlerNoFoundException(Exception e) {
        log.error(e.getMessage(), e);
        return Result.fail(404, "路径不存在，请检查路径是否正确");
    }

    @ExceptionHandler(DuplicateKeyException.class)
    public Result handleDuplicateKeyException(DuplicateKeyException e) {
        log.error(e.getMessage(), e);
        return Result.fail(500, "数据库中已存在该记录");
    }

    @ExceptionHandler(RuntimeException.class)
    public Result handleRuntimeException(Exception e) {
        log.error(e.getMessage(), e);
        return Result.fail(500, "操作失败，" + e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public Result handleException(Exception e) {
        log.error(e.getMessage(), e);
        return Result.fail(500, "操作失败，" + e.getMessage());
    }

    /**
     * @param e HttpRequestMethodNotSupportedException
     * @return Result 异常信息
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public Result httpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException e) {
        StringBuilder sb = new StringBuilder();
        sb.append("不支持");
        sb.append(e.getMethod());
        sb.append("请求方法，");
        sb.append("支持以下");
        String[] methods = e.getSupportedMethods();
        if (methods != null) {
            for (String str : methods) {
                sb.append(str);
                sb.append("、");
            }
        }
        log.error(sb.toString(), e);
        return Result.fail(405, sb.toString());
    }

    /**
     * spring默认上传大小100MB 超出大小捕获异常MaxUploadSizeExceededException
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public Result handleMaxUploadSizeExceededException(MaxUploadSizeExceededException e) {
        log.error(e.getMessage(), e);
        return Result.fail(500, "文件大小超出10MB限制, 请压缩或降低文件质量! ");
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public Result handleDataIntegrityViolationException(DataIntegrityViolationException e) {
        log.error(e.getMessage(), e);
        return Result.fail(500, "字段太长,超出数据库字段的长度");
    }

    @ExceptionHandler(PoolException.class)
    public Result handlePoolException(PoolException e) {
        log.error(e.getMessage(), e);
        return Result.fail(500, "Redis 连接异常!");
    }

}
