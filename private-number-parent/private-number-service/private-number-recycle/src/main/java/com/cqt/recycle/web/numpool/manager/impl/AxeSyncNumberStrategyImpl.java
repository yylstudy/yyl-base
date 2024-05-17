package com.cqt.recycle.web.numpool.manager.impl;

import cn.hutool.core.collection.CollUtil;
import com.cqt.common.enums.BusinessTypeEnum;
import com.cqt.common.enums.OperateTypeEnum;
import com.cqt.common.util.PrivateCacheUtil;
import com.cqt.model.numpool.dto.NumberChangeSyncDTO;
import com.cqt.recycle.web.numpool.cache.LocalCacheService;
import com.cqt.recycle.web.numpool.manager.SyncNumberStrategy;
import com.cqt.redis.util.RedissonUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author linshiqiang
 * @date 2022/5/26 11:14
 * AXE模式 redis号码池同步策略实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AxeSyncNumberStrategyImpl implements SyncNumberStrategy {

    public static final String BUSINESS_TYPE = BusinessTypeEnum.AXE.name();

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
                    for (String telX : list) {
                        String usableExtPoolKey = PrivateCacheUtil.getUsableExtPoolListKey(vccId, poolType, areaCode, telX);
                        redissonUtil.delDeque(usableExtPoolKey);
                    }
                });
            }

            if (CollUtil.isNotEmpty(slaveNumberMap)) {
                slaveNumberMap.forEach((areaCode, list) -> {
                });
            }

        }

        // TODO 初始化分机号, 分机号个数 需要查询企业业务配置
        List<String> extPoolList = LocalCacheService.X_EXT_POOL_LIST;
        Collections.shuffle(extPoolList);
        if (OperateTypeEnum.INSERT.name().equals(numberChangeSyncDTO.getOperationType())) {
            if (CollUtil.isNotEmpty(masterNumberMap)) {
                masterNumberMap.forEach((areaCode, list) -> {

                    for (String telX : list) {
                        String usableExtPoolKey = PrivateCacheUtil.getUsableExtPoolListKey(vccId, poolType, areaCode, telX);
                        // 如果键存在, 则不添加
                        if (redissonUtil.isExistDeque(usableExtPoolKey)) {
                            continue;
                        }
                        Collections.shuffle(extPoolList);
                        boolean set = redissonUtil.setDequeString(usableExtPoolKey, extPoolList);
                        log.info("AXE 新增分机号池子: {}, result: {}", usableExtPoolKey, set);
                    }

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
