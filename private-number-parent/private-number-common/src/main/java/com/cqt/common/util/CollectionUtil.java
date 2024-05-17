package com.cqt.common.util;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.RandomUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author linshiqiang
 * @date 2021/9/16 10:37
 */
public class CollectionUtil {

    /**
     * set 随机取出一个元素
     */
    public static String getRandomSet(Set<String> set) {
        if (CollUtil.isEmpty(set)) {
            return "";
        }
        ArrayList<String> list = new ArrayList<>(set);
        int i = RandomUtil.randomInt(list.size());
        return list.get(i);
    }

    public static String getRandomList(List<String> list) {
        if (CollUtil.isEmpty(list)) {
            return "";
        }
        int i = RandomUtil.randomInt(list.size());
        return list.get(i);
    }


    public static String getRandomSet2(Set<Object> set) {
        ArrayList<Object> list = new ArrayList<>(set);
        int i = RandomUtil.randomInt(list.size());
        return Convert.toStr(list.get(i));
    }


}
