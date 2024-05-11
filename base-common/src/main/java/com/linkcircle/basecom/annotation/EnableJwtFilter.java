package com.linkcircle.basecom.annotation;

import com.linkcircle.basecom.handler.defaultHandler.DefaultTokenHandler;
import com.linkcircle.basecom.config.JwtFilterImportBeanDefinitionRegistrar;
import com.linkcircle.basecom.handler.TokenHandler;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * @author yang.yonglian
 * @version 1.0.0
 * @Description 开启jwt过滤器，可重写DefaultTokenHandler实现自定义token校验和解析
 * @createTime 2024/3/26 13:37
 */
@Import({JwtFilterImportBeanDefinitionRegistrar.class})
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface EnableJwtFilter {
    /**
     * token处理器
     * @return
     */
    Class<? extends TokenHandler> tokenHandler() default DefaultTokenHandler.class;
}
