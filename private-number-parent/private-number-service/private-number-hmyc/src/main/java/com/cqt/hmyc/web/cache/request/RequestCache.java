package com.cqt.hmyc.web.cache.request;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.DelayQueue;

/**
 * @author linshiqiang
 * @date 2021/11/16 9:46
 * 重复requestId 结果缓存
 */
public class RequestCache {

    /**
     * requestId 对应的绑定关系结果
     */
    public final static Map<String, CompletableFuture<String>> REQUEST_ID_INFO_CACHE = new ConcurrentHashMap<>(64);

    /**
     * 重复requestId 请求延时队列
     */
    public final static DelayQueue<RequestDelayed> REQUEST_DELAYED_QUEUE = new DelayQueue<>();
}
