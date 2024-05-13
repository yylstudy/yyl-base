package com.yyl;

import cn.hutool.core.date.DateUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.sharding.api.sharding.complex.ComplexKeysShardingAlgorithm;
import org.apache.shardingsphere.sharding.api.sharding.complex.ComplexKeysShardingValue;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author yang.yonglian
 * @version 1.0.0
 * @Description 复合分片算法
 * @createTime 2022/12/8 9:42
 */
@Slf4j
public class CustomComplexKeysShardingAlgorithm implements ComplexKeysShardingAlgorithm {
    @Override
    public Collection<String> doSharding(Collection availableTargetNames, ComplexKeysShardingValue shardingValue) {
        log.info("availableTargetNames:{}",availableTargetNames);
        Map<String, Collection<Object>> map = shardingValue.getColumnNameAndShardingValuesMap();
        Long id = (Long)map.get("id").stream().collect(Collectors.toList()).get(0);
        String createTimeStr = (String)map.get("create_time").stream().collect(Collectors.toList()).get(0);
        Date createTime = DateUtil.parse(createTimeStr);
        Date shardingTime = DateUtil.parse("2022-12-01");
        Collection<String> shardingTables;
        if(createTime.before(shardingTime)){
            shardingTables = Collections.singleton("test_"+DateUtil.format(createTime,"yyyyMM"));
        }else{
            long ss = id%3;
            shardingTables = Collections.singleton("test_"+DateUtil.format(createTime,"yyyyMM")+"_"+ss);
        }
        for(String shardingTable:shardingTables){
            log.info("shardingTable:{}",shardingTable);
        }
        return shardingTables;
    }

    @Override
    public Properties getProps() {
        return null;
    }

    @Override
    public void init(Properties props) {

    }
}
