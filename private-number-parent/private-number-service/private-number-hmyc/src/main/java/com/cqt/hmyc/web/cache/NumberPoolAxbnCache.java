package com.cqt.hmyc.web.cache;

import cn.hutool.core.collection.CollUtil;
import com.cqt.common.util.PrivateCacheUtil;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author linshiqiang
 * @date 2022/3/7 14:19
 * AXBN 号码池缓存
 */
public class NumberPoolAxbnCache {

    /**
     * AXBN 号码池
     * 地市号码池map
     * key vccId:areaCode
     * value ArrayList<String></String>
     */
    public final static Map<String, HashSet<String>> AXBN_NUM_POOL_MAP = new ConcurrentHashMap<>(512);

    /**
     * 添加缓存 AXBN
     *
     * @param vccId    企业id
     * @param areaCode 地市编码
     * @param pool     地市池子
     */
    public static void addPool(String vccId, String areaCode, HashSet<String> pool) {
        String key = PrivateCacheUtil.getKey(vccId, areaCode);
        Optional<HashSet<String>> optional = getPool(vccId, areaCode);
        if (optional.isPresent()) {
            HashSet<String> set = optional.get();
            set.addAll(pool);
            AXBN_NUM_POOL_MAP.put(key, set);
            return;
        }
        AXBN_NUM_POOL_MAP.put(PrivateCacheUtil.getKey(vccId, areaCode), pool);
    }


    /**
     * 获取相对应号码池 AXBN
     */
    public static Optional<HashSet<String>> getPool(String vccId, String areaCode) {

        return Optional.ofNullable(AXBN_NUM_POOL_MAP.get(PrivateCacheUtil.getKey(vccId, areaCode)));
    }

    public static int size() {

        return AXBN_NUM_POOL_MAP.size();
    }

    public static void clear() {
        AXBN_NUM_POOL_MAP.clear();
    }

    /**
     * 移除一个元素
     */
    public static void remove(String vccId, String areaCode, String number) {
        String key = PrivateCacheUtil.getKey(vccId, areaCode);
        if (CollUtil.isNotEmpty(AXBN_NUM_POOL_MAP.get(key))) {
            AXBN_NUM_POOL_MAP.get(key).remove(number);
        }
    }

    /**
     * 批量移除元素
     */
    public static void removeAll(String vccId, String areaCode, List<String> numberList) {
        if (CollUtil.isEmpty(numberList)) {
            return;
        }
        String key = PrivateCacheUtil.getKey(vccId, areaCode);
        if (CollUtil.isNotEmpty(AXBN_NUM_POOL_MAP.get(key))) {
            numberList.forEach(AXBN_NUM_POOL_MAP.get(key)::remove);
        }
    }
}
