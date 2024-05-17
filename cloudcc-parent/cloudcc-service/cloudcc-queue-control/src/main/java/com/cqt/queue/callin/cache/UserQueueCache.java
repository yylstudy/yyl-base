package com.cqt.queue.callin.cache;

import cn.hutool.core.collection.CollUtil;
import com.cqt.model.queue.dto.UserQueueUpDTO;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author linshiqiang
 * date:  2023-12-04 15:33
 */
public class UserQueueCache {

    /**
     * key company_code
     */
    private static final Cache<String, List<UserQueueUpDTO>> CACHE = CacheBuilder.newBuilder()
            .expireAfterAccess(Duration.ofMinutes(5))
            .maximumSize(10000)
            .build();

    public static synchronized void put(String companyCode, UserQueueUpDTO userQueueUpDTO) {
        List<UserQueueUpDTO> list = CACHE.getIfPresent(companyCode);
        if (CollUtil.isEmpty(list)) {
            List<UserQueueUpDTO> queue = new ArrayList<>(128);
            queue.add(userQueueUpDTO);
            CACHE.put(companyCode, queue);
            return;
        }
        list.add(userQueueUpDTO);
    }

    public static List<UserQueueUpDTO> get(String companyCode) {
        return CACHE.getIfPresent(companyCode);
    }

    public static synchronized void remove(String companyCode, String uuid) {
        List<UserQueueUpDTO> list = get(companyCode);
        if (CollUtil.isNotEmpty(list)) {
            Iterator<UserQueueUpDTO> iterator = list.iterator();
            while (iterator.hasNext()) {
                UserQueueUpDTO userQueueUpDTO = iterator.next();
                if (uuid.equals(userQueueUpDTO.getUuid())) {
                    iterator.remove();
                    return;
                }
            }
        }
    }

    public static Cache<String, List<UserQueueUpDTO>> all() {
        return CACHE;
    }
}
