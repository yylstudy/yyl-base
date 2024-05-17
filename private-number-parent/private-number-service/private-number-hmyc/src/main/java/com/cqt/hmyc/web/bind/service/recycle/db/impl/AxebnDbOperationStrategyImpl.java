package com.cqt.hmyc.web.bind.service.recycle.db.impl;

import com.cqt.common.constants.TableNameConstant;
import com.cqt.common.enums.BusinessTypeEnum;
import com.cqt.common.enums.OperateTypeEnum;
import com.cqt.hmyc.web.bind.mapper.axebn.PrivateBindInfoAxebnHisMapper;
import com.cqt.hmyc.web.bind.mapper.axebn.PrivateBindInfoAxebnMapper;
import com.cqt.hmyc.web.bind.service.axebn.AxebnBindConverter;
import com.cqt.hmyc.web.bind.service.recycle.db.DbOperationStrategy;
import com.cqt.model.bind.axebn.entity.PrivateBindInfoAxebn;
import com.cqt.model.bind.axebn.entity.PrivateBindInfoAxebnHis;
import com.cqt.model.bind.bo.MqBindInfoBO;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.api.hint.HintManager;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author linshiqiang
 * @date 2022/4/19 20:32
 * AXEBN此模式 废弃
 */
@Service
@Slf4j
@Deprecated
public class AxebnDbOperationStrategyImpl implements DbOperationStrategy {

    public static final String BUSINESS_TYPE = BusinessTypeEnum.AXEBN.name();

    @Resource
    private PrivateBindInfoAxebnMapper privateBindInfoAxebnMapper;

    @Resource
    private PrivateBindInfoAxebnHisMapper privateBindInfoAxebnHisMapper;

    @Resource
    private AxebnBindConverter axebnBindConverter;

    @Override
    public String getBusinessType() {
        return BUSINESS_TYPE;
    }

    @Override
    public void operate(MqBindInfoBO mqBindInfoBO) {
        try (HintManager hintManager = HintManager.getInstance()) {
            hintManager.addTableShardingValue(TableNameConstant.PRIVATE_BIND_INFO_AXEBN, mqBindInfoBO.getVccId());
            hintManager.addTableShardingValue(TableNameConstant.PRIVATE_BIND_INFO_AXEBN_HIS, mqBindInfoBO.getDate());
            saveAxebn(mqBindInfoBO);
        }
    }

    public void saveAxebn(MqBindInfoBO mqBindInfoBO) {
        PrivateBindInfoAxebn privateBindInfoAxebn = mqBindInfoBO.getPrivateBindInfoAxebn();
        if (OperateTypeEnum.INSERT.name().equals(mqBindInfoBO.getOperateType())) {
            privateBindInfoAxebn.setCreateTime(mqBindInfoBO.getDateTime());
            privateBindInfoAxebn.setUpdateTime(mqBindInfoBO.getDateTime());
            int insert = privateBindInfoAxebnMapper.insert(privateBindInfoAxebn);
            PrivateBindInfoAxebnHis bindInfoAxebnHis = axebnBindConverter.bindInfoAxebn2bindInfoAxebnHis(privateBindInfoAxebn);
            privateBindInfoAxebnHisMapper.insert(bindInfoAxebnHis);
            if (insert == 0) {
                log.error("axebn insert db fail: {}", privateBindInfoAxebn);
            }
        }
        if (OperateTypeEnum.UPDATE.name().equals(mqBindInfoBO.getOperateType())) {
            PrivateBindInfoAxebn bindInfoAxebn = new PrivateBindInfoAxebn();
            bindInfoAxebn.setBindId(privateBindInfoAxebn.getBindId());
            bindInfoAxebn.setExpiration(privateBindInfoAxebn.getExpiration());
            bindInfoAxebn.setExpireTime(privateBindInfoAxebn.getExpireTime());
            bindInfoAxebn.setUpdateTime(mqBindInfoBO.getDateTime());
            int update = privateBindInfoAxebnMapper.updateById(bindInfoAxebn);
            if (update == 0) {
                log.error("axebn update db fail: {}", privateBindInfoAxebn);
            }
        }
        if (OperateTypeEnum.DELETE.name().equals(mqBindInfoBO.getOperateType())) {
            int delete = privateBindInfoAxebnMapper.deleteById(privateBindInfoAxebn.getBindId());
            if (delete == 0) {
                log.error("axebn delete db fail: {}", privateBindInfoAxebn.getBindId());
            }
        }
    }

}
