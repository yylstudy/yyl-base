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
 * AX 号码池缓存
 */
public class NumberPoolAxCache {

    /**
     * AX 号码池
     * 地市号码池map
     * key vccId:areaCode
     * value ArrayList<String></String>
     */
    public final static Map<String, List<String>> AX_NUM_POOL_MAP = new ConcurrentHashMap<>(512);


    /**
     * 添加缓存AX
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
            AX_NUM_POOL_MAP.put(key, CollUtil.distinct(list));
            return;
        }

        AX_NUM_POOL_MAP.put(key, pool);
    }

    /**
     * 获取相对应AXE号码池AX
     */
    public static Optional<List<String>> getPool(String vccId, String areaCode) {

        return Optional.ofNullable(AX_NUM_POOL_MAP.get(PrivateCacheUtil.getKey(vccId, areaCode)));
    }

    public static String getKey(String vccId, String areaCode) {

        return vccId + ":" + areaCode;
    }

    public static int size() {

        return AX_NUM_POOL_MAP.size();
    }

    public static void clear() {
        AX_NUM_POOL_MAP.clear();
    }

    /**
     * 移除一个元素
     */
    public static void remove(String vccId, String areaCode, String number) {
        String key = PrivateCacheUtil.getKey(vccId, areaCode);
        if (CollUtil.isNotEmpty(AX_NUM_POOL_MAP.get(key))) {
            AX_NUM_POOL_MAP.get(key).remove(number);
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
        if (CollUtil.isNotEmpty(AX_NUM_POOL_MAP.get(key))) {
            numberList.forEach(AX_NUM_POOL_MAP.get(key)::remove);
        }
    }
}
