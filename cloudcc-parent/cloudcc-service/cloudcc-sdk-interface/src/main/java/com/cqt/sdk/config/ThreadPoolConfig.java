package com.cqt.sdk.config;

import com.alibaba.ttl.threadpool.TtlExecutors;
import com.cqt.base.decorator.MdcTaskDecorator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author linshiqiang
 * @since 2021/9/10 11:39
 * 线程池配置
 */
@Slf4j
@Configuration
public class ThreadPoolConfig {

    public static final String SDK_POOL_NAME = "sdk-pool-";

    private static final Integer CPU_CORE = Runtime.getRuntime().availableProcessors();

    @Bean(SDK_POOL_NAME)
    public Executor sdkExecutor() {
        log.info("初始化 {} 线程池, 当前核数: {}", SDK_POOL_NAME, CPU_CORE);
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(CPU_CORE);
        executor.setMaxPoolSize(CPU_CORE * 3);
        executor.setQueueCapacity(5000);
        executor.setKeepAliveSeconds(60);
        executor.setThreadNamePrefix(SDK_POOL_NAME);
        executor.setTaskDecorator(new MdcTaskDecorator());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        // 拒绝策略, 由主线程执行
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return TtlExecutors.getTtlExecutor(executor);
    }

}
