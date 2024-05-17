package com.cqt.hmyc.config.pool;

import cn.hutool.core.convert.Convert;
import com.cqt.model.common.properties.HideProperties;
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

    public static final int QUEUE_CAPACITY = 20000;

    private final HideProperties hideProperties;

    public ThreadPoolConfig(HideProperties hideProperties) {
        this.hideProperties = hideProperties;
    }

    /**
     * mq发送 保存数据库
     */
    @Bean("saveExecutor")
    public ThreadPoolTaskExecutor saveExecutor() {
        log.info("初始化saveExecutor线程池, 当前核数: {}", CPU_NUM);
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(CPU_NUM);
        executor.setMaxPoolSize(CPU_NUM * 3);
        executor.setQueueCapacity(Convert.toInt(hideProperties.getMaxQueueSize(), QUEUE_CAPACITY));
        executor.setKeepAliveSeconds(60);
        executor.setThreadNamePrefix("saveExecutor-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        // 拒绝策略, 由主线程执行
        // 发送mq失败直接丢弃
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.DiscardPolicy());
        executor.initialize();
        return executor;
    }

    @Bean("bindExecutor")
    public ThreadPoolTaskExecutor bindExecutor() {
        log.info("初始化bindExecutor线程池, 当前核数: {}", CPU_NUM);
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(CPU_NUM * 2);
        executor.setMaxPoolSize(CPU_NUM * 4);
        executor.setQueueCapacity(Convert.toInt(hideProperties.getMaxQueueSize(), QUEUE_CAPACITY));
        executor.setKeepAliveSeconds(60);
        executor.setThreadNamePrefix("bindExecutor-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        // 拒绝策略, 由主线程执行
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.DiscardPolicy());
        executor.initialize();
        return executor;
    }

    /**
     * 异地机房redis操作
     */
    @Bean("otherExecutor")
    public ThreadPoolTaskExecutor otherExecutor() {
        log.info("初始化 otherExecutor 线程池, 当前核数: {}", CPU_NUM);
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(CPU_NUM * 2);
        executor.setMaxPoolSize(CPU_NUM * 4);
        executor.setQueueCapacity(Convert.toInt(hideProperties.getMaxQueueSize(), QUEUE_CAPACITY));
        executor.setKeepAliveSeconds(60);
        executor.setThreadNamePrefix("otherExecutor-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        // 拒绝策略 主线程执行
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }

}
