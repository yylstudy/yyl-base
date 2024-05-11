package com.linkcircle.redis.config;


import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * @author yang.yonglian
 * @version 1.0.0
 * @Description TODO
 * @createTime 2024/3/25 13:46
 */
@Slf4j
public class Redis2Executor {

    private volatile static Boolean cluster2Active;
    private volatile static RedisTemplate<String, Object> redisTemplate2;

    private Redis2Executor() {

    }

    private static int threadNum = ApplicationContextUtil.getEnvironment().getProperty("thread.num", Integer.class, 4);
    private static ThreadPoolExecutor redisTriggerPool = new ThreadPoolExecutor(
            threadNum,
            threadNum,
            60L,
            TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(20000),
            r -> new Thread(r, "redis-sync-task-thread" + r.hashCode()));

    public static ThreadPoolExecutor getRedisTriggerPool() {
        return redisTriggerPool;
    }

    /**
     * 异地机房执行redisTemplate2
     * @param consumer
     */
    public static void execute(Consumer<RedisTemplate<String, Object>> consumer) {
        try {
            if (cluster2Active == null) {
                synchronized (Redis2Executor.class) {
                    if (cluster2Active == null) {
                        MultiRedisProperties multiRedisProperties = ApplicationContextUtil.getBean(MultiRedisProperties.class);
                        cluster2Active = multiRedisProperties.getCluster2().isActive();
                        if (cluster2Active) {
                            redisTemplate2 = ApplicationContextUtil.getBean("redisTemplate2");
                        }
                    }
                }
            }
            if (cluster2Active) {
                redisTriggerPool.execute(() -> {
                    try {
                        consumer.accept(redisTemplate2);
                    } catch (Exception e) {
                        log.error("execute redis error", e);
                    }
                });
            }
        } catch (Exception e) {
            log.error("submit task error", e);
        }
    }

    /**
     * 异地机房执行任务
     * @param runnable
     */
    public static void execute(Runnable runnable){
        redisTriggerPool.execute(runnable);
    }

}
