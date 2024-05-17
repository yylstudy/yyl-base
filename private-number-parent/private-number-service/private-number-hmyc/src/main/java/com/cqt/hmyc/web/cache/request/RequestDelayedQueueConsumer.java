package com.cqt.hmyc.web.cache.request;

import com.cqt.common.constants.SystemConstant;
import com.cqt.model.common.properties.HideProperties;
import com.cqt.redis.util.RedissonUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.TimeUnit;

/**
 * @author linshiqiang
 * @date 2021/11/16 12:28
 * 重复requestId 延时队列 消费者
 */
@Component
@Slf4j
public class RequestDelayedQueueConsumer {

    @Resource
    private RedissonUtil redissonUtil;

    @Resource(name = "bindExecutor")
    private ThreadPoolTaskExecutor bindExecutor;

    @Resource
    private HideProperties hideProperties;

    @PostConstruct
    public void run() {
        bindExecutor.execute(this::deal);
    }

    public void deal() {
        DelayQueue<RequestDelayed> queue = RequestCache.REQUEST_DELAYED_QUEUE;
        while (true) {
            try {
                if (queue.isEmpty()) {
                    // 队列为空时, 睡眠20毫秒
                    TimeUnit.MILLISECONDS.sleep(20);
                }
                RequestDelayed delayed = queue.take();
                String key = delayed.key;
                String bindInfoStr = redissonUtil.getString(key);
                String cacheKey = key + delayed.threadName;
                if (SystemConstant.EMPTY_OBJECT.equals(bindInfoStr)) {
                    log.info("requestId: {}, 未执行完, ", cacheKey);
                    RequestCache.REQUEST_DELAYED_QUEUE.put(new RequestDelayed(hideProperties.getRequestTimeout(), TimeUnit.MILLISECONDS, key, delayed.threadName));
                } else {
                    CompletableFuture<String> completableFuture = RequestCache.REQUEST_ID_INFO_CACHE.remove(cacheKey);
                    completableFuture.complete(bindInfoStr);
                    log.info("requestId: {}, 执行完成, ", cacheKey);
                }
            } catch (InterruptedException e) {
                log.error("take delayed queue error: ", e);
            }

        }
    }
}
