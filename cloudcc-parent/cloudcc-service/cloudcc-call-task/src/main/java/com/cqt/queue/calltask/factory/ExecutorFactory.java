package com.cqt.queue.calltask.factory;

import cn.hutool.core.util.HashUtil;
import com.alibaba.ttl.threadpool.TtlExecutors;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author linshiqiang
 * date:  2023-10-26 9:51
 */
@Slf4j
public enum ExecutorFactory {

    INSTANCE;

    private static final Integer INITIAL_CAPACITY = 4;

    private static final Cache<Integer, Executor> EXECUTOR_CACHE = CacheBuilder.newBuilder()
            .maximumSize(20)
            .expireAfterAccess(1, TimeUnit.HOURS)
            .removalListener(new MyRemovalListener())
            .build();

    /**
     * 创建线程池
     */
    public synchronized Executor createExecutor(Integer poolSize, Integer queueCapacity, String poolName) {

        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(poolSize);
        executor.setMaxPoolSize(poolSize * 2);
        executor.setQueueCapacity(queueCapacity);
        executor.setKeepAliveSeconds(60);
        executor.setThreadNamePrefix(poolName);
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.DiscardPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.initialize();
        return TtlExecutors.getTtlExecutor(executor);
    }

    public synchronized Executor getExecutor(@Nonnull String poolName,
                                             @Nonnull Integer poolSize,
                                             @Nonnull Integer queueCapacity) {
        int hash = HashUtil.fnvHash(poolName);
        int index = hash % INITIAL_CAPACITY;
        Executor executor = EXECUTOR_CACHE.getIfPresent(index);
        if (Objects.nonNull(executor)) {
            return executor;
        }
        Executor newExecutor = createExecutor(poolSize, queueCapacity, poolName);
        EXECUTOR_CACHE.put(index, newExecutor);
        return newExecutor;
    }

    public static class MyRemovalListener implements RemovalListener<Integer, Executor> {

        @Override
        public void onRemoval(@Nonnull RemovalNotification<Integer, Executor> notification) {
            try {
                ThreadPoolTaskExecutor executor = (ThreadPoolTaskExecutor) notification.getValue();
                assert executor != null;
                executor.shutdown();
            } catch (Exception e) {
                log.error("[RemovalListener] error: ", e);
            }
        }
    }
}
