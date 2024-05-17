package com.cqt.monitor.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author hlx
 * @date 2021-07-16
 */
@Configuration
public class ThreadConfig {

    private final static Integer CPU_CORE = Runtime.getRuntime().availableProcessors();


    /**
     * spring线程池实例
     *
     * @return 线程池bean
     */
    @Bean("threadPool")
    public ThreadPoolTaskExecutor threadPoolTaskExecutor() {
        ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
        //核心池大小
        threadPoolTaskExecutor.setCorePoolSize(CPU_CORE);
        //最大线程数
        threadPoolTaskExecutor.setMaxPoolSize(CPU_CORE * 3);
        //队列程度
        threadPoolTaskExecutor.setQueueCapacity(1000);
        //线程空闲时间
        threadPoolTaskExecutor.setKeepAliveSeconds(100);
        //线程前缀名称
        threadPoolTaskExecutor.setThreadNamePrefix("task-async");
        //拒绝策略
        threadPoolTaskExecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        return threadPoolTaskExecutor;
    }


    /**
     * 异地机房redis操作
     */
    @Bean("otherExecutor")
    public ThreadPoolTaskExecutor otherExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(CPU_CORE);
        executor.setMaxPoolSize(CPU_CORE * 3);
        executor.setQueueCapacity(5000);
        executor.setKeepAliveSeconds(60);
        executor.setThreadNamePrefix("otherExecutor-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        // 拒绝策略, 由主线程执行
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }

    @Bean("disMonitorExecutor")
    public ThreadPoolTaskExecutor disMonitorExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(CPU_CORE);
        executor.setMaxPoolSize(CPU_CORE * 2);
        executor.setQueueCapacity(200);
        executor.setKeepAliveSeconds(60);
        executor.setThreadNamePrefix("disMonitorExecutor-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        // 拒绝策略, 由主线程执行
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }
}
