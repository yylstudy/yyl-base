package com.cqt.cloudcc.manager.cache;

import cn.hutool.core.util.StrUtil;
import com.cqt.model.queue.dto.AgentCheckinCacheDTO;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import java.time.Duration;

/**
 * @author linshiqiang
 * date:  2023-12-01 13:58
 * 签入 存入缓存
 */
public class AgentCheckinCache {

    private static final Cache<String, AgentCheckinCacheDTO> CACHE = CacheBuilder.newBuilder()
            .expireAfterAccess(Duration.ofHours(5))
            .maximumSize(1000000)
            .build();

    public static void put(String companyCode, String agentId, AgentCheckinCacheDTO agentCheckinCacheDTO) {
        CACHE.put(getKey(companyCode, agentId), agentCheckinCacheDTO);
    }

    public static AgentCheckinCacheDTO get(String companyCode, String agentId) {
        return CACHE.getIfPresent(getKey(companyCode, agentId));
    }

    public static void remove(String companyCode, String agentId) {
        CACHE.invalidate(getKey(companyCode, agentId));
    }

    public static Cache<String, AgentCheckinCacheDTO> all() {
        return CACHE;
    }

    private static String getKey(String companyCode, String agentId) {
        return companyCode + StrUtil.COLON + agentId;
    }
}
