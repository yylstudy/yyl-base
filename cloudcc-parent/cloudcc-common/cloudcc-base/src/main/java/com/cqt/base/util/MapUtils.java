package com.cqt.base.util;

import cn.hutool.core.collection.CollUtil;

import java.util.*;

/**
 * @author linshiqiang
 * date:  2023-07-20 14:56
 */
public class MapUtils {

    /**
     * map根据技能权值分组
     * 并排序
     *
     * @param <K>     键类型
     * @param <V>     值类型
     * @param entries entry列表
     * @return map
     */
    public static <K, V> Map<V, List<K>> groupByValue(Iterable<Map.Entry<K, V>> entries, boolean isOrderByKey) {
        Map<V, List<K>> map;
        if (isOrderByKey) {
            map = new TreeMap<>();
        } else {
            map = new LinkedHashMap<>();
        }
        if (CollUtil.isEmpty(entries)) {
            return map;
        }
        for (final Map.Entry<K, V> pair : entries) {
            final List<K> values = map.computeIfAbsent(pair.getValue(), k -> new ArrayList<>());
            values.add(pair.getKey());
        }
        return map;
    }
}
