package com.cqt.recycle.config.sharding;

import cn.hutool.core.date.DateUtil;
import org.apache.shardingsphere.api.sharding.standard.PreciseShardingAlgorithm;
import org.apache.shardingsphere.api.sharding.standard.PreciseShardingValue;

import java.util.Collection;
import java.util.Date;

/**
 * @author linshiqiang
 * @date 2021/12/6 14:59
 */
public class DatePreciseShardingAlgorithm implements PreciseShardingAlgorithm<Date> {

    @Override
    public String doSharding(Collection<String> availableTargetNames, PreciseShardingValue<Date> preciseShardingValue) {
        Date date = preciseShardingValue.getValue();
        String suffix = DateUtil.format(date, "yyyyMMdd");
        for (String tableName : availableTargetNames) {
            if (tableName.endsWith(suffix)) {
                return tableName;
            }
        }
        throw new IllegalArgumentException("未找到匹配的数据表");
    }
}