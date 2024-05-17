package com.cqt.unicom.config.cache;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author zhengsuhao
 * @date 2023/1/17
 * 本地缓存
 */
public class UnicomLocalCacheService {


    /**
     * 放音编码与文件名对应关系
     */
    public final static Map<String, String> AUDIO_CODE_CACHE = new ConcurrentHashMap<>(16);


    public static String getIvrCode(String audioName) {
        return AUDIO_CODE_CACHE.getOrDefault(audioName, "");
    }

}
