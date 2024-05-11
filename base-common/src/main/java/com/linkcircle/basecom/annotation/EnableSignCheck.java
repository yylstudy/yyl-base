package com.linkcircle.basecom.annotation;

import com.linkcircle.basecom.config.SignCheckImportBeanDefinitionRegistrar;
import com.linkcircle.basecom.handler.SignHandler;
import com.linkcircle.basecom.handler.defaultHandler.DefaultSignHandler;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * @author yang.yonglian
 * @version 1.0.0
 * @Description 开启签名校验，可重写DefaultAppKeyHandler实现appKey的合法验证
 * @createTime 2024/3/26 13:37
 */
@Import({SignCheckImportBeanDefinitionRegistrar.class})
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface EnableSignCheck {
    /**
     * 签名处理器
     * @return
     */
    Class<? extends SignHandler> signHandler() default DefaultSignHandler.class;
}
