package com.cqt.hmyc.config.algorithm;

import cn.hutool.core.util.StrUtil;
import org.apache.shardingsphere.api.sharding.standard.PreciseShardingAlgorithm;
import org.apache.shardingsphere.api.sharding.standard.PreciseShardingValue;

import java.util.Collection;

/**
 * @author linshiqiang
 * @date 2022/7/6 14:55
 * 精确分库算法
 */
public class MyDataSourcePreciseAlgorithm implements PreciseShardingAlgorithm<String> {

    @Override
    public String doSharding(Collection<String> availableTargetNames, PreciseShardingValue<String> shardingValue) {

        try {
            // 绑定id 最后几位对2取模
            String value = shardingValue.getValue();
            int index = Integer.parseInt(StrUtil.subSuf(value, -3));

            return index % 2 == 0 ? "ds0" : "ds1";
        } catch (NumberFormatException e) {
            return "ds0";
        }
    }
}
