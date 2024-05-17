package com.cqt.hmyc.web.cache;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author linshiqiang
 * @date 2021/9/9 19:57
 * 本地缓存
 */
public class LocalCacheService {


    /**
     * 放音编码与文件名对应关系
     */
    public final static Map<String, String> AUDIO_CODE_CACHE = new ConcurrentHashMap<>(16);

    public final static Map<String, String> LUA_DIGEST_CACHE = new ConcurrentHashMap<>(16);

}
