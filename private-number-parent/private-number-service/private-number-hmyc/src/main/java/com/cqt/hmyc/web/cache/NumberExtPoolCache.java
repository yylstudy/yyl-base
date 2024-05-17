package com.cqt.hmyc.web.cache;

import cn.hutool.core.collection.CollUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author linshiqiang
 * @date 2022/2/15 15:23
 * 分机号缓存
 */
@Slf4j
public class NumberExtPoolCache {

    /**
     * 分机号初始化 list
     * 0000 - 9999
     */
    public final static List<String> X_EXT_POOL_10000_LIST = new ArrayList<>();

    /**
     * 1000 - 9999
     */
    public final static List<String> X_EXT_POOL_9000_LIST = new ArrayList<>();

    public static List<String> getExtNumList(Integer extCount) {
        init();
        try {
            if (extCount == 9000) {
                Collections.shuffle(X_EXT_POOL_9000_LIST);
                return X_EXT_POOL_9000_LIST;
            }
            Collections.shuffle(X_EXT_POOL_10000_LIST);
            return X_EXT_POOL_10000_LIST;
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    public static void init() {
        if (CollUtil.isNotEmpty(NumberExtPoolCache.X_EXT_POOL_10000_LIST)) {
            return;
        }
        List<Integer> extList = IntStream.range(0, 10000).boxed().collect(Collectors.toList());
        extList.forEach(item -> {
            String ext = String.format("%04d", item);
            NumberExtPoolCache.X_EXT_POOL_10000_LIST.add(ext);
            if (item >= 1000) {
                NumberExtPoolCache.X_EXT_POOL_9000_LIST.add(ext);
            }
        });
        Collections.shuffle(NumberExtPoolCache.X_EXT_POOL_9000_LIST);
        Collections.shuffle(NumberExtPoolCache.X_EXT_POOL_10000_LIST);

        log.info("初始化 set 0000 - 9999 分机号初始化: {} 个", NumberExtPoolCache.X_EXT_POOL_10000_LIST.size());
        log.info("初始化 set 1000 - 9999 分机号初始化: {} 个", NumberExtPoolCache.X_EXT_POOL_9000_LIST.size());
    }

}
