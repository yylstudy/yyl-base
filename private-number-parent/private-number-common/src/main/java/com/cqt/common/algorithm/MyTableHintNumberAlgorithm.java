package com.cqt.common.algorithm;

import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.api.sharding.hint.HintShardingAlgorithm;
import org.apache.shardingsphere.api.sharding.hint.HintShardingValue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * @author linshiqiang
 * @date 2022/2/21 17:04
 * 分表算法-分配键为X号码tel_x
 */
@Slf4j
public class MyTableHintNumberAlgorithm implements HintShardingAlgorithm<String> {


    private static Integer SHARDING_COUNT = 5;

    static {
        // 查询配置表分片数量
        String count = SpringUtil.getProperty("spring.shardingsphere.datasource.tableShardingCount");
        if (StrUtil.isNotEmpty(count)) {
            SHARDING_COUNT = Integer.parseInt(count);
        }
    }

    @Override
    public Collection<String> doSharding(Collection<String> availableTargetNames, HintShardingValue<String> shardingValue) {

        // hint分片值 vcc_id@fnv(tel_x)
        Collection<String> values = shardingValue.getValues();
        Optional<String> first = values.stream().findFirst();
        if (!first.isPresent()) {
            throw new RuntimeException("未配置分片值");
        }
        String logicTableName = shardingValue.getLogicTableName();
        String value = first.get();
        List<String> list = StrUtil.split(value, StrUtil.AT);
        String vccId = list.get(0);
        // 没有@fnv(tel_x), 只有vccId, 返回所有分片表
        // 统计查询场景-ex 分机号余量查询
        if (list.size() == 1) {
            ArrayList<String> tables = new ArrayList<>();
            for (int i = 0; i < SHARDING_COUNT; i++) {
                tables.add(logicTableName + "_" + vccId + "_" + i);
            }
            return tables;
        }
        String numberHash = list.get(1);
        // 根据x号码的hash 模 分片数量
        int index = Integer.parseInt(numberHash) % SHARDING_COUNT;
        String table = logicTableName + "_" + vccId + "_" + index;
        log.debug("table: {}", table);
        return Lists.newArrayList(table);
    }
}
