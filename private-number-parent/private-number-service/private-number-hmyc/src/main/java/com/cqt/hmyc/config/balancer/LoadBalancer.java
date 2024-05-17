package com.cqt.hmyc.config.balancer;

import java.util.List;

/**
 * @author linshiqiang
 * @date 2021/9/26 10:10
 */
public interface LoadBalancer<T> {

    /**
     * 轮训获取
     *
     * @param key
     * @param dataList
     * @return
     */
    T get(String key, List<T> dataList);

    T get(List<T> dataList);
}
