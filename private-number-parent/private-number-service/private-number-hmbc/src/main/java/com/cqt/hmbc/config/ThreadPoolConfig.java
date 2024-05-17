package com.cqt.hmbc.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * 线程池配置
 *
 * @author Xienx
 * @date 2023年02月08日 10:15
 */
@Slf4j
@EnableAsync
@Configuration(proxyBeanMethods = false)
public class ThreadPoolConfig {
    public final static String REDIS_EXECUTOR = "redisExecutor";
    public final static String COMMON_EXECUTOR = "commonExecutor";
    private final static Integer CPU_CORE = Runtime.getRuntime().availableProcessors();

    @Bean(COMMON_EXECUTOR)
    public ThreadPoolTaskExecutor commonExecutor() {
        log.info("初始化 commonExecutor 线程池, 当前核数: {}", CPU_CORE);
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(CPU_CORE * 2);
        executor.setMaxPoolSize(CPU_CORE * 2);
        executor.setQueueCapacity(1024);
        executor.setKeepAliveSeconds(60);
        executor.setThreadNamePrefix("common-exec-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        // 拒绝策略, 由主线程执行
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }


    @Bean(REDIS_EXECUTOR)
    public ThreadPoolTaskExecutor redisExecutor() {
        log.info("初始化 redisExecutor 线程池, 当前核数: {}", CPU_CORE);
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(CPU_CORE);
        executor.setMaxPoolSize(CPU_CORE);
        executor.setQueueCapacity(4096);
        executor.setKeepAliveSeconds(60);
        executor.setThreadNamePrefix("redis-exec-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        // 拒绝策略, 由主线程执行
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }
}
