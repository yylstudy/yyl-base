package com.cqt.monitor.config.balancer;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author linshiqiang
 * @date 2021/9/26 12:54
 * 随机
 */
public class RandomLoadBalancer implements LoadBalancer {

    @Override
    public String getUrl(List<String> urlList) {
        int ind = ThreadLocalRandom.current().nextInt(urlList.size());
        return urlList.get(ind);
    }
}
