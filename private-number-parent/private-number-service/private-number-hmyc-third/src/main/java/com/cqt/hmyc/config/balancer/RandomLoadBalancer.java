package com.cqt.hmyc.config.balancer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author linshiqiang
 * @date 2021/9/26 12:54
 * 随机
 */
@Component
@Slf4j
public class RandomLoadBalancer implements LoadBalancer {

    @Override
    public String get(List<String> numberList) {
        return getNum(numberList);
    }

    private String getNum(List<String> numberList) {
        int ind = ThreadLocalRandom.current().nextInt(numberList.size());
        return numberList.get(ind);
    }

    @Override
    public String getAx(List<String> numberList) {
        return getNum(numberList);
    }

    @Override
    public String getAxe(List<String> numberList) {
        return getNum(numberList);
    }

    @Override
    public String getAxeyb(List<String> numberList) {
        return getNum(numberList);
    }

    @Override
    public String getAxy(List<String> numberList) {
        return getNum(numberList);
    }
}
