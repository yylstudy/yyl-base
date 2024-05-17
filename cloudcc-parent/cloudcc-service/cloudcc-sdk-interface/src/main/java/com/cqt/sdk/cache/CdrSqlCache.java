package com.cqt.sdk.cache;

import java.util.HashMap;
import java.util.Map;

/**
 * @author linshiqiang
 * date:  2023-09-11 9:29
 */
public class CdrSqlCache {

    private static final String KEY = "default";

    private static final Map<String, String> CACHE = new HashMap<>();

    public static void put(String sql) {
        CACHE.put(KEY, sql);
    }

    public static String get() {
        return CACHE.get(KEY);
    }
}
