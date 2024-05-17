package com.cqt.broadnet.config;

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

    public static final int CPU_NUM = Runtime.getRuntime().availableProcessors();

    /**
     * 接口异步
     */
    @Bean("saveExecutor")
    public ThreadPoolTaskExecutor saveExecutor() {
        log.info("初始化saveExecutor线程池, 当前核数: {}", CPU_NUM);
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(CPU_NUM * 2);
        executor.setMaxPoolSize(CPU_NUM * 3);
        executor.setQueueCapacity(1000);
        executor.setKeepAliveSeconds(60);
        executor.setThreadNamePrefix("saveExecutor-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        // 拒绝策略, 由主线程执行
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }

}
