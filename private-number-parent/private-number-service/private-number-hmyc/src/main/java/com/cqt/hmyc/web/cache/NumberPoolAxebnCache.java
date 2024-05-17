package com.cqt.hmyc.web.cache;

import com.cqt.common.util.PrivateCacheUtil;

import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author linshiqiang
 * @date 2022/3/7 14:19
 * AXEBN 号码池缓存
 */
@Deprecated
public class NumberPoolAxebnCache {

    /**
     * AXEBN 号码池
     * 地市号码池map
     * key vccId:areaCode
     * value ArrayList<String></String>
     */
    public final static Map<String, ArrayList<String>> AXEBN_NUM_POOL_MAP = new ConcurrentHashMap<>(512);

    /**
     * 添加缓存 AXEBN
     *
     * @param vccId    企业id
     * @param areaCode 地市编码
     * @param pool     地市池子
     */
    public static void addPool(String vccId, String areaCode, ArrayList<String> pool) {

        AXEBN_NUM_POOL_MAP.put(PrivateCacheUtil.getKey(vccId, areaCode), pool);
    }

    /**
     * 获取相对应AXE号码池AXEBN
     */
    public static Optional<ArrayList<String>> getPool(String vccId, String areaCode) {

        return Optional.ofNullable(AXEBN_NUM_POOL_MAP.get(PrivateCacheUtil.getKey(vccId, areaCode)));
    }

    public static int size() {

        return AXEBN_NUM_POOL_MAP.size();
    }

    public static void clear() {
        AXEBN_NUM_POOL_MAP.clear();
    }
}
