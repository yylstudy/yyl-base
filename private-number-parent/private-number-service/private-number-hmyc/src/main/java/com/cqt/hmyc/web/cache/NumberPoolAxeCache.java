package com.cqt.hmyc.web.cache;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import com.cqt.common.util.PrivateCacheUtil;
import com.cqt.model.bind.dto.NumberAreaCodeDTO;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author linshiqiang
 * @date 2022/2/15 14:52
 * AXE 号码池缓存
 */
public class NumberPoolAxeCache {

    /**
     * AXE 号码池
     * 地市号码池map
     * key vccId:areaCode
     * value List<String></String>
     */
    public final static Map<String, List<String>> AXE_NUM_POOL_MAP = new ConcurrentHashMap<>(512);

    /**
     * AXE模式的Y显号码
     * AXE-Y    Y不可呼通
     * key vccId:areaCode
     * value List<String>
     */
    public final static Map<String, List<String>> AXE_Y_NUM_POOL_MAP = new ConcurrentHashMap<>(512);

    /**
     * 添加AXE-Y池子
     */
    public static void addPoolOfAxeY(String vccId, String areaCode, List<String> pool) {

        AXE_Y_NUM_POOL_MAP.put(PrivateCacheUtil.getKey(vccId, areaCode), pool);
    }

    /**
     * 获取AXE-Y池子
     */
    public static Optional<List<String>> getPoolOfAxeY(String vccId, String areaCode) {

        return Optional.ofNullable(AXE_Y_NUM_POOL_MAP.get(PrivateCacheUtil.getKey(vccId, areaCode)));
    }


    /**
     * 添加缓存AXE
     *
     * @param vccId    企业id
     * @param areaCode 地市编码
     * @param pool     地市池子
     */
    public static void addPool(String vccId, String areaCode, List<String> pool) {
        String key = PrivateCacheUtil.getKey(vccId, areaCode);
        Optional<List<String>> optional = getPool(vccId, areaCode);
        if (optional.isPresent()) {
            List<String> list = optional.get();
            list.addAll(pool);
            AXE_NUM_POOL_MAP.put(key, CollUtil.distinct(list));
            return;
        }

        AXE_NUM_POOL_MAP.put(key, pool);
    }

    /**
     * 获取相对应AXE号码池AXE
     */
    public static Optional<List<String>> getPool(String vccId, String areaCode) {

        return Optional.ofNullable(AXE_NUM_POOL_MAP.get(PrivateCacheUtil.getKey(vccId, areaCode)));
    }

    /**
     * 判断X号码是否在号码池内
     */
    public static Boolean isInAxePool(String vccId, String areaCode, String telX) {
        Optional<List<String>> optional = getPool(vccId, areaCode);
        return optional.map(list -> list.contains(telX)).orElse(false);
    }

    /**
     * 获取池子副本
     */
    public static List<String> getPoolReplica(String vccId, String areaCode) {
        List<String> list = AXE_NUM_POOL_MAP.get(PrivateCacheUtil.getKey(vccId, areaCode));
        if (CollUtil.isNotEmpty(list)) {
            ArrayList<String> pool = new ArrayList<>(list);
            Collections.shuffle(pool);
            return pool;
        }
        return new ArrayList<>();
    }

    public static List<NumberAreaCodeDTO> getNumberPoolReplica(String vccId, String areaCode) {
        List<String> list = AXE_NUM_POOL_MAP.get(PrivateCacheUtil.getKey(vccId, areaCode));
        if (CollUtil.isNotEmpty(list)) {
            List<NumberAreaCodeDTO> pool = new ArrayList<>();
            for (String number : list) {
                pool.add(NumberAreaCodeDTO.builder().number(number).areaCode(areaCode).build());
            }
            Collections.shuffle(pool);
            return pool;
        }
        return new ArrayList<>();
    }

    public static int size() {

        return AXE_NUM_POOL_MAP.size();
    }

    public static int sizeOfAxeY() {

        return AXE_Y_NUM_POOL_MAP.size();
    }

    public static void clear() {
        AXE_NUM_POOL_MAP.clear();
        AXE_Y_NUM_POOL_MAP.clear();
    }


    /**
     * 移除一个元素
     */
    public static void remove(String vccId, String areaCode, String number) {
        String key = PrivateCacheUtil.getKey(vccId, areaCode);
        if (CollUtil.isNotEmpty(AXE_NUM_POOL_MAP.get(key))) {
            AXE_NUM_POOL_MAP.get(key).remove(number);
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
        if (CollUtil.isNotEmpty(AXE_NUM_POOL_MAP.get(key))) {
            numberList.forEach(AXE_NUM_POOL_MAP.get(key)::remove);
        }
    }

    public static Long getAxePoolCount(String key) {
        return Convert.toLong(AXE_NUM_POOL_MAP.get(key).size());
    }
}
