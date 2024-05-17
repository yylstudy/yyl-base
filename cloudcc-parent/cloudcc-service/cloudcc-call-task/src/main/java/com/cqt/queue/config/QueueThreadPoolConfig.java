package com.cqt.queue.config;

import cn.hutool.core.thread.NamedThreadFactory;
import com.alibaba.ttl.threadpool.TtlExecutors;
import com.cqt.base.decorator.MdcTaskDecorator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author linshiqiang
 * date:  2023-07-20 10:25
 */
@Slf4j
@Configuration
public class QueueThreadPoolConfig {

    public static final String POLLING_AGENT_POOL_NAME = "polling-agent-pool";

    public static final String SCHEDULE_POLLING_POOL_NAME = "schedule-polling-pool";

    private static final Integer CPU_CORE = Runtime.getRuntime().availableProcessors();

    @Bean(POLLING_AGENT_POOL_NAME)
    public Executor pollingAgentExecutor() {
        log.info("初始化 {} 线程池, 当前核数: {}", POLLING_AGENT_POOL_NAME, CPU_CORE);
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(CPU_CORE);
        executor.setMaxPoolSize(CPU_CORE * 3);
        executor.setQueueCapacity(200);
        executor.setKeepAliveSeconds(60);
        executor.setThreadNamePrefix(POLLING_AGENT_POOL_NAME);
        executor.setTaskDecorator(new MdcTaskDecorator());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setPrestartAllCoreThreads(true);
        // 拒绝策略, 由主线程执行
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return TtlExecutors.getTtlExecutor(executor);
    }

    @Bean(SCHEDULE_POLLING_POOL_NAME)
    public Executor schedulePollingExecutor() {
        ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(3,
                new NamedThreadFactory("schedule-polling-agent", false));
        executor.setCorePoolSize(CPU_CORE);
        // 拒绝策略, 由主线程执行
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        return TtlExecutors.getTtlExecutor(executor);
    }
}
