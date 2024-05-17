package com.cqt.hmyc.web.cache;

import cn.hutool.core.collection.CollUtil;
import com.cqt.common.util.PrivateCacheUtil;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author linshiqiang
 * @date 2022/3/7 14:19
 * AXG 号码池缓存
 */
public class NumberPoolAxgCache {

    /**
     * AXG 号码池
     * 地市号码池map
     * key vccId:areaCode
     * value List<String></String>
     */
    public final static Map<String, List<String>> AXG_NUM_POOL_MAP = new ConcurrentHashMap<>(512);

    /**
     * 添加缓存 AXG
     *
     * @param vccId    企业id
     * @param areaCode 地市编码
     * @param pool     地市池子
     */
    public static void addPool(String vccId, String areaCode, List<String> pool) {
        if (CollUtil.isEmpty(pool)) {
            return;
        }
        String key = PrivateCacheUtil.getKey(vccId, areaCode);
        Optional<List<String>> optional = getPool(vccId, areaCode);
        if (optional.isPresent()) {
            List<String> list = optional.get();
            list.addAll(pool);
            AXG_NUM_POOL_MAP.put(key, CollUtil.distinct(list));
            return;
        }

        AXG_NUM_POOL_MAP.put(key, pool);
    }

    /**
     * 获取相对应号码池 AXG
     */
    public static Optional<List<String>> getPool(String vccId, String areaCode) {

        return Optional.ofNullable(AXG_NUM_POOL_MAP.get(PrivateCacheUtil.getKey(vccId, areaCode)));
    }

    public static int size() {

        return AXG_NUM_POOL_MAP.size();
    }

    public static void clear() {
        AXG_NUM_POOL_MAP.clear();
    }

    /**
     * 移除一个元素
     */
    public static void remove(String vccId, String areaCode, String number) {
        String key = PrivateCacheUtil.getKey(vccId, areaCode);
        if (CollUtil.isNotEmpty(AXG_NUM_POOL_MAP.get(key))) {
            AXG_NUM_POOL_MAP.get(key).remove(number);
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
        if (CollUtil.isNotEmpty(AXG_NUM_POOL_MAP.get(key))) {
            numberList.forEach(AXG_NUM_POOL_MAP.get(key)::remove);
        }
    }
}
