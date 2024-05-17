package com.cqt.hmyc.web.cache;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author linshiqiang
 * @date 2022/2/15 15:23
 * 号码类型缓存
 */
public class NumberTypeCache {

    /**
     * 号码类型
     */
    public final static Map<String, String> NUM_TYPE_MAP = new ConcurrentHashMap<>(8192);

    public static void put(String number, String type) {
        NUM_TYPE_MAP.put(number, type);
    }

    public static void putAll(Map<String, String> map) {
        NUM_TYPE_MAP.putAll(map);
    }

    public static void remove(String number){
        NUM_TYPE_MAP.remove(number);
    }

    public static String getNumType(String number) {

        return NUM_TYPE_MAP.get(number);
    }

    public static int size() {

        return NUM_TYPE_MAP.size();
    }

    public static void clear() {
        NUM_TYPE_MAP.clear();
    }
}
