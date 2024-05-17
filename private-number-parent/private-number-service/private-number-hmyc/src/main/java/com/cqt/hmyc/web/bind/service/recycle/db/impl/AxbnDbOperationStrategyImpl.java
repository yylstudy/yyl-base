package com.cqt.hmyc.web.bind.service.recycle.db.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cqt.common.constants.TableNameConstant;
import com.cqt.common.enums.BusinessTypeEnum;
import com.cqt.common.enums.ModeEnum;
import com.cqt.common.enums.OperateTypeEnum;
import com.cqt.common.util.BindIdUtil;
import com.cqt.hmyc.web.bind.mapper.axbn.PrivateBindAxbnRealTelMapper;
import com.cqt.hmyc.web.bind.mapper.axbn.PrivateBindInfoAxbnHisMapper;
import com.cqt.hmyc.web.bind.mapper.axbn.PrivateBindInfoAxbnMapper;
import com.cqt.hmyc.web.bind.service.axbn.AxbnBindConverter;
import com.cqt.hmyc.web.bind.service.recycle.db.DbOperationStrategy;
import com.cqt.model.bind.axbn.entity.PrivateBindAxbnRealTel;
import com.cqt.model.bind.axbn.entity.PrivateBindInfoAxbn;
import com.cqt.model.bind.axbn.entity.PrivateBindInfoAxbnHis;
import com.cqt.model.bind.bo.MqBindInfoBO;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.api.hint.HintManager;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author linshiqiang
 * @date 2022/4/19 20:32
 */
@Service
@Slf4j
public class AxbnDbOperationStrategyImpl implements DbOperationStrategy {

    public static final String BUSINESS_TYPE = BusinessTypeEnum.AXBN.name();

    @Resource
    private PrivateBindInfoAxbnMapper privateBindInfoAxbnMapper;

    @Resource
    private PrivateBindInfoAxbnHisMapper privateBindInfoAxbnHisMapper;

    @Resource
    private PrivateBindAxbnRealTelMapper privateBindAxbnRealTelMapper;

    @Resource
    private AxbnBindConverter axbnBindConverter;

    @Override
    public String getBusinessType() {
        return BUSINESS_TYPE;
    }

    @Override
    public void operate(MqBindInfoBO mqBindInfoBO) {
        try (HintManager hintManager = HintManager.getInstance()) {
            hintManager.addTableShardingValue(TableNameConstant.PRIVATE_BIND_AXBN_REAL_TEL, mqBindInfoBO.getVccId());
            hintManager.addTableShardingValue(TableNameConstant.PRIVATE_BIND_INFO_AXBN, mqBindInfoBO.getVccId());
            hintManager.addTableShardingValue(TableNameConstant.PRIVATE_BIND_INFO_AXBN_HIS, mqBindInfoBO.getDate());
            saveAxbn(mqBindInfoBO);
        }
    }

    private void saveAxbn(MqBindInfoBO mqBindInfoBO) {
        PrivateBindInfoAxbn privateBindInfoAxbn = mqBindInfoBO.getPrivateBindInfoAxbn();
        if (OperateTypeEnum.INSERT.name().equals(mqBindInfoBO.getOperateType())) {
            privateBindInfoAxbn.setCreateTime(mqBindInfoBO.getDateTime());
            privateBindInfoAxbn.setUpdateTime(mqBindInfoBO.getDateTime());
            int insert = privateBindInfoAxbnMapper.insert(privateBindInfoAxbn);
            PrivateBindInfoAxbnHis bindInfoAxbnHis = axbnBindConverter.bindInfoAxbn2BindInfoAxbnHis(privateBindInfoAxbn);
            List<PrivateBindAxbnRealTel> privateBindAxbnRealTelList = getPrivateBindAxbnRealTelList(privateBindInfoAxbn);
            privateBindAxbnRealTelMapper.insertBatch(privateBindAxbnRealTelList);
            privateBindInfoAxbnHisMapper.insert(bindInfoAxbnHis);
            // 保存真实号码
            if (insert == 0) {
                log.error("axbn insert db fail: {}", privateBindInfoAxbn);
            }
        }
        if (OperateTypeEnum.UPDATE.name().equals(mqBindInfoBO.getOperateType())) {
            PrivateBindInfoAxbn bindInfoAxbn = new PrivateBindInfoAxbn();
            bindInfoAxbn.setBindId(privateBindInfoAxbn.getBindId());
            bindInfoAxbn.setExpiration(privateBindInfoAxbn.getExpiration());
            bindInfoAxbn.setExpireTime(privateBindInfoAxbn.getExpireTime());
            bindInfoAxbn.setUpdateTime(mqBindInfoBO.getDateTime());
            int update = privateBindInfoAxbnMapper.updateById(bindInfoAxbn);
            if (update == 0) {
                log.error("axbn update db fail: {}", privateBindInfoAxbn);
            }
        }
        if (OperateTypeEnum.DELETE.name().equals(mqBindInfoBO.getOperateType())) {
            int delete = privateBindInfoAxbnMapper.deleteById(privateBindInfoAxbn.getBindId());
            LambdaQueryWrapper<PrivateBindAxbnRealTel> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(PrivateBindAxbnRealTel::getBindId, privateBindInfoAxbn.getBindId());
            privateBindAxbnRealTelMapper.delete(queryWrapper);
            if (delete == 0) {
                log.error("axbn delete db fail: {}", privateBindInfoAxbn.getBindId());
            }
        }
    }

    /**
     * AXBN 真实号码列表
     */
    @SuppressWarnings("all")
    private List<PrivateBindAxbnRealTel> getPrivateBindAxbnRealTelList(PrivateBindInfoAxbn privateBindInfoAxbn) {
        List<PrivateBindAxbnRealTel> realTelList = new ArrayList<>();
        PrivateBindAxbnRealTel realTelAx = PrivateBindAxbnRealTel.builder()
                .id(BindIdUtil.getAxbnRealTelId(privateBindInfoAxbn.getVccId(), privateBindInfoAxbn.getTelA(), privateBindInfoAxbn.getTelX()))
                .tel(privateBindInfoAxbn.getTelA())
                .telX(privateBindInfoAxbn.getTelX())
                .areaCode(privateBindInfoAxbn.getAreaCode())
                .bindId(privateBindInfoAxbn.getBindId())
                .vccId(privateBindInfoAxbn.getVccId())
                .createTime(DateUtil.date())
                .build();
        realTelList.add(realTelAx);
        PrivateBindAxbnRealTel realTelBx = PrivateBindAxbnRealTel.builder()
                .id(BindIdUtil.getAxbnRealTelId(privateBindInfoAxbn.getVccId(), privateBindInfoAxbn.getTelB(), privateBindInfoAxbn.getTelX()))
                .tel(privateBindInfoAxbn.getTelB())
                .telX(privateBindInfoAxbn.getTelX())
                .areaCode(privateBindInfoAxbn.getAreaCode())
                .bindId(privateBindInfoAxbn.getBindId())
                .vccId(privateBindInfoAxbn.getVccId())
                .createTime(DateUtil.date())
                .build();
        realTelList.add(realTelBx);
        String otherBy = privateBindInfoAxbn.getOtherBy();
        if (StrUtil.isNotEmpty(otherBy)) {
            Map<String, String> otherByMap = JSON.parseObject(otherBy, Map.class);
            for (Map.Entry<String, String> entry : otherByMap.entrySet()) {
                String bNum = entry.getKey();
                String yNum = entry.getValue();
                PrivateBindAxbnRealTel realTelOtherBx = PrivateBindAxbnRealTel.builder()
                        .id(BindIdUtil.getAxbnRealTelId(privateBindInfoAxbn.getVccId(), bNum, privateBindInfoAxbn.getTelX()))
                        .tel(bNum)
                        .telX(privateBindInfoAxbn.getTelX())
                        .areaCode(privateBindInfoAxbn.getAreaCode())
                        .bindId(privateBindInfoAxbn.getBindId())
                        .vccId(privateBindInfoAxbn.getVccId())
                        .createTime(DateUtil.date())
                        .build();
                realTelList.add(realTelOtherBx);
                if (ModeEnum.ALL_B.getCode().equals(privateBindInfoAxbn.getMode())) {
                    PrivateBindAxbnRealTel realTelAy = PrivateBindAxbnRealTel.builder()
                            .id(BindIdUtil.getAxbnRealTelId(privateBindInfoAxbn.getVccId(), privateBindInfoAxbn.getTelA(), yNum))
                            .tel(privateBindInfoAxbn.getTelA())
                            .telX(yNum)
                            .areaCode(privateBindInfoAxbn.getAreaCode())
                            .bindId(privateBindInfoAxbn.getBindId())
                            .vccId(privateBindInfoAxbn.getVccId())
                            .createTime(DateUtil.date())
                            .build();
                    realTelList.add(realTelAy);
                }
            }
        }
        return realTelList;
    }


}
