package com.cqt.recycle.config.pool;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author linshiqiang
 * @date 2021/9/10 11:39
 * 线程池配置
 */
@Slf4j
@Configuration
public class ThreadPoolConfig {

    private static final Integer CPU_CORE = Runtime.getRuntime().availableProcessors();

    @Bean("recycleExecutor")
    public ThreadPoolTaskExecutor saveExecutor() {
        log.info("初始化 recycleExecutor 线程池, 当前核数: {}", CPU_CORE);
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(CPU_CORE);
        executor.setMaxPoolSize(CPU_CORE * 3);
        executor.setQueueCapacity(5000);
        executor.setKeepAliveSeconds(60);
        executor.setThreadNamePrefix("recycleExecutor-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        // 拒绝策略, 由主线程执行
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }

}
