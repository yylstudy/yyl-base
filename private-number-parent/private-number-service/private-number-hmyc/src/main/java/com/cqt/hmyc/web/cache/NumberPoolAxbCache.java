package com.cqt.hmyc.web.cache;

import cn.hutool.core.collection.CollUtil;
import com.cqt.common.enums.AxbPoolTypeEnum;
import com.cqt.common.util.PrivateCacheUtil;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author linshiqiang
 * @date 2022/2/15 14:52
 * AXB号码池缓存
 */
@Slf4j
public class NumberPoolAxbCache {

    /**
     * axb 号码池
     * 地市号码池map
     * key vccId:areaCode
     * value set<String></String>
     */
    public final static Map<String, HashSet<String>> AXB_NUM_POOL_MAP = new ConcurrentHashMap<>(512);

    /**
     * axb 号码主池  号码副池  主池号码 + 副池号码 = 号码池
     * key 是区号 全国区号为0000
     */
    public final static Map<String, HashSet<String>> AXB_NUM_POOL_MASTER_MAP = new ConcurrentHashMap<>(512);


    /**
     * axb 号码副池  主池号码 + 副池号码 = 号码池
     * key 是区号 全国区号为0000
     */
    public final static Map<String, HashSet<String>> AXB_NUM_POOL_SLAVE_MAP = new ConcurrentHashMap<>(512);

    /**
     * 添加缓存
     *
     * @param poolTypeEnum 池子类型
     * @param vccId        企业id
     * @param areaCode     地市编码
     * @param poolSet      地市池子
     */
    public static void addPool(AxbPoolTypeEnum poolTypeEnum, String vccId, String areaCode, HashSet<String> poolSet) {
        if (CollUtil.isEmpty(poolSet)) {
            return;
        }
        String key = PrivateCacheUtil.getKey(vccId, areaCode);
        Optional<HashSet<String>> optional = getPool(poolTypeEnum, vccId, areaCode);
        switch (poolTypeEnum) {
            case ALL:
                if (optional.isPresent()) {
                    log.info("add axb pool all, key:{}, value:{}", key, poolSet.size());
                    HashSet<String> set = optional.get();
                    set.addAll(poolSet);
                    AXB_NUM_POOL_MAP.put(key, set);
                    return;
                }
                AXB_NUM_POOL_MAP.put(key, poolSet);
                return;
            case MASTER:
                if (optional.isPresent()) {
                    HashSet<String> set = optional.get();
                    set.addAll(poolSet);
                    AXB_NUM_POOL_MASTER_MAP.put(key, set);
                    return;
                }
                AXB_NUM_POOL_MASTER_MAP.put(key, poolSet);
                return;
            case SLAVE:
                if (optional.isPresent()) {
                    HashSet<String> set = optional.get();
                    set.addAll(poolSet);
                    AXB_NUM_POOL_SLAVE_MAP.put(key, set);
                    return;
                }
                AXB_NUM_POOL_SLAVE_MAP.put(key, poolSet);
                return;
            default:
                break;
        }
    }

    public static void addPool(AxbPoolTypeEnum poolTypeEnum, String vccId, String areaCode, String number) {
        String key = PrivateCacheUtil.getKey(vccId, areaCode);
        Optional<HashSet<String>> optional = getPool(poolTypeEnum, vccId, areaCode);
        if (optional.isPresent()) {
            optional.get().add(number);
            return;
        }
        switch (poolTypeEnum) {
            case ALL:
                AXB_NUM_POOL_MAP.put(key, Sets.newHashSet(number));
                return;
            case MASTER:
                AXB_NUM_POOL_MASTER_MAP.put(key, Sets.newHashSet(number));
                return;
            case SLAVE:
                AXB_NUM_POOL_SLAVE_MAP.put(key, Sets.newHashSet(number));
                return;
            default:
                break;
        }
    }

    /**
     * 获取相对应AXB号码池
     */
    public static Optional<HashSet<String>> getPool(AxbPoolTypeEnum poolTypeEnum, String vccId, String areaCode) {
        String key = PrivateCacheUtil.getKey(vccId, areaCode);
        switch (poolTypeEnum) {
            case ALL:
                return Optional.ofNullable(AXB_NUM_POOL_MAP.get(key));
            case MASTER:
                return Optional.ofNullable(AXB_NUM_POOL_MASTER_MAP.get(key));
            case SLAVE:
                return Optional.ofNullable(AXB_NUM_POOL_SLAVE_MAP.get(key));
            default:
                break;
        }
        return Optional.empty();
    }

    /**
     * 判断X号码是否在池子中
     *
     * @param vccId    企业id
     * @param areaCode 地市编码
     * @param telX     X号码
     * @return true 在池子中 false 不在池子中
     */
    public static Boolean isExistAllPool(String vccId, String areaCode, String telX) {
        String key = PrivateCacheUtil.getKey(vccId, areaCode);

        HashSet<String> allPool = AXB_NUM_POOL_MAP.get(key);
        if (CollUtil.isEmpty(allPool)) {
            return false;
        }

        return allPool.contains(telX);
    }

    /**
     * 键
     */
    public static String getKey(String vccId, String areaCode) {

        return vccId + ":" + areaCode;
    }

    /**
     * 全部号码数量
     */
    public static int sizeAll() {

        return AXB_NUM_POOL_MAP.size();
    }

    /**
     * 主池号码数量
     */
    public static int sizeMaster() {

        return AXB_NUM_POOL_MASTER_MAP.size();
    }

    /**
     * 备池号码池数量
     */
    public static int sizeSlave() {

        return AXB_NUM_POOL_SLAVE_MAP.size();
    }

    /**
     * 清空
     */
    public static void clear() {
        AXB_NUM_POOL_MAP.clear();
        AXB_NUM_POOL_MASTER_MAP.clear();
        AXB_NUM_POOL_SLAVE_MAP.clear();
    }

    /**
     * 移除一个元素
     */
    public static void remove(String vccId, String areaCode, String number) {
        String key = PrivateCacheUtil.getKey(vccId, areaCode);

        if (CollUtil.isNotEmpty(AXB_NUM_POOL_MAP.get(key))) {
            AXB_NUM_POOL_MAP.get(key).remove(number);
        }
        if (CollUtil.isNotEmpty(AXB_NUM_POOL_MASTER_MAP.get(key))) {
            AXB_NUM_POOL_MASTER_MAP.get(key).remove(number);
        }
        if (CollUtil.isNotEmpty(AXB_NUM_POOL_SLAVE_MAP.get(key))) {
            AXB_NUM_POOL_SLAVE_MAP.get(key).remove(number);
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
        if (CollUtil.isNotEmpty(AXB_NUM_POOL_MAP.get(key))) {
            numberList.forEach(AXB_NUM_POOL_MAP.get(key)::remove);
        }
        if (CollUtil.isNotEmpty(AXB_NUM_POOL_MASTER_MAP.get(key))) {
            numberList.forEach(AXB_NUM_POOL_MASTER_MAP.get(key)::remove);
        }
        if (CollUtil.isNotEmpty(AXB_NUM_POOL_SLAVE_MAP.get(key))) {
            numberList.forEach(AXB_NUM_POOL_SLAVE_MAP.get(key)::remove);
        }
    }
}
