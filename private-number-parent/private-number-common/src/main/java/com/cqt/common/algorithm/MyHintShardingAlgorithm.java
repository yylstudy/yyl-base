package com.cqt.common.algorithm;

import cn.hutool.core.collection.ListUtil;
import org.apache.shardingsphere.api.sharding.hint.HintShardingAlgorithm;
import org.apache.shardingsphere.api.sharding.hint.HintShardingValue;

import java.util.Collection;
import java.util.Optional;

/**
 * @author linshiqiang
 * @date 2022/2/21 17:04
 */
public class MyHintShardingAlgorithm implements HintShardingAlgorithm<String> {

    @Override
    public Collection<String> doSharding(Collection<String> availableTargetNames, HintShardingValue<String> shardingValue) {

        String logicTableName = shardingValue.getLogicTableName();
        Collection<String> values = shardingValue.getValues();
        Optional<String> first = values.stream().findFirst();
        if (first.isPresent()) {
            return ListUtil.toList(logicTableName + "_" + first.get());
        }
        return ListUtil.toList(logicTableName);
    }
}
