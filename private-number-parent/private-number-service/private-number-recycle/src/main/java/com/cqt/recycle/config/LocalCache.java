package com.cqt.recycle.config;


import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author linshiqiang
 * @date 2021/9/16 15:17
 * 本地缓存
 */
public class LocalCache {

    /**
     * 美团小号 地市-机房对应关系
     * key: 地市编码
     * value:
     * 本机房 local
     * 异地机房 back
     */
    public static final Map<String, String> AREA_LOCATION_CACHE = new ConcurrentHashMap<>(512);
}
