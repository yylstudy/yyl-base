package com.cqt.recycle.config.sharding;

import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.google.common.collect.Range;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.api.sharding.standard.RangeShardingAlgorithm;
import org.apache.shardingsphere.api.sharding.standard.RangeShardingValue;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author linshiqiang
 * @date 2021/12/6 15:01
 */
@Slf4j
public class DateRangeShardingAlgorithm implements RangeShardingAlgorithm<Date> {

    @Override
    public Collection<String> doSharding(Collection<String> availableTargetNames, RangeShardingValue<Date> rangeShardingValue) {
        log.debug("availableTargetNames : {}", availableTargetNames);
        log.debug("range: {}", rangeShardingValue.toString());
        Range<Date> valueRange = rangeShardingValue.getValueRange();
        Date lowerDate = valueRange.lowerEndpoint();
        Date upperDate = valueRange.upperEndpoint();
        Optional<String> stringOptional = availableTargetNames.stream().findFirst();
        if (!stringOptional.isPresent()) {
            return Collections.emptyList();
        }
        String tableName = stringOptional.get();
        List<DateTime> dateTimes = DateUtil.rangeToList(lowerDate, upperDate, DateField.DAY_OF_YEAR);
        List<String> objectList = dateTimes.stream().map(item -> DateUtil.format(item, "yyyyMMdd")).collect(Collectors.toList());
        List<String> tableList = new ArrayList<>();
        for (String date : objectList) {
            tableList.add(tableName + "_" + date);
        }

        log.debug("match tableNames: {}",  tableList);
        return tableList;
    }
}