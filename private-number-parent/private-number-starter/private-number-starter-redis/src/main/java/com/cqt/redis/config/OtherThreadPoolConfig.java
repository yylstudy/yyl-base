package com.cqt.redis.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author linshiqiang
 * @since 2021/9/10 11:39
 * 线程池配置
 */
@Configuration
public class OtherThreadPoolConfig {

    public static final int CPU_NUM = Runtime.getRuntime().availableProcessors();

    public static final int QUEUE_CAPACITY = 20000;

    /**
     * 异地机房redis操作
     */
    @Bean("otherExecutor")
    public ThreadPoolTaskExecutor otherExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(CPU_NUM * 2);
        executor.setMaxPoolSize(CPU_NUM * 4);
        executor.setQueueCapacity(QUEUE_CAPACITY);
        executor.setKeepAliveSeconds(60);
        executor.setThreadNamePrefix("otherExecutor-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        // 拒绝策略 主线程执行
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }

}
