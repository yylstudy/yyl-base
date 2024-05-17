package com.cqt.sms.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * @Description: TODO
 * @author: scott
 * @date: 2022年03月25日 14:56
 */
@Slf4j
@EnableAsync
@Configuration
public class ThreadPoolConfig {
    /**
     *   每秒需要多少个线程处理?
     *   核心线程会一直存活，即使没有任务执行也存活
     *   当线程数少于核心线程数时，即使有空闲的线程，也会优先创建新的线程去处理。
     *   设置参数allowCoreThreadTimeout=true(默认false)，核心线程会超时关闭。
     *   tasks/(1/taskcost)
     */
    private int corePoolSize = 32 ;

    /**
     * 线程池维护线程的最大数量
     * 当线程数大于corePoolSize时，且任务队列也已满，线程池会创建新的线程处理任务。
     * 当线程数=maxPoolSize，且任务队列已满，线程池会默认抛出异常拒绝执行任务。
     * (max(tasks)- queueCapacity)/(1/taskcost)
     */
    private int maxPoolSize = 64;

    /**
     * 缓存队列
     * 当核心线程数达到最大时，新任务会放在队列中排队等待执行。
     * (coreSizePool/taskcost)*responsetime
     */
    private int queueCapacity = 256;

    /**
     * 允许的空闲时间
     * 当线程空闲时间达到这个设置时间时，线程会自动退出，直到线程数量=corePoolSize
     * 如果allowCoreThreadTimeout=true，则会直到线程数量=0
     * 默认为60
     */
    private int keepAlive = 60;

    @Bean
    public TaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        try {
            int numThreads = Runtime.getRuntime().availableProcessors() * 2;
            int maxThreads = numThreads * 10;
            // 设置核心线程数
            if (numThreads < corePoolSize) {
                numThreads = corePoolSize;
            }
            if (maxThreads < maxPoolSize) {
                maxThreads = maxPoolSize;
            }
            executor.setCorePoolSize(numThreads);
            // 设置最大线程数
            executor.setMaxPoolSize(maxThreads);
        } catch (Exception e) {
            executor.setCorePoolSize(corePoolSize);
            executor.setMaxPoolSize(maxPoolSize);
            log.error(e.getMessage(), e);
        }
        log.info("自定义线程池线程数：coreThreads:[{}], maxThreads:[{}]", executor.getCorePoolSize(), executor.getMaxPoolSize());
        executor.setQueueCapacity(queueCapacity);
        // 设置允许的空闲时间（秒）
        executor.setKeepAliveSeconds(keepAlive);
        executor.setAwaitTerminationSeconds(600);
        // 设置默认线程名称
        executor.setThreadNamePrefix("thread-task-");
        // 设置线程优先级
        executor.setThreadPriority(1);
        // 设置拒绝策略rejection-policy：当pool已经达到max size的时候，如何处理新任务
        // CALLER_RUNS：不在新线程中执行任务，而是由调用者所在的线程来执行
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();

        return executor;
    }
}
