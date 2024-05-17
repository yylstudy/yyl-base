package com.cqt.wechat.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * @author hlx
 * @date 2021-07-16
 */
@Configuration
public class ThreadConfig {

    /**
     * spring线程池实例
     */
    @Bean("threadPoolTaskExecutor")
    public ThreadPoolTaskExecutor threadPoolTaskExecutor() {
        ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
        //核心池大小
        threadPoolTaskExecutor.setCorePoolSize(10);
        //最大线程数
        threadPoolTaskExecutor.setMaxPoolSize(100);
        //队列程度
        threadPoolTaskExecutor.setQueueCapacity(400);
        //线程空闲时间
        threadPoolTaskExecutor.setKeepAliveSeconds(100);
        //线程前缀名称
        threadPoolTaskExecutor.setThreadNamePrefix("task-asyn");
        return threadPoolTaskExecutor;
    }
}
