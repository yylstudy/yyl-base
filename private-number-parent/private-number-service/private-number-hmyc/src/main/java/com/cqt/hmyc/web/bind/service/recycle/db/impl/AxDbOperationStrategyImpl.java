package com.cqt.hmyc.web.bind.service.recycle.db.impl;

import com.cqt.common.constants.TableNameConstant;
import com.cqt.common.enums.BusinessTypeEnum;
import com.cqt.common.enums.OperateTypeEnum;
import com.cqt.hmyc.web.bind.mapper.ax.PrivateBindInfoAxHisMapper;
import com.cqt.hmyc.web.bind.mapper.ax.PrivateBindInfoAxMapper;
import com.cqt.hmyc.web.bind.service.ax.AxBindConverter;
import com.cqt.hmyc.web.bind.service.recycle.db.DbOperationStrategy;
import com.cqt.model.bind.ax.entity.PrivateBindInfoAx;
import com.cqt.model.bind.ax.entity.PrivateBindInfoAxHis;
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
public class AxDbOperationStrategyImpl implements DbOperationStrategy {

    public static final String BUSINESS_TYPE = BusinessTypeEnum.AX.name();

    @Resource
    private PrivateBindInfoAxMapper privateBindInfoAxMapper;

    @Resource
    private PrivateBindInfoAxHisMapper privateBindInfoAxHisMapper;

    @Resource
    private AxBindConverter axBindConverter;

    @Override
    public String getBusinessType() {
        return BUSINESS_TYPE;
    }

    @Override
    public void operate(MqBindInfoBO mqBindInfoBO) {
        try (HintManager hintManager = HintManager.getInstance()) {
            hintManager.addTableShardingValue(TableNameConstant.PRIVATE_BIND_INFO_AX, mqBindInfoBO.getVccId());
            hintManager.addTableShardingValue(TableNameConstant.PRIVATE_BIND_INFO_AX_HIS, mqBindInfoBO.getDate());
            saveAx(mqBindInfoBO);
        }
    }

    private void saveAx(MqBindInfoBO mqBindInfoBO) {
        PrivateBindInfoAx privateBindInfoAx = mqBindInfoBO.getPrivateBindInfoAx();
        if (OperateTypeEnum.INSERT.name().equals(mqBindInfoBO.getOperateType())) {
            privateBindInfoAx.setCreateTime(mqBindInfoBO.getDateTime());
            privateBindInfoAx.setUpdateTime(mqBindInfoBO.getDateTime());
            int insert = privateBindInfoAxMapper.insert(privateBindInfoAx);
            PrivateBindInfoAxHis bindInfoAxHis = axBindConverter.bindInfoAx2BindInfoAxHis(privateBindInfoAx);
            privateBindInfoAxHisMapper.insert(bindInfoAxHis);
            log.info("ax bindId: {}, insert db finish: {}", privateBindInfoAx.getBindId(), insert);
            return;
        }
        if (OperateTypeEnum.UPDATE.name().equals(mqBindInfoBO.getOperateType())) {
            PrivateBindInfoAx bindInfoAx = new PrivateBindInfoAx();
            bindInfoAx.setBindId(privateBindInfoAx.getBindId());
            bindInfoAx.setExpiration(privateBindInfoAx.getExpiration());
            bindInfoAx.setExpireTime(privateBindInfoAx.getExpireTime());
            bindInfoAx.setUpdateTime(mqBindInfoBO.getDateTime());
            bindInfoAx.setTelB(privateBindInfoAx.getTelB());
            int update = privateBindInfoAxMapper.updateById(bindInfoAx);
            log.info("ax bindId: {}, update db finish: {}", privateBindInfoAx.getBindId(), update);
            return;
        }
        if (OperateTypeEnum.DELETE.name().equals(mqBindInfoBO.getOperateType())) {
            int delete = privateBindInfoAxMapper.deleteById(privateBindInfoAx.getBindId());
            log.info("ax bindId: {}, delete db finish: {}", privateBindInfoAx.getBindId(), delete);
        }
    }
}
