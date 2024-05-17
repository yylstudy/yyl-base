package com.cqt.hmyc.config;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author linshiqiang
 * date:  2023-01-28 15:40
 * 放音编码缓存
 */
public class AudioCodeCache {

    /**
     * 放音编码与文件名对应关系
     */
    public static final Map<String, String> AUDIO_CODE_CACHE = new ConcurrentHashMap<>(16);

    public static String get(String key) {
        return AUDIO_CODE_CACHE.get(key);
    }
}
