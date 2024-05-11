package com.linkcircle.redis.annotation;

import com.linkcircle.redis.config.DefaultUserIdHandler;
import com.linkcircle.redis.config.RepeatSubmitImportBeanDefinitionRegistrar;
import com.linkcircle.redis.config.UserIdHandler;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * @author yang.yonglian
 * @version 1.0.0
 * @Description 开启重复提交校验
 * @createTime 2024/3/26 13:37
 */
@Import({RepeatSubmitImportBeanDefinitionRegistrar.class})
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface EnableRepeatSubmit {
    /**
     * 用户ID处理器，主要用于自定义RepeatSubmit中的keyAppendUserId中的userId的值
     * @return
     */
    Class<? extends UserIdHandler> userIdHandler() default DefaultUserIdHandler.class;
}
