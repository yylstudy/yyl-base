package com.linkcircle.mybatis.annotation;

import com.linkcircle.mybatis.interceptor.EnDecryptInterceptor;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * @author yang.yonglian
 * @version 1.0.0
 * @Description 字段加密注解
 * @createTime 2024/4/11 15:14
 */
@Target({ElementType.TYPE})
@Import(EnDecryptInterceptor.class)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface EnableFieldEncrypt {


}
