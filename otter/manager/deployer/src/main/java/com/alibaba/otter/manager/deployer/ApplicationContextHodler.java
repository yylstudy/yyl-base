package com.alibaba.otter.manager.deployer;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * @author yang.yonglian
 * @version 1.0.0
 * @Description TODO
 * @createTime 2022/3/31 9:45
 */

public class ApplicationContextHodler implements ApplicationContextAware {
    private static ApplicationContext applicationContext;



    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        ApplicationContextHodler.applicationContext = applicationContext;
    }

    public static <T> T getBean(String beanName){
        return (T)applicationContext.getBean(beanName);
    }

    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }
}
