package com.cqt.hmyc.web.bind.cache;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author dingsh
 * @date 2022/07/28
 */
public class LocalCacheService {

    /**
     * (hdh)放音编码与文件名对应关系
     */
    public final static Map<String, String> HDH_AUDIO_CODE_CACHE = new ConcurrentHashMap<>(16);

    /**
     * 本地ip
     */
    public static String LOCAL_IP;
}
