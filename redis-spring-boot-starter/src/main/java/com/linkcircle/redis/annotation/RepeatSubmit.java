package com.linkcircle.redis.annotation;

import java.lang.annotation.*;

/**
 * @author yang.yonglian
 * @version 1.0.0
 * @Description 防止重复提交注解
 * @createTime 2024/1/30 16:36
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
@Documented
public @interface RepeatSubmit {
    /**
     * 防止重复提交时间，单位秒
     * @return
     */
    int lockTime() default 5;

    /**
     * redis锁key，支持spel表达式
     */
    String lockKey();

    /**
     * 请求完成后，是否依然等待至maxLockTime时间，默认不需要，也就是马上可以下次请求
     * @return
     */
    boolean needWait() default false;
    /**
     * 锁的key末尾是否追加用户ID
     */
    boolean keyAppendUserId() default false;
}
