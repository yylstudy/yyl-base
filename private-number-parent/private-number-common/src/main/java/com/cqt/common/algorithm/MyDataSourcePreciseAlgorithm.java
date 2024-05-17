package com.cqt.common.algorithm;

import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import org.apache.shardingsphere.api.sharding.standard.PreciseShardingAlgorithm;
import org.apache.shardingsphere.api.sharding.standard.PreciseShardingValue;

import java.util.Collection;

/**
 * @author linshiqiang
 * @date 2022/7/6 14:55
 * 精确分库算法
 */
public class MyDataSourcePreciseAlgorithm implements PreciseShardingAlgorithm<String> {

    private static final String DS = "bind";

    private static Integer DS_COUNT = 2;

    static {
        // 查询配置数据库数量
        String count = SpringUtil.getProperty("spring.shardingsphere.datasource.count");
        if (StrUtil.isNotEmpty(count)) {
            DS_COUNT = Integer.parseInt(count);
        }
    }

    @Override
    public String doSharding(Collection<String> availableTargetNames, PreciseShardingValue<String> shardingValue) {

        try {
            // 绑定id 最后几位对2取模
            String value = shardingValue.getValue();
            return getDs(value);
        } catch (Exception e) {
            return DS + 0;
        }
    }

    private String getDs(String id) {

        // id为空 默认为0
        if (StrUtil.isEmpty(id)) {
            return DS + 0;
        }

        int hash = id.hashCode();
        int index = hash % DS_COUNT;
        if (index < 0) {
            index = -index;
        }

        return DS + index;
    }
}
