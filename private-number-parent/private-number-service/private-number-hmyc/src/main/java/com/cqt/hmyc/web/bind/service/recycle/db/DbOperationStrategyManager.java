package com.cqt.hmyc.web.bind.service.recycle.db;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.cqt.model.bind.bo.MqBindInfoBO;
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
 * 数据库操作策略执行器
 */
@Slf4j
@Service
public class DbOperationStrategyManager {

    private final Map<String, DbOperationStrategy> STRATEGY_MAP = new ConcurrentHashMap<>();

    @Resource
    private List<DbOperationStrategy> dbOperationStrategyList;

    @PostConstruct
    public void initStrategy() {
        for (DbOperationStrategy strategy : dbOperationStrategyList) {
            STRATEGY_MAP.put(strategy.getBusinessType(), strategy);
        }
    }

    public void operate(MqBindInfoBO mqBindInfoBO) {
        DateTime dateTime = DateUtil.date();
        mqBindInfoBO.setDateTime(dateTime);
        String date = DateUtil.format(dateTime, "yyyyMMdd");
        mqBindInfoBO.setDate(date);
        DbOperationStrategy strategy = STRATEGY_MAP.get(mqBindInfoBO.getNumType());
        Optional<DbOperationStrategy> strategyOptional = Optional.ofNullable(strategy);
        strategyOptional.ifPresent(recycleNumberStrategy -> recycleNumberStrategy.operate(mqBindInfoBO));
    }

}
