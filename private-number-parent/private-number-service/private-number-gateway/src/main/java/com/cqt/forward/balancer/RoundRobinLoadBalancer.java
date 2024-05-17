package com.cqt.forward.balancer;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.nacos.api.naming.pojo.Instance;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author linshiqiang
 * @date 2021/9/26 10:11
 * 轮训
 */
@Component
@Slf4j
public class RoundRobinLoadBalancer implements LoadBalancer{

    private final AtomicInteger nextServerCyclicCounter;

    public RoundRobinLoadBalancer() {
        nextServerCyclicCounter = new AtomicInteger(0);
    }

    @Override
    public Instance getInstance(List<Instance> backServerList) {
        if (CollUtil.isEmpty(backServerList)) {
            return null;
        }
        try {
            int index = incrementAndGetModulo(backServerList.size());
            return backServerList.get(index);
        } catch (Exception e) {
            log.error("get RoundRobinLoadBalancer error, ", e);
        }
        return null;
    }

    private int incrementAndGetModulo(int modulo) {
        for (;;) {
            int current = nextServerCyclicCounter.get();
            int next = (current + 1) % modulo;
            if (nextServerCyclicCounter.compareAndSet(current, next)) {
                return next;
            }
        }
    }
}
