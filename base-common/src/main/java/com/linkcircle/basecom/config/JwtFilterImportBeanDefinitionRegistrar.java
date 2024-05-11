package com.linkcircle.basecom.config;

import com.linkcircle.basecom.annotation.EnableJwtFilter;
import com.linkcircle.basecom.filter.JwtFilter;
import com.linkcircle.basecom.handler.TokenHandler;
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
public class JwtFilterImportBeanDefinitionRegistrar implements ImportBeanDefinitionRegistrar {
    /**
     * 手动注册@Import的类到spring容器中
     * @param importingClassMetadata 这个是@Import注解所在类的AnnotationMetadata
     * @param registry
     */
    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        try{
            Map<String,Object> attributeMap = importingClassMetadata
                    .getAnnotationAttributes(EnableJwtFilter.class.getName());
            RootBeanDefinition rootBeanDefinition = new RootBeanDefinition(JwtFilter.class);
            Class<? extends TokenHandler> tokenHandlerClazz = (Class)attributeMap.get("tokenHandler");
            RootBeanDefinition tokenHandlerRootBeanDefinition = new RootBeanDefinition(tokenHandlerClazz);
            registry.registerBeanDefinition(rootBeanDefinition.getBeanClassName()+"-tokenHandler",tokenHandlerRootBeanDefinition);
            rootBeanDefinition.setDependsOn("applicationContextHolder");
            registry.registerBeanDefinition(rootBeanDefinition.getBeanClassName(),rootBeanDefinition);
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }
}
