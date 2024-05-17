package com.cqt.sdk.client.event.arrange;

import cn.hutool.core.util.StrUtil;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import io.netty.util.Timeout;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.Objects;

/**
 * @author linshiqiang
 * date:  2023-08-08 10:08
 * 事后处理任务缓存
 */
@Slf4j
public class ArrangeTaskCache {

    private static final Cache<String, Timeout> CACHE = CacheBuilder.newBuilder()
            .expireAfterWrite(Duration.ofHours(8))
            .maximumSize(10000)
            .build();

    public static long size() {
        return CACHE.size();
    }

    /**
     * 添加
     *
     * @param companyCode 企业id
     * @param agentId     坐席id
     * @param task        任务
     */
    public static void add(String companyCode, String agentId, Timeout task) {
        String key = getKey(companyCode, agentId);
        CACHE.put(key, task);
    }

    /**
     * 取消任务
     *
     * @param companyCode 企业id
     * @param agentId     坐席id
     * @return true/false
     */
    public static boolean cancel(String companyCode, String agentId) {
        String key = getKey(companyCode, agentId);
        Timeout task = CACHE.getIfPresent(key);
        if (Objects.nonNull(task)) {
            boolean cancel = task.cancel();
            log.info("[事后处理] 企业: {}, 坐席: {}, 取消cache任务: {}", companyCode, agentId, cancel);
            return cancel;
        }
        return false;
    }

    /**
     * 缓存键
     *
     * @param companyCode 企业id
     * @param agentId     坐席id
     * @return key
     */
    public static String getKey(String companyCode, String agentId) {
        return companyCode + StrUtil.AT + agentId;
    }
}
