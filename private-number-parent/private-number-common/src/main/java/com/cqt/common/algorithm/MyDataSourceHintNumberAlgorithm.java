package com.cqt.common.algorithm;

import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.api.sharding.hint.HintShardingAlgorithm;
import org.apache.shardingsphere.api.sharding.hint.HintShardingValue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

/**
 * @author linshiqiang
 * @date 2022/7/6 14:55
 * 分库算法-分配键为X号码tel_x
 */
@Slf4j
public class MyDataSourceHintNumberAlgorithm implements HintShardingAlgorithm<String> {

    private static final String DS = "bind";

    private static final String BIND_0 = "bind0";

    private static Integer DS_COUNT = 2;

    static {
        // 查询配置数据库数量
        String count = SpringUtil.getProperty("spring.shardingsphere.datasource.count");
        if (StrUtil.isNotEmpty(count)) {
            DS_COUNT = Integer.parseInt(count);
        }
    }

    @Override
    public Collection<String> doSharding(Collection<String> availableTargetNames, HintShardingValue<String> shardingValue) {

        // hint分片值  fnv(tel_x)
        Collection<String> values = shardingValue.getValues();
        Optional<String> first = values.stream().findFirst();
        if (first.isPresent()) {
            String value = first.get();
            int index = Integer.parseInt(value) % DS_COUNT;
            String ds = DS + index;
            log.debug("ds: {}", ds);
            return Lists.newArrayList(ds);
        }
        // 没有设置分片值, 返回所有库
        if (DS_COUNT == 1) {
            return Lists.newArrayList(BIND_0);
        }
        ArrayList<String> dbs = new ArrayList<>();
        for (int i = 0; i < DS_COUNT; i++) {
            dbs.add(DS + i);
        }
        return dbs;
    }
}
