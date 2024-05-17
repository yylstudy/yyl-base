package com.cqt.hmyc.config.balancer;

import cn.hutool.core.collection.CollUtil;
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
public class RoundRobinLoadBalancer implements LoadBalancer {

    private final AtomicInteger nextServerCyclicCounter;

    private final AtomicInteger axIndex;

    private final AtomicInteger axeIndex;

    private final AtomicInteger axeybIndex;

    private final AtomicInteger axyIndex;

    public RoundRobinLoadBalancer() {
        nextServerCyclicCounter = new AtomicInteger(0);
        axIndex = new AtomicInteger(0);
        axeIndex = new AtomicInteger(0);
        axeybIndex = new AtomicInteger(0);
        axyIndex = new AtomicInteger(0);
    }

    @Override
    public String get(List<String> numberList) {
        if (CollUtil.isEmpty(numberList)) {
            return "";
        }
        try {
            int index = incrementAndGetModulo(numberList.size());
            return numberList.get(index);
        } catch (Exception e) {
            log.error("get RoundRobinLoadBalancer error, ", e);
        }
        return "";
    }

    @Override
    public String getAx(List<String> numberList) {
        try {
            int index = incrementAndGetModulo(numberList.size(), axIndex);
            return numberList.get(index);
        } catch (Exception e) {
            log.error("get RoundRobinLoadBalancer error, ", e);
        }
        return "";
    }

    @Override
    public String getAxe(List<String> numberList) {
        try {
            int index = incrementAndGetModulo(numberList.size(), axeIndex);
            return numberList.get(index);
        } catch (Exception e) {
            log.error("get RoundRobinLoadBalancer error, ", e);
        }
        return "";
    }

    @Override
    public String getAxeyb(List<String> numberList) {
        try {
            int index = incrementAndGetModulo(numberList.size(), axeybIndex);
            return numberList.get(index);
        } catch (Exception e) {
            log.error("get RoundRobinLoadBalancer error, ", e);
        }
        return "";
    }

    @Override
    public String getAxy(List<String> numberList) {
        try {
            int index = incrementAndGetModulo(numberList.size(), axyIndex);
            return numberList.get(index);
        } catch (Exception e) {
            log.error("get RoundRobinLoadBalancer error, ", e);
        }
        return "";
    }

    private int incrementAndGetModulo(int modulo, AtomicInteger index) {
        for (; ; ) {
            int current = index.get();
            int next = (current + 1) % modulo;
            if (index.compareAndSet(current, next)) {
                return next;
            }
        }
    }

    private int incrementAndGetModulo(int modulo) {
        for (; ; ) {
            int current = nextServerCyclicCounter.get();
            int next = (current + 1) % modulo;
            if (nextServerCyclicCounter.compareAndSet(current, next)) {
                return next;
            }
        }
    }
}
