package com.linkcircle.basecom.exception;

import com.linkcircle.basecom.common.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.stream.Collectors;

/**
 * @Description:
 * @Author: yang.yonglian
 * @CreateDate: 2024/3/1 20:18
 * @Version: 1.0
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler implements Ordered {

    @ExceptionHandler(NoHandlerFoundException.class)
    public Result<?> handlerNoFoundException(NoHandlerFoundException e) {
        log.error(e.getMessage(), e);
        return Result.error(404,"请求路径:"+e.getRequestURL()+"不存在，请检查路径是否正确");
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public Result<?> HttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException e){
        StringBuffer sb = new StringBuffer();
        sb.append("不支持");
        sb.append(e.getMethod());
        sb.append("请求方法，");
        sb.append("支持以下");
        String [] methods = e.getSupportedMethods();
        if(methods!=null){
            for(String str:methods){
                sb.append(str);
                sb.append("、");
            }
        }
        log.error(sb.toString(), e);
        return Result.error(405,sb.toString());
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    public Result handleArgumentNotValidException(Exception e){
        BindingResult bindingResult;
        if(e instanceof MethodArgumentNotValidException){
            bindingResult = ((MethodArgumentNotValidException) e).getBindingResult();
        }else{
            bindingResult = ((BindException)e).getBindingResult();
        }
        String errorMsg = bindingResult.getFieldErrors().stream().map(FieldError::getDefaultMessage).collect(Collectors.joining(","));
        Result result = Result.error(errorMsg);
        return result;
    }

    /**
     * 业务异常
     */
    @ExceptionHandler(BusinessException.class)
    public Result<?> businessExceptionHandler(BusinessException e) {
        return Result.error(e.getMessage());
    }

    /**
     * 未登录异常
     */
    @ExceptionHandler(NoAuthException.class)
    public Result<?> noAuthExceptionHandler(NoAuthException e) {
        return Result.error(e.getCode(), e.getMessage());
    }

    /**
     * 其他全部异常
     * @param e 全局异常
     * @return 错误结果
     */
    @ExceptionHandler(Exception.class)
    public Result<?> errorHandler(Exception e) {
        log.error("执行异常",e);
        if(StringUtils.hasText(e.getMessage())){
            return Result.error(e.getMessage());
        }
        return Result.error(e.toString());
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
