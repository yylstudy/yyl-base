package com.cqt.forward.cache;


import com.alibaba.nacos.api.naming.pojo.Instance;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author linshiqiang
 * date 2021/9/16 15:17
 * 本地缓存
 */
public class LocalCache {

    /**
     * 地市-机房对应关系
     * key: 地市编码
     * value:
     * A
     * B
     */
    public static final Map<String, String> AREA_LOCATION_CACHE = new ConcurrentHashMap<>(512);

    /**
     * 异地机房服务实例列表
     * key 服务名称
     * value 实例列表
     */
    public static final Map<String, List<Instance>> BACK_SERVER_INSTANCE_MAP = new ConcurrentHashMap<>(8);

    /**
     * 设置
     *
     * @param serviceName  服务名称
     * @param instanceList 实例列表
     */
    public static void put(String serviceName, List<Instance> instanceList) {
        BACK_SERVER_INSTANCE_MAP.put(serviceName, instanceList);
    }

    /**
     * 获取异地服务列表
     *
     * @return list
     */
    public static List<Instance> getServiceList(String serviceName) {
        return BACK_SERVER_INSTANCE_MAP.getOrDefault(serviceName, new ArrayList<>());
    }

    public static int size(String serviceName) {

        return getServiceList(serviceName).size();
    }

    /**
     * 获取地市编码所属机房
     *
     * @param areaCode 地市编码
     * @return 机房
     */
    public static String getLocation(String areaCode) {

        return AREA_LOCATION_CACHE.get(areaCode);
    }

}
