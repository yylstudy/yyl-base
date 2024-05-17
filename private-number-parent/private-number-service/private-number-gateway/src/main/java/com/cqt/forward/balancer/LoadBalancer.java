package com.cqt.forward.balancer;

import com.alibaba.nacos.api.naming.pojo.Instance;

import java.util.List;

/**
 * @author linshiqiang
 * @date 2021/9/26 10:10
 */
public interface LoadBalancer {


    /**
     * 获取远程服务实例
     * @param backServerList 服务列表
     * @return 服务实例
     */
    Instance getInstance(List<Instance> backServerList);
}
