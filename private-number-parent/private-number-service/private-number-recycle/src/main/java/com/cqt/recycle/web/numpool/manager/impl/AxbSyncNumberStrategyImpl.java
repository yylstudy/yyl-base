package com.cqt.recycle.web.numpool.manager.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cqt.common.constants.TableNameConstant;
import com.cqt.common.enums.AxbPoolTypeEnum;
import com.cqt.common.enums.BusinessTypeEnum;
import com.cqt.common.enums.InitFlagEnum;
import com.cqt.common.enums.OperateTypeEnum;
import com.cqt.common.util.PrivateCacheUtil;
import com.cqt.model.bind.axb.entity.PrivateBindAxbInitUserTelPool;
import com.cqt.model.numpool.dto.NumberChangeSyncDTO;
import com.cqt.recycle.web.numpool.manager.SyncNumberStrategy;
import com.cqt.recycle.web.numpool.mapper.PrivateBindAxbInitUserTelPoolMapper;
import com.cqt.redis.util.RedissonUtil;
import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.api.hint.HintManager;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author linshiqiang
 * @date 2022/5/26 11:14
 * AXB模式 redis号码池同步策略实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AxbSyncNumberStrategyImpl implements SyncNumberStrategy {

    public static final String BUSINESS_TYPE = BusinessTypeEnum.AXB.name();

    private final PrivateBindAxbInitUserTelPoolMapper bindAxbInitUserTelPoolMapper;

    private final ThreadPoolTaskExecutor recycleExecutor;

    private final RedissonUtil redissonUtil;

    @Override
    public void sync(NumberChangeSyncDTO numberChangeSyncDTO) {
        String operationType = numberChangeSyncDTO.getOperationType();
        String vccId = numberChangeSyncDTO.getVccId();
        Map<String, List<String>> masterNumberMap = numberChangeSyncDTO.getMasterNumberMap();
        Map<String, List<String>> slaveNumberMap = numberChangeSyncDTO.getSlaveNumberMap();
        try (HintManager hintManager = HintManager.getInstance()) {
            hintManager.addTableShardingValue(TableNameConstant.PRIVATE_BIND_AXB_INIT_USER_TEL_POOL, vccId);

            if (OperateTypeEnum.DELETE.name().equals(operationType)) {
                // 删除真实号码可用号码池
                if (CollUtil.isNotEmpty(masterNumberMap)) {
                    masterNumberMap.forEach((areaCode, telList) -> operatePool(vccId, areaCode, telList, operationType, AxbPoolTypeEnum.MASTER));
                }
                if (CollUtil.isNotEmpty(slaveNumberMap)) {
                    slaveNumberMap.forEach((areaCode, telList) -> operatePool(vccId, areaCode, telList, operationType, AxbPoolTypeEnum.SLAVE));
                }
            }

            if (OperateTypeEnum.INSERT.name().equals(operationType)) {
                // 新增真实号码可用号码池
                if (CollUtil.isNotEmpty(masterNumberMap)) {

                    masterNumberMap.forEach((areaCode, telList) -> operatePool(vccId, areaCode, telList, operationType, AxbPoolTypeEnum.MASTER));
                }
                if (CollUtil.isNotEmpty(slaveNumberMap)) {
                    slaveNumberMap.forEach((areaCode, telList) -> operatePool(vccId, areaCode, telList, operationType, AxbPoolTypeEnum.SLAVE));
                }
            }
        }
    }

    /**
     * 添加/删除号码入口
     *
     * @param vccId         企业id
     * @param areaCode      地市编码
     * @param telList       号码列表
     * @param operationType 操作类型
     */
    private void operatePool(String vccId, String areaCode, List<String> telList, String operationType, AxbPoolTypeEnum axbPoolTypeEnum) {
        List<PrivateBindAxbInitUserTelPool> realTelList = getRealTelList(areaCode);
        log.info("AXB {}可用号码池, vccId: {}, areaCode: {}, 真实号码数量: {}, X号码数量: {}", operationType, vccId, areaCode, realTelList.size(), telList.size());
        if (CollUtil.isEmpty(realTelList)) {
            return;
        }
        // 根据initFlag分组
        Map<Integer, List<PrivateBindAxbInitUserTelPool>> initFlagMap = realTelList.stream()
                .filter(item -> ObjectUtil.isNotEmpty(item.getInitFlag()))
                .collect(Collectors.groupingBy(PrivateBindAxbInitUserTelPool::getInitFlag));
        // 使用主池和备池
        List<PrivateBindAxbInitUserTelPool> slaveTelList = initFlagMap.get(2);

        // 106号码
        List<PrivateBindAxbInitUserTelPool> telPoolList = initFlagMap.get(3);
        if (CollUtil.isNotEmpty(telPoolList)) {
            log.info("106号码池, vccId: {}, areaCode: {}, 106号码数量: {}, X号码数量: {}", vccId, areaCode, telPoolList.size(), telList.size());
            List<String> keyList = telPoolList.stream()
                    .map(item -> PrivateCacheUtil.getIndustrySmsTelUsablePoolKey(vccId, areaCode, item.getTel()))
                    .collect(Collectors.toList());
            operateTelListToRedis(keyList, telList, operationType);
        }


        // 号码是在主池, 所有键都要加
        if (axbPoolTypeEnum.equals(AxbPoolTypeEnum.MASTER)) {
            List<String> masterKeyList = realTelList.stream()
                    .filter(item -> InitFlagEnum.FIRST_INIT.getCode().equals(item.getInitFlag().toString())
                            || InitFlagEnum.SECOND_INIT.getCode().equals(item.getInitFlag().toString()))
                    .map(item -> PrivateCacheUtil.getUsablePoolKey(vccId, BUSINESS_TYPE, areaCode, item.getTel()))
                    .collect(Collectors.toList());
            operateTelListToRedis(masterKeyList, telList, operationType);
            log.info("AXB {}可用号码池, vccId: {}, areaCode: {}, 使用主池键数量: {}, 添加X号码数量: {}", operationType, vccId, areaCode, realTelList.size(), telList.size());
        }
        // 号码是在备池, 只有备池的键要加
        if (axbPoolTypeEnum.equals(AxbPoolTypeEnum.SLAVE)) {
            if (CollUtil.isNotEmpty(slaveTelList)) {
                List<String> allKeyList = slaveTelList.stream()
                        .map(item -> PrivateCacheUtil.getUsablePoolKey(vccId, BUSINESS_TYPE, areaCode, item.getTel()))
                        .collect(Collectors.toList());
                operateTelListToRedis(allKeyList, telList, operationType);
                log.info("AXB {}可用号码池, vccId: {}, areaCode: {}, 使用备池键数量: {}, 添加X号码数量: {}", operationType, vccId, areaCode, slaveTelList.size(), telList.size());
            }
        }
    }

    /**
     * 批量添加/删除号码到redis的可用号码池key
     *
     * @param keyList       可用号码池key
     * @param telList       号码列表
     * @param operationType 操作类型
     */
    private void operateTelListToRedis(List<String> keyList, List<String> telList, String operationType) {

        // list 分组, size多少合适?
        List<List<String>> partition = Lists.partition(keyList, 10000);
        for (List<String> list : partition) {
            recycleExecutor.execute(() -> {
                if (OperateTypeEnum.INSERT.name().equals(operationType)) {
                    redissonUtil.setSetsBatch(list, telList);
                    log.info("AXB 批量添加号码到redis, key: {}, telList: {}", list.get(0), telList);
                }
                if (OperateTypeEnum.DELETE.name().equals(operationType)) {
                    redissonUtil.removeSetsBatch(list, telList);
                    log.info("AXB 删除号码, key: {}, telList: {}", list.get(0), telList);
                }
            });
        }
    }


    /**
     * AXB 初始化真实号码
     *
     * @param areaCode 地市编码
     * @return 号码列表
     */
    private List<PrivateBindAxbInitUserTelPool> getRealTelList(String areaCode) {
        LambdaQueryWrapper<PrivateBindAxbInitUserTelPool> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.select(PrivateBindAxbInitUserTelPool::getTel, PrivateBindAxbInitUserTelPool::getInitFlag);
        queryWrapper.eq(PrivateBindAxbInitUserTelPool::getAreaCode, areaCode);
        return bindAxbInitUserTelPoolMapper.selectList(queryWrapper);
    }

    @Override
    public String getBusinessType() {
        return BUSINESS_TYPE;
    }
}
