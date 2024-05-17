package com.cqt.sms.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author Xienx
 * @date 2023-05-06 09:17:9:17
 */
@EnableAsync(proxyTargetClass = true)
@Configuration(proxyBeanMethods = false)
public class ThreadPoolConfig {
    private final static Integer CPU_CORE = Runtime.getRuntime().availableProcessors();

    @Bean("executor")
    public ThreadPoolTaskExecutor executor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(CPU_CORE * 2);
        executor.setMaxPoolSize(CPU_CORE * 4);
        executor.setQueueCapacity(1024);
        executor.setKeepAliveSeconds(60);
        executor.setThreadNamePrefix("thread-pool-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        // 拒绝策略, 由主线程执行
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }
}
