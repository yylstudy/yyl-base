package com.linkcircle.mq.config;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.Map;

/**
 * @author yang.yonglian
 * @version 1.0.0
 * @Description TODO
 * @createTime 2024/5/7 19:07
 */
public class MqApplicationContext implements ApplicationContextAware {
    private static ApplicationContext applicationContext;
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        MqApplicationContext.applicationContext = applicationContext;
    }
    public static <T> T getBean(Class<T> clazz){
        return applicationContext.getBean(clazz);
    }
    public static <T> Map<String, T> getBeansOfType(Class<T> clazz){
        return applicationContext.getBeansOfType(clazz);
    }

}
