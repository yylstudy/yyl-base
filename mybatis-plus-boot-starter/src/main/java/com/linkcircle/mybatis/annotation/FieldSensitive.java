package com.linkcircle.mybatis.annotation;

import com.linkcircle.mybatis.sensitive.CurrencySensitiveStrategy;
import com.linkcircle.mybatis.sensitive.SensitiveStrategy;

import java.lang.annotation.*;

/**
 * @author yang.yonglian
 * @version 1.0.0
 * @Description 字段脱敏
 * @createTime 2024/4/11 15:14
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface FieldSensitive {
    /**
     * 脱敏策略，常用的
     * @see CurrencySensitiveStrategy
     * @return
     */
    Class<? extends SensitiveStrategy> strategy();

}
