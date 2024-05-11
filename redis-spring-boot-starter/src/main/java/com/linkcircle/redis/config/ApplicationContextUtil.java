package com.linkcircle.redis.config;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * @author yang.yonglian
 * @version 1.0.0
 * @Description TODO
 * @createTime 2024/3/25 13:46
 */
@Component
public class ApplicationContextUtil  implements ApplicationContextAware, EnvironmentAware {
    private static ApplicationContext applicationContext;
    private static Environment environment;
    public static Environment getEnvironment(){
        return environment;
    }
    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }
    public static <T> T getBean(Class<T> clazz){
        return applicationContext.getBean(clazz);
    }
    public static <T> T getBean(String beanName){
        return (T)applicationContext.getBean(beanName);
    }

    @Override
    public void setEnvironment(Environment environment) {
        ApplicationContextUtil.environment = environment;
    }
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        ApplicationContextUtil.applicationContext = applicationContext;
    }
}
