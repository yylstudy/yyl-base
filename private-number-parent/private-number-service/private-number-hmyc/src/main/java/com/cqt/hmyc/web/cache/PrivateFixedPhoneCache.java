package com.cqt.hmyc.web.cache;

import com.google.common.collect.Lists;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author linshiqiang
 * date:  2023-03-08 13:53
 * 固话号码缓存
 */
public class PrivateFixedPhoneCache {

    public static final Map<String, List<String>> PHONE_CACHE = new ConcurrentHashMap<>();

    public static List<String> get(String key) {
        return PHONE_CACHE.getOrDefault(key, Lists.newArrayList());
    }

    public static void putAll(Map<String, List<String>> all) {
        PHONE_CACHE.putAll(all);
    }

    public static void clear() {
        PHONE_CACHE.clear();
    }
}
