package com.cqt.hmyc.web.bind.service.recycle.db.impl;

import com.cqt.common.constants.TableNameConstant;
import com.cqt.common.enums.BusinessTypeEnum;
import com.cqt.common.enums.OperateTypeEnum;
import com.cqt.hmyc.web.bind.mapper.axb.PrivateBindInfoAxbHisMapper;
import com.cqt.hmyc.web.bind.mapper.axb.PrivateBindInfoAxbMapper;
import com.cqt.hmyc.web.bind.service.axb.AxbBindConverter;
import com.cqt.hmyc.web.bind.service.recycle.db.DbOperationStrategy;
import com.cqt.model.bind.axb.entity.PrivateBindInfoAxb;
import com.cqt.model.bind.axb.entity.PrivateBindInfoAxbHis;
import com.cqt.model.bind.bo.MqBindInfoBO;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.api.hint.HintManager;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author linshiqiang
 * @date 2022/4/19 20:32
 */
@Service
@Slf4j
public class AxbDbOperationStrategyImpl implements DbOperationStrategy {

    public static final String BUSINESS_TYPE = BusinessTypeEnum.AXB.name();

    @Resource
    private PrivateBindInfoAxbMapper privateBindInfoAxbMapper;

    @Resource
    private PrivateBindInfoAxbHisMapper privateBindInfoAxbHisMapper;

    @Resource
    private AxbBindConverter axbBindConverter;

    @Override
    public String getBusinessType() {
        return BUSINESS_TYPE;
    }

    @Override
    public void operate(MqBindInfoBO mqBindInfoBO) {
        try (HintManager hintManager = HintManager.getInstance()) {
            hintManager.addTableShardingValue(TableNameConstant.PRIVATE_BIND_INFO_AXB, mqBindInfoBO.getVccId());
            hintManager.addTableShardingValue(TableNameConstant.PRIVATE_BIND_INFO_AXB_HIS, mqBindInfoBO.getDate());
            saveAxb(mqBindInfoBO);
        }
    }

    private void saveAxb(MqBindInfoBO mqBindInfoBO) {
        PrivateBindInfoAxb privateBindInfoAxb = mqBindInfoBO.getPrivateBindInfoAxb();
        if (OperateTypeEnum.INSERT.name().equals(mqBindInfoBO.getOperateType())) {
            privateBindInfoAxb.setCreateTime(mqBindInfoBO.getDateTime());
            privateBindInfoAxb.setUpdateTime(mqBindInfoBO.getDateTime());
            int insert = privateBindInfoAxbMapper.insert(privateBindInfoAxb);
            PrivateBindInfoAxbHis bindInfoAxbHis = axbBindConverter.bindInfoAxb2BindInfoAxbHis(privateBindInfoAxb);
            privateBindInfoAxbHisMapper.insert(bindInfoAxbHis);
            log.info("axb bindId: {}, insert db finish: {}", privateBindInfoAxb.getBindId(), insert);
            return;
        }
        if (OperateTypeEnum.UPDATE.name().equals(mqBindInfoBO.getOperateType())) {
            PrivateBindInfoAxb bindInfoAxb = new PrivateBindInfoAxb();
            bindInfoAxb.setBindId(privateBindInfoAxb.getBindId());
            bindInfoAxb.setExpiration(privateBindInfoAxb.getExpiration());
            bindInfoAxb.setExpireTime(privateBindInfoAxb.getExpireTime());
            bindInfoAxb.setUpdateTime(mqBindInfoBO.getDateTime());
            bindInfoAxb.setTelA(privateBindInfoAxb.getTelA());
            bindInfoAxb.setTelB(privateBindInfoAxb.getTelB());
            int update = privateBindInfoAxbMapper.updateById(bindInfoAxb);
            log.info("axb bindId: {}, update db finish: {}", privateBindInfoAxb.getBindId(), update);
            return;
        }
        if (OperateTypeEnum.DELETE.name().equals(mqBindInfoBO.getOperateType())) {
            int delete = privateBindInfoAxbMapper.deleteById(privateBindInfoAxb.getBindId());
            log.info("axb bindId: {}, delete db finish: {}", privateBindInfoAxb.getBindId(), delete);
        }
    }
}
