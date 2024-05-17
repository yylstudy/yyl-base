package com.cqt.hmyc.web.numpool.manager.impl;

import cn.hutool.core.collection.CollUtil;
import com.cqt.common.enums.BusinessTypeEnum;
import com.cqt.common.enums.OperateTypeEnum;
import com.cqt.hmyc.web.cache.NumberPoolAxCache;
import com.cqt.hmyc.web.numpool.manager.SyncNumberStrategy;
import com.cqt.model.common.Result;
import com.cqt.model.numpool.dto.NumberChangeSyncDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * @author linshiqiang
 * @date 2022/5/26 11:14
 * AX模式 号码池同步策略实现
 */
@Slf4j
@Service
public class AxSyncNumberStrategyImpl implements SyncNumberStrategy {

    public static final String BUSINESS_TYPE = BusinessTypeEnum.AX.name();

    @Override
    public Result sync(NumberChangeSyncDTO numberChangeSyncDTO) {

        String vccId = numberChangeSyncDTO.getVccId();
        Map<String, List<String>> masterNumberMap = numberChangeSyncDTO.getMasterNumberMap();
        Map<String, List<String>> slaveNumberMap = numberChangeSyncDTO.getSlaveNumberMap();

        if (OperateTypeEnum.DELETE.name().equals(numberChangeSyncDTO.getOperationType())) {
            if (CollUtil.isNotEmpty(masterNumberMap)) {

                masterNumberMap.forEach((areaCode, list) -> NumberPoolAxCache.removeAll(vccId, areaCode, list));
            }
            if (CollUtil.isNotEmpty(slaveNumberMap)) {

                slaveNumberMap.forEach((areaCode, list) -> NumberPoolAxCache.removeAll(vccId, areaCode, list));
            }
        }

        if (OperateTypeEnum.INSERT.name().equals(numberChangeSyncDTO.getOperationType())) {
            if (CollUtil.isNotEmpty(masterNumberMap)) {

                masterNumberMap.forEach((areaCode, list) -> NumberPoolAxCache.addPool(vccId, areaCode, list));
            }
            if (CollUtil.isNotEmpty(slaveNumberMap)) {

                slaveNumberMap.forEach((areaCode, list) -> NumberPoolAxCache.addPool(vccId, areaCode, list));
            }
        }

        return Result.ok();
    }

    @Override
    public String getBusinessType() {
        return BUSINESS_TYPE;
    }
}
