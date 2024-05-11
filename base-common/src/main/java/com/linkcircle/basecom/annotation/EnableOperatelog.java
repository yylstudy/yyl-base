package com.linkcircle.basecom.annotation;

import com.linkcircle.basecom.config.OperateLogImportBeanDefinitionRegistrar;
import com.linkcircle.basecom.handler.OperateLogHandler;
import com.linkcircle.basecom.handler.defaultHandler.DefaultOperateLogHandler;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * @author yang.yonglian
 * @version 1.0.0
 * @Description 开启操作日志，可重写DefaultOperateLogService实现日志保存，对应的
 * @createTime 2024/3/26 13:37
 */
@Import({OperateLogImportBeanDefinitionRegistrar.class})
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface EnableOperatelog {
    /**
     * 操作日志保存处理器
     * @return
     */
    Class<? extends OperateLogHandler> operateLogService() default DefaultOperateLogHandler.class;
}
