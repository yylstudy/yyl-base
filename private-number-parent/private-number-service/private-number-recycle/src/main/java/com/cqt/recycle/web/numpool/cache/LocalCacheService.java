package com.cqt.recycle.web.numpool.cache;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author linshiqiang
 * @date 2021/9/9 19:57
 * 本地缓存
 */
@Slf4j
public class LocalCacheService {

    public final static List<String> X_EXT_POOL_LIST = new ArrayList<>();

    static {
        List<Integer> extList = IntStream.range(0, 10000).boxed().collect(Collectors.toList());
        extList.forEach(item -> {
            String ext = String.format("%04d", item);
            X_EXT_POOL_LIST.add(ext);
        });
        Collections.shuffle(X_EXT_POOL_LIST);
        log.info("初始化 set 分机号初始化: {} 个", X_EXT_POOL_LIST.size());
    }
}
