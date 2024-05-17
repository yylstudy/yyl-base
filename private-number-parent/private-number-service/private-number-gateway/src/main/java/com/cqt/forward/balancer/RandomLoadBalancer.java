package com.cqt.forward.balancer;

import com.alibaba.nacos.api.naming.pojo.Instance;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author linshiqiang
 * @date 2021/9/26 12:54
 * 随机
 */
public class RandomLoadBalancer implements LoadBalancer{

    @Override
    public Instance getInstance(List<Instance> backServerList) {
        int ind = ThreadLocalRandom.current().nextInt(backServerList.size());
        return backServerList.get(ind);
    }
}
