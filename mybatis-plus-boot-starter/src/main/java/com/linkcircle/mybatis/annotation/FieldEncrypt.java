package com.linkcircle.mybatis.annotation;

import com.linkcircle.mybatis.encrypt.DefaultEnDecryptor;
import com.linkcircle.mybatis.encrypt.EnDecryptor;

import java.lang.annotation.*;

/**
 * @author yang.yonglian
 * @version 1.0.0
 * @Description 字段加密注解
 * @createTime 2024/4/11 15:14
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface FieldEncrypt {
    /**
     * 加密器
     * @return
     */
    Class<? extends EnDecryptor> encryptor() default DefaultEnDecryptor.class;

}
