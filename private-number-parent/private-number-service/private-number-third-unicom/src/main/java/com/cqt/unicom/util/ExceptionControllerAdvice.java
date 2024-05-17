package com.cqt.unicom.util;

import com.cqt.model.unicom.vo.GeneralMessageVO;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.ArrayList;
import java.util.List;

/**
 * @author zhengsuhao
 * @date 2022/12/14
 */
@Api(tags = "请求错误拦截返回")
@Slf4j
@RestControllerAdvice(basePackages = "com.cqt.unicom.controller")
public class ExceptionControllerAdvice {

    /**
     * @param e
     * @return GeneralMessageVO
     */
    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public GeneralMessageVO handlerVaildException(MethodArgumentNotValidException e) {
        log.error("数据校验出现问题:{}，异常类型:{}", e.getMessage(), e.getClass());
        BindingResult bindingResult = e.getBindingResult();
        List<Object> errorList = new ArrayList<>();
        bindingResult.getFieldErrors().forEach(fieldError -> {
            errorList.add(fieldError);
        });
        String defaultMessage = bindingResult.getFieldError().getDefaultMessage();
        return GeneralMessageVO.fail("400",defaultMessage);
    }
}
