package com.cqt.hmyc.web.bind.service.recycle.recycle;

import com.cqt.model.bind.dto.BindRecycleDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author linshiqiang
 * @date 2022/4/19 20:10
 * 号码回收处理策略执行器
 */
@Slf4j
@Service
public class RecycleNumberStrategyManager {

    private final Map<String, RecycleNumberStrategy> STRATEGY_MAP = new ConcurrentHashMap<>();

    @Resource
    private List<RecycleNumberStrategy> recycleNumberStrategyList;

    @PostConstruct
    public void initStrategy() {
        for (RecycleNumberStrategy strategy : recycleNumberStrategyList) {
            STRATEGY_MAP.put(strategy.getBusinessType(), strategy);
        }
    }

    public void recycle(BindRecycleDTO bindRecycleDTO) {

        RecycleNumberStrategy strategy = STRATEGY_MAP.get(bindRecycleDTO.getNumType());
        Optional<RecycleNumberStrategy> strategyOptional = Optional.ofNullable(strategy);
        strategyOptional.ifPresent(recycleNumberStrategy -> recycleNumberStrategy.recycle(bindRecycleDTO));
    }

}
