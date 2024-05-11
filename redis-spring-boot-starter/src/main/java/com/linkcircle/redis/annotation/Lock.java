package com.linkcircle.redis.annotation;

import java.lang.annotation.*;

/**
 * @author yang.yonglian
 * @version 1.0.0
 * @Description 分布式锁注解
 * @createTime 2024/3/27 18:03
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface Lock {
    /**
     * key 支持spel表达式
     * @return
     */
    String lockKey();

    /**
     * 锁超时时间,默认30000毫秒
     * @return
     */
    long expireSeconds() default 30000L;

    /**
     * 等待加锁超时时间,默认10000毫秒 -1 则表示一直等待
     * @return
     */
    long waitTime() default 10000L;
}
