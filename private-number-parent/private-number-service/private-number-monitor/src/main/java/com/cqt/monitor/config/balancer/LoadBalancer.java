package com.cqt.monitor.config.balancer;

import java.util.List;

/**
 * @author linshiqiang
 * @date 2021/9/26 10:10
 */
public interface LoadBalancer {


    /**
     * 获取远程服务实例
     * @param urlList 服务列表
     * @return 服务实例
     */
    String getUrl(List<String> urlList);
}
