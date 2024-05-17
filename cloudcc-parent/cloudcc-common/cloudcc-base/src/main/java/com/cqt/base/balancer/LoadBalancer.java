package com.cqt.base.balancer;

import java.util.List;

/**
 * @author linshiqiang
 * date 2021/9/26 10:10
 */
public interface LoadBalancer<T> {

    /**
     * 轮训获取
     *
     * @param key      键
     * @param dataList 数据列表
     * @return 元素
     */
    T get(String key, List<T> dataList);

    T get(List<T> dataList);
}
