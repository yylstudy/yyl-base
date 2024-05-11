package com.linkcircle.basecom.annotation;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.linkcircle.basecom.config.DefaultAutoFillMetaObjectHandler;
import com.linkcircle.basecom.config.AutoFillMetaObjectHandlerImportBeanDefinitionRegistrar;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * @author yang.yonglian
 * @version 1.0.0
 * @Description 开启自动填充功能，默认自动填充BaseEntity四个属性值
 * @createTime 2024/3/26 13:37
 */
@Import({AutoFillMetaObjectHandlerImportBeanDefinitionRegistrar.class})
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface EnableAutoFillBaseEntity {
    Class<? extends MetaObjectHandler> autoFillMetaObjectHandler() default DefaultAutoFillMetaObjectHandler.class;
}
