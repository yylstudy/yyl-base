package com.cqt.hmyc.web.numpool.manager;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.fastjson.JSON;
import com.cqt.common.enums.OperateTypeEnum;
import com.cqt.hmyc.config.balancer.RoundRobinLoadBalancer;
import com.cqt.hmyc.web.cache.NumberTypeCache;
import com.cqt.model.common.Result;
import com.cqt.model.numpool.dto.NumberChangeSyncDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author linshiqiang
 * @date 2022/5/26 11:16
 */
@Component
@Slf4j
public class SyncNumberStrategyManager {

    private final Map<String, SyncNumberStrategy> STRATEGY_MAP = new ConcurrentHashMap<>();

    @Resource
    private List<SyncNumberStrategy> syncNumberStrategyList;

    @PostConstruct
    public void initStrategy() {
        for (SyncNumberStrategy strategy : syncNumberStrategyList) {
            STRATEGY_MAP.put(strategy.getBusinessType(), strategy);
        }
    }

    public Result sync(NumberChangeSyncDTO numberChangeSyncDTO) {
        RoundRobinLoadBalancer.clear();
        if (log.isInfoEnabled()) {
            log.info("同步号码池信息: {}", JSON.toJSONString(numberChangeSyncDTO));
        }
        SyncNumberStrategy strategy = STRATEGY_MAP.get(numberChangeSyncDTO.getBusinessType());
        Optional<SyncNumberStrategy> strategyOptional = Optional.ofNullable(strategy);
        if (!strategyOptional.isPresent()) {
            return Result.fail(-1, "businessType 未适配!");
        }

        // 设置业务模式
        Map<String, List<String>> masterNumberMap = numberChangeSyncDTO.getMasterNumberMap();
        deal(numberChangeSyncDTO, masterNumberMap);
        Map<String, List<String>> slaveNumberMap = numberChangeSyncDTO.getSlaveNumberMap();
        deal(numberChangeSyncDTO, slaveNumberMap);

        return strategy.sync(numberChangeSyncDTO);
    }

    private static void deal(NumberChangeSyncDTO numberChangeSyncDTO, Map<String, List<String>> slaveNumberMap) {
        if (CollUtil.isNotEmpty(slaveNumberMap)) {
            for (List<String> list : slaveNumberMap.values()) {
                if (CollUtil.isNotEmpty(list)) {
                    for (String number : list) {
                        if (OperateTypeEnum.INSERT.name().equals(numberChangeSyncDTO.getOperationType())) {
                            NumberTypeCache.put(number, numberChangeSyncDTO.getBusinessType());
                        }
                    }
                }
            }
        }
    }

}
