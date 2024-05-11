package com.linkcircle.redis.config;

import com.linkcircle.redis.annotation.EnableRepeatSubmit;
import com.linkcircle.redis.aspect.RepeatSubmitAspect;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;

import java.util.Map;

/**
 * @author yang.yonglian
 * @version 1.0.0
 * @Description TODO
 * @createTime 2024/3/26 14:08
 */
@Slf4j
public class RepeatSubmitImportBeanDefinitionRegistrar implements ImportBeanDefinitionRegistrar {
    /**
     * 手动注册@Import的类到spring容器中
     * @param importingClassMetadata 这个是@Import注解所在类的AnnotationMetadata
     * @param registry
     */
    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        try{
            Map<String,Object> attributeMap = importingClassMetadata
                    .getAnnotationAttributes(EnableRepeatSubmit.class.getName());
            RootBeanDefinition rootBeanDefinition = new RootBeanDefinition(RepeatSubmitAspect.class);
            Class<? extends UserIdHandler> userIdHandlerClazz = (Class)attributeMap.get("userIdHandler");
            RootBeanDefinition userIdHandlerRootBeanDefinition = new RootBeanDefinition(userIdHandlerClazz);
            registry.registerBeanDefinition(rootBeanDefinition.getBeanClassName()+"-userIdHandler",userIdHandlerRootBeanDefinition);
            rootBeanDefinition.setDependsOn("applicationContextHolder");
            registry.registerBeanDefinition(rootBeanDefinition.getBeanClassName(),rootBeanDefinition);
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }
}
