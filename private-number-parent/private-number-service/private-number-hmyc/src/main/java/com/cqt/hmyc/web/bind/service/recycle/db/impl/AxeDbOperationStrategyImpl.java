package com.cqt.hmyc.web.bind.service.recycle.db.impl;

import cn.hutool.core.util.StrUtil;
import com.cqt.common.constants.TableNameConstant;
import com.cqt.common.enums.BusinessTypeEnum;
import com.cqt.common.enums.OperateTypeEnum;
import com.cqt.common.util.BindIdUtil;
import com.cqt.hmyc.web.bind.event.AxeBindStatsEvent;
import com.cqt.hmyc.web.bind.mapper.axe.PrivateBindInfoAxeHisMapper;
import com.cqt.hmyc.web.bind.mapper.axe.PrivateBindInfoAxeMapper;
import com.cqt.hmyc.web.bind.service.axe.AxeBindConverter;
import com.cqt.hmyc.web.bind.service.recycle.db.DbOperationStrategy;
import com.cqt.model.bind.axe.entity.PrivateBindInfoAxe;
import com.cqt.model.bind.axe.entity.PrivateBindInfoAxeHis;
import com.cqt.model.bind.bo.MqBindInfoBO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.api.hint.HintManager;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

/**
 * @author linshiqiang
 * @date 2022/4/19 20:32
 * AXE 数据库操作策略
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AxeDbOperationStrategyImpl implements DbOperationStrategy {

    public static final String BUSINESS_TYPE = BusinessTypeEnum.AXE.name();

    private final PrivateBindInfoAxeMapper privateBindInfoAxeMapper;

    private final PrivateBindInfoAxeHisMapper privateBindInfoAxeHisMapper;

    private final AxeBindConverter axeBindConverter;

    private final ApplicationContext applicationContext;

    @Override
    public String getBusinessType() {
        return BUSINESS_TYPE;
    }

    @Override
    public void operate(MqBindInfoBO mqBindInfoBO) {
        try (HintManager hintManager = HintManager.getInstance()) {
            String numberHash = BindIdUtil.getHash(mqBindInfoBO.getPrivateBindInfoAxe().getTelX());
            String sharingKey = mqBindInfoBO.getVccId() + StrUtil.AT + numberHash;
            hintManager.addDatabaseShardingValue(TableNameConstant.PRIVATE_BIND_INFO_AXE, numberHash);
            hintManager.addTableShardingValue(TableNameConstant.PRIVATE_BIND_INFO_AXE, sharingKey);
            hintManager.addTableShardingValue(TableNameConstant.PRIVATE_BIND_INFO_AXE_HIS, mqBindInfoBO.getDate());
            saveAxe(mqBindInfoBO);
        }
    }

    public void saveAxe(MqBindInfoBO mqBindInfoBO) {
        PrivateBindInfoAxe privateBindInfoAxe = mqBindInfoBO.getPrivateBindInfoAxe();
        if (OperateTypeEnum.INSERT.name().equals(mqBindInfoBO.getOperateType())) {
            privateBindInfoAxe.setCreateTime(mqBindInfoBO.getDateTime());
            privateBindInfoAxe.setUpdateTime(mqBindInfoBO.getDateTime());
            int insert = privateBindInfoAxeMapper.insert(privateBindInfoAxe);
            PrivateBindInfoAxeHis bindInfoAxeHis = axeBindConverter.bindInfoAxe2BindInfoAxeHis(privateBindInfoAxe);
            privateBindInfoAxeHisMapper.insert(bindInfoAxeHis);
            log.info("axe bindId: {}, insert db finish: {}", privateBindInfoAxe.getBindId(), insert);
            axeStatsEvent(mqBindInfoBO);
            return;
        }
        if (OperateTypeEnum.UPDATE.name().equals(mqBindInfoBO.getOperateType())) {
            PrivateBindInfoAxe bindInfoAxe = new PrivateBindInfoAxe();
            bindInfoAxe.setBindId(privateBindInfoAxe.getBindId());
            bindInfoAxe.setTelB(privateBindInfoAxe.getTelB());
            bindInfoAxe.setExpiration(privateBindInfoAxe.getExpiration());
            bindInfoAxe.setExpireTime(privateBindInfoAxe.getExpireTime());
            bindInfoAxe.setUpdateTime(mqBindInfoBO.getDateTime());
            int update = privateBindInfoAxeMapper.updateById(bindInfoAxe);
            log.info("axe bindId: {}, update db finish: {}", privateBindInfoAxe.getBindId(), update);
            return;
        }
        if (OperateTypeEnum.DELETE.name().equals(mqBindInfoBO.getOperateType())) {
            int delete = privateBindInfoAxeMapper.deleteById(privateBindInfoAxe.getBindId());
            log.info("axe bindId: {}, delete db finish: {}", privateBindInfoAxe.getBindId(), delete);
            axeStatsEvent(mqBindInfoBO);
        }
    }

    private void axeStatsEvent(MqBindInfoBO mqBindInfoBO) {
        try {
            applicationContext.publishEvent(new AxeBindStatsEvent(this,
                    mqBindInfoBO.getVccId(),
                    mqBindInfoBO.getCityCode(),
                    OperateTypeEnum.valueOf(mqBindInfoBO.getOperateType())));
        } catch (Exception e) {
            log.error("axeStatsEvent e: ", e);
        }
    }

}
