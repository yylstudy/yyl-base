package com.linkcircle.basecom.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.linkcircle.basecom.annotation.EnableAutoFillBaseEntity;
import com.linkcircle.basecom.annotation.EnableOperatelog;
import com.linkcircle.basecom.aspect.OperateLogAspect;
import com.linkcircle.basecom.handler.OperateLogHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;

import java.util.Map;

/**
 * @author yang.yonglian
 * @version 1.0.0
 * @Description 注册AutoFillMetaObjectHandler
 * @createTime 2024/3/26 14:08
 */
@Slf4j
public class AutoFillMetaObjectHandlerImportBeanDefinitionRegistrar implements ImportBeanDefinitionRegistrar {
    /**
     * 手动注册@Import的类到spring容器中
     * @param importingClassMetadata 这个是@Import注解所在类的AnnotationMetadata
     * @param registry
     */
    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        try{
            Map<String,Object> attributeMap = importingClassMetadata
                    .getAnnotationAttributes(EnableAutoFillBaseEntity.class.getName());
            Class<? extends MetaObjectHandler> metaObjectHandlerClazz = (Class)attributeMap.get("autoFillMetaObjectHandler");
            RootBeanDefinition metaObjectHandlerRootBeanDefinition = new RootBeanDefinition(metaObjectHandlerClazz);
            registry.registerBeanDefinition(metaObjectHandlerRootBeanDefinition.getBeanClassName()+"-metaObjectHandler",metaObjectHandlerRootBeanDefinition);
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }
}
