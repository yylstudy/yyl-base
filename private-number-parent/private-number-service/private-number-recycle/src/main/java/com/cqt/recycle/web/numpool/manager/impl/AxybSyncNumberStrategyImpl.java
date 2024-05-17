package com.cqt.recycle.web.numpool.manager.impl;

import cn.hutool.core.collection.CollUtil;
import com.cqt.common.enums.BusinessTypeEnum;
import com.cqt.common.enums.OperateTypeEnum;
import com.cqt.model.numpool.dto.NumberChangeSyncDTO;
import com.cqt.recycle.web.numpool.manager.SyncNumberStrategy;
import com.cqt.redis.util.RedissonUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * @author linshiqiang
 * @date 2022/5/26 11:14
 * AXYB模式 redis号码池同步策略实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AxybSyncNumberStrategyImpl implements SyncNumberStrategy {

    public static final String BUSINESS_TYPE = BusinessTypeEnum.AXYB.name();

    private final RedissonUtil redissonUtil;

    @Override
    public void sync(NumberChangeSyncDTO numberChangeSyncDTO) {

        String poolType = numberChangeSyncDTO.getPoolType();
        String vccId = numberChangeSyncDTO.getVccId();
        Map<String, List<String>> masterNumberMap = numberChangeSyncDTO.getMasterNumberMap();
        Map<String, List<String>> slaveNumberMap = numberChangeSyncDTO.getSlaveNumberMap();

        if (OperateTypeEnum.DELETE.name().equals(numberChangeSyncDTO.getOperationType())) {
            if (CollUtil.isNotEmpty(masterNumberMap)) {
                masterNumberMap.forEach((areaCode, list) -> {
                });
            }

            if (CollUtil.isNotEmpty(slaveNumberMap)) {
                slaveNumberMap.forEach((areaCode, list) -> {
                });
            }

        }

        if (OperateTypeEnum.INSERT.name().equals(numberChangeSyncDTO.getOperationType())) {
            if (CollUtil.isNotEmpty(masterNumberMap)) {
                masterNumberMap.forEach((areaCode, list) -> {
                });
            }

            if (CollUtil.isNotEmpty(slaveNumberMap)) {
                slaveNumberMap.forEach((areaCode, list) -> {
                });
            }

        }

    }

    @Override
    public String getBusinessType() {
        return BUSINESS_TYPE;
    }
}
