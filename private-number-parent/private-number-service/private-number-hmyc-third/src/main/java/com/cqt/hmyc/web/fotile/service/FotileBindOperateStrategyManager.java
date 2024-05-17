package com.cqt.hmyc.web.fotile.service;

import com.alibaba.fastjson.JSON;
import com.cqt.hmyc.web.fotile.model.dto.BindOperationDTO;
import com.cqt.hmyc.web.fotile.model.vo.BindOperationResultVO;
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
 * @date 2022/7/22 16:24
 * 方太绑定操作策略执行入口
 */
@Component
@Slf4j
public class FotileBindOperateStrategyManager {

    private final Map<Integer, FotileBindOperateStrategy> STRATEGY_MAP = new ConcurrentHashMap<>();

    @Resource
    private List<FotileBindOperateStrategy> bindInfoQueryStrategyList;

    @PostConstruct
    public void initStrategy() {
        for (FotileBindOperateStrategy bindOperateStrategy : bindInfoQueryStrategyList) {
            STRATEGY_MAP.put(bindOperateStrategy.getOperationType(), bindOperateStrategy);
        }
    }

    public BindOperationResultVO operate(BindOperationDTO bindOperationDTO, String vccId) {
        Integer operation = bindOperationDTO.getOperation();
        if (log.isInfoEnabled()) {
            log.info("vccId: {}, operation: {}, 绑定操作入参: {}", vccId, operation, JSON.toJSONString(bindOperationDTO));
        }
        FotileBindOperateStrategy strategy = STRATEGY_MAP.get(operation);
        Optional<FotileBindOperateStrategy> strategyOptional = Optional.ofNullable(strategy);
        if (!strategyOptional.isPresent()) {
            return BindOperationResultVO.success("操作成功!");
        }

        return strategy.deal(bindOperationDTO, vccId);
    }


}
