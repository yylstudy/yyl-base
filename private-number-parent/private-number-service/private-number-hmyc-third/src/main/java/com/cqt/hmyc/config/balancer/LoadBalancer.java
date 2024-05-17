package com.cqt.hmyc.config.balancer;

import java.util.List;

/**
 * @author linshiqiang
 * @date 2021/9/26 10:10
 */
public interface LoadBalancer {


    /**
     * 获取list
     * @param numberList numberList
     * @return string
     */
    String get(List<String> numberList);

    String getAx(List<String> numberList);

    String getAxe(List<String> numberList);

    String getAxeyb(List<String> numberList);

    String getAxy(List<String> numberList);

}
