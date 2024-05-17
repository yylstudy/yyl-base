package com.cqt.hmyc.web.cache;

import cn.hutool.core.collection.CollUtil;
import com.cqt.common.util.PrivateCacheUtil;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author linshiqiang
 * @date 2022/2/15 14:52
 * AXYB号码池缓存
 */
public class NumberPoolAxybCache {

    /**
     * axyb X号码池
     * 地市号码池map
     * key vccId:areaCode
     * value List<String></String>
     */
    public final static Map<String, List<String>> X_NUM_POOL_MAP = new ConcurrentHashMap<>(16);

    /**
     * axyb X号码池
     * 地市号码池map
     * key vccId:areaCode
     * value List<String></String>
     */
    public final static Map<String, List<String>> Y_NUM_POOL_MAP = new ConcurrentHashMap<>(16);

    /**
     * 添加缓存 X号码池
     *
     * @param vccId    企业id
     * @param areaCode 地市编码
     * @param pool     地市池子
     */
    public static void addPoolX(String vccId, String areaCode, List<String> pool) {

        if (CollUtil.isEmpty(pool)) {
            return;
        }
        String key = PrivateCacheUtil.getKey(vccId, areaCode);
        Optional<List<String>> optional = getPoolX(vccId, areaCode);
        if (optional.isPresent()) {
            List<String> list = optional.get();
            list.addAll(pool);
            X_NUM_POOL_MAP.put(key, CollUtil.distinct(list));
            return;
        }

        X_NUM_POOL_MAP.put(key, pool);
    }

    /**
     * 添加缓存 Y号码池
     *
     * @param vccId    企业id
     * @param areaCode 地市编码
     * @param pool     地市池子
     */
    public static void addPoolY(String vccId, String areaCode, List<String> pool) {
        if (CollUtil.isEmpty(pool)) {
            return;
        }
        String key = PrivateCacheUtil.getKey(vccId, areaCode);
        Optional<List<String>> optional = getPoolX(vccId, areaCode);
        if (optional.isPresent()) {
            List<String> list = optional.get();
            list.addAll(pool);
            Y_NUM_POOL_MAP.put(key, CollUtil.distinct(list));
            return;
        }

        Y_NUM_POOL_MAP.put(key, pool);
    }

    /**
     * 获取相对应AXYB号码池
     */
    public static Optional<List<String>> getPoolX(String vccId, String areaCode) {

        return Optional.ofNullable(X_NUM_POOL_MAP.get(PrivateCacheUtil.getKey(vccId, areaCode)));
    }

    public static Optional<List<String>> getPoolY(String vccId, String areaCode) {

        return Optional.ofNullable(Y_NUM_POOL_MAP.get(PrivateCacheUtil.getKey(vccId, areaCode)));
    }

    public static int sizeX() {

        return X_NUM_POOL_MAP.size();
    }

    public static int sizeY() {

        return Y_NUM_POOL_MAP.size();
    }

    public static void clear() {
        X_NUM_POOL_MAP.clear();
        Y_NUM_POOL_MAP.clear();
    }

    /**
     * 移除一个元素
     */
    public static void removeX(String vccId, String areaCode, String number) {
        String key = PrivateCacheUtil.getKey(vccId, areaCode);
        if (CollUtil.isNotEmpty(X_NUM_POOL_MAP.get(key))) {
            X_NUM_POOL_MAP.get(key).remove(number);
        }
    }

    public static void removeY(String vccId, String areaCode, String number) {
        String key = PrivateCacheUtil.getKey(vccId, areaCode);
        if (CollUtil.isNotEmpty(Y_NUM_POOL_MAP.get(key))) {
            Y_NUM_POOL_MAP.get(key).remove(number);
        }
    }

    /**
     * 批量移除元素
     */
    public static void removeAllX(String vccId, String areaCode, List<String> numberList) {
        if (CollUtil.isEmpty(numberList)) {
            return;
        }
        String key = PrivateCacheUtil.getKey(vccId, areaCode);
        if (CollUtil.isNotEmpty(X_NUM_POOL_MAP.get(key))) {
            numberList.forEach(X_NUM_POOL_MAP.get(key)::remove);
        }
    }

    /**
     * 批量移除元素
     */
    public static void removeAllY(String vccId, String areaCode, List<String> numberList) {
        if (CollUtil.isEmpty(numberList)) {
            return;
        }
        String key = PrivateCacheUtil.getKey(vccId, areaCode);
        if (CollUtil.isNotEmpty(Y_NUM_POOL_MAP.get(key))) {
            numberList.forEach(Y_NUM_POOL_MAP.get(key)::remove);
        }
    }
}
