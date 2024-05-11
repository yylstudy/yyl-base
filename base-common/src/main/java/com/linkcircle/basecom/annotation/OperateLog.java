package com.linkcircle.basecom.annotation;


import java.lang.annotation.*;

/**
 * @author yang.yonglian
 * @version 1.0.0
 * @Description 操作日志注解
 * @createTime 2024/1/10 16:41
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OperateLog {
    /**
     * 日志内容，支持spel表达式
     * @return
     */
    String content() default "";
}
