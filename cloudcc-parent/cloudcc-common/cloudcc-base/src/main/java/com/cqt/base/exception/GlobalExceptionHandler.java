package com.cqt.base.exception;

import com.cqt.base.model.ResultVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletResponse;
import java.util.List;


/**
 * @author linshiqiang
 * date 2021/3/29 16:26
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(value = HttpMediaTypeNotSupportedException.class)
    public ResultVO<Void> httpMediaTypeNotSupportedException(HttpMediaTypeNotSupportedException e) {
        LOGGER.error("HttpMediaTypeNotSupportedException: ", e);
        return ResultVO.fail(400, "Content-Type not supported");
    }

    @ExceptionHandler(value = HttpMessageNotReadableException.class)
    public ResultVO<Void> httpMessageNotReadableException(HttpMessageNotReadableException e) {
        LOGGER.error("httpMessageNotReadableException: {}", e.getMessage());
        return ResultVO.fail(400, "请求body异常");
    }

    /**
     * 异常
     */
    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public ResultVO<Void> methodArgumentNotValidException(MethodArgumentNotValidException e) {
        BindingResult bindingResult = e.getBindingResult();
        LOGGER.error("methodArgumentNotValidException: {}", getErrorMessage(bindingResult.getAllErrors()));
        return getError(bindingResult.getAllErrors());
    }

    private ResultVO<Void> getError(List<ObjectError> allErrors) {
        StringBuilder message = new StringBuilder();
        for (ObjectError error : allErrors) {
            message.append(error.getDefaultMessage()).append(" && ");
        }
        // 因为&两边空格
        String msg = message.substring(0, message.length() - 3);
        LOGGER.error(msg);
        return ResultVO.fail(400, msg);
    }

    private String getErrorMessage(List<ObjectError> allErrors) {
        StringBuilder message = new StringBuilder();
        for (ObjectError error : allErrors) {
            message.append(error.getDefaultMessage()).append(" && ");
        }
        return message.substring(0, message.length() - 3);
    }

    @ExceptionHandler(value = ParamsException.class)
    public ResultVO<Void> paramsException(ParamsException e) {
        LOGGER.error("paramsException: {}", e.getMessage());
        return ResultVO.fail(400, e.getMessage());
    }

    @ExceptionHandler(value = BizException.class)
    public ResultVO<Void> bizException(BizException e, HttpServletResponse response) {
        LOGGER.error("bizException: ", e);
        response.setStatus(HttpStatus.BAD_REQUEST.value());
        return ResultVO.fail(400, e.getMessage());
    }

    @ExceptionHandler(value = AuthException.class)
    public ResultVO<Void> authException(AuthException e) {
        LOGGER.error("AuthException: ", e);
        return ResultVO.fail(403, "鉴权不通过!");
    }

    @ExceptionHandler(value = Exception.class)
    public ResultVO<Void> exception(Exception e) {
        LOGGER.error("Exception: ", e);
        return ResultVO.fail(500, "System Error!");
    }
}
