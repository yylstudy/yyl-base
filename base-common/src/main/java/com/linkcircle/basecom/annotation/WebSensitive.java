package com.linkcircle.basecom.annotation;

import com.fasterxml.jackson.annotation.JacksonAnnotationsInside;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.linkcircle.basecom.desensitization.CurrencySensitiveStrategy;
import com.linkcircle.basecom.desensitization.SensitiveSerializer;
import com.linkcircle.basecom.desensitization.SensitiveStrategy;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author yang.yonglian
 * @version 1.0.0
 * @Description web脱敏注解
 * @createTime 2024/4/12 17:12
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@JacksonAnnotationsInside
@JsonSerialize(using = SensitiveSerializer.class)
public @interface WebSensitive {
    /**
     * 脱敏策略，常用的
     * @see CurrencySensitiveStrategy
     * @return
     */
    Class<? extends SensitiveStrategy> strategy();
}
