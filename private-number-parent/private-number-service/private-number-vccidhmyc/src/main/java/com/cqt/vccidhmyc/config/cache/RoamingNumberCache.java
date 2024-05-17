package com.cqt.vccidhmyc.config.cache;

import cn.hutool.core.util.StrUtil;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author linshiqiang
 * date:  2023-03-27 14:32
 * 漫游号缓存
 */
public class RoamingNumberCache {

    private static final HashSet<String> ROAMING_SET = new HashSet<>();

    public static void addAll(List<String> list) {
        Set<String> set = list.stream()
                .distinct()
                .filter(StrUtil::isNotBlank)
                .collect(Collectors.toSet());
        ROAMING_SET.addAll(set);
    }

    public static int size() {
        return ROAMING_SET.size();
    }

    public static Boolean isExist(String number) {
        return ROAMING_SET.contains(number);
    }

}
