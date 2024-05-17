package com.cqt.hmyc.web.fotile.service.impl;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import cn.hutool.extra.pinyin.PinyinUtil;
import com.alibaba.fastjson.JSON;
import com.cqt.common.constants.TableNameConstant;
import com.cqt.common.enums.BusinessTypeEnum;
import com.cqt.common.enums.DualRecordModeEnum;
import com.cqt.common.enums.ModelEnum;
import com.cqt.common.enums.RecordFileFormatEnum;
import com.cqt.common.enums.RecordModeEnum;
import com.cqt.common.util.PrivateCacheUtil;
import com.cqt.hmyc.web.bind.manager.PrivateMqProducer;
import com.cqt.hmyc.web.bind.mapper.axb.PrivateBindInfoAxbHisMapper;
import com.cqt.hmyc.web.bind.mapper.axb.PrivateBindInfoAxbMapper;
import com.cqt.hmyc.web.bind.service.AxbBindConverter;
import com.cqt.hmyc.web.fotile.model.dto.BindOperationDTO;
import com.cqt.hmyc.web.fotile.model.vo.BindOperationResultVO;
import com.cqt.hmyc.web.fotile.service.FotileBindOperateStrategy;
import com.cqt.model.bind.axb.entity.PrivateBindInfoAxb;
import com.cqt.model.bind.axb.entity.PrivateBindInfoAxbHis;
import com.cqt.model.bind.dto.BindRecycleDTO;
import com.cqt.redis.util.RedissonUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.api.hint.HintManager;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * @author linshiqiang
 * @date 2022/7/22 16:20
 * 绑定操作策略
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class FotileBindingStrategyImpl implements FotileBindOperateStrategy {

    public static final Integer OPERATION_TYPE = 0;

    private final PrivateBindInfoAxbMapper privateBindInfoAxbMapper;

    private final PrivateBindInfoAxbHisMapper privateBindInfoAxbHisMapper;

    private final RedissonUtil redissonUtil;

    private final AxbBindConverter axbBindConverter;

    private final PrivateMqProducer privateMqProducer;

    @Override
    public BindOperationResultVO deal(BindOperationDTO bindOperationDTO, String vccId) {
        String businessType = BusinessTypeEnum.AXB.name();
        PrivateBindInfoAxb privateBindInfoAxb = getPrivateBindInfoAxb(bindOperationDTO, vccId);
        String bindId = privateBindInfoAxb.getBindId();
        // 入库
        try (HintManager hintManager = HintManager.getInstance()) {
            String date = DateUtil.format(privateBindInfoAxb.getCreateTime(), DatePattern.PURE_DATE_PATTERN);

            hintManager.addTableShardingValue(TableNameConstant.PRIVATE_BIND_INFO_AXB, vccId);
            hintManager.addTableShardingValue(TableNameConstant.PRIVATE_BIND_INFO_AXB_HIS, date);
            privateBindInfoAxb.setSupplierId("fangtai");
            PrivateBindInfoAxbHis privateBindInfoAxbHis = axbBindConverter.bindInfoAxb2BindInfoAxbHis(privateBindInfoAxb);

            int insert = privateBindInfoAxbMapper.insert(privateBindInfoAxb);
            int insertHis = privateBindInfoAxbHisMapper.insert(privateBindInfoAxbHis);
            log.info("vccId: {}, bindId: {}, insert axb : {}, insert axb  his: {}", vccId, bindId, insert, insertHis);
        } catch (Exception e) {
            log.error("vccId: {}, insert axb error: {}", vccId, e);
        }
        long expiration = privateBindInfoAxb.getExpiration();
        // 存redis ax bx
        String axBindInfoKey = PrivateCacheUtil.getBindInfoKey(vccId, businessType, privateBindInfoAxb.getTelA(), privateBindInfoAxb.getTelX());
        String bxBindInfoKey = PrivateCacheUtil.getBindInfoKey(vccId, businessType, privateBindInfoAxb.getTelB(), privateBindInfoAxb.getTelX());
        String bindInfoJson = JSON.toJSONString(privateBindInfoAxb);
        boolean setAx = redissonUtil.setObject(axBindInfoKey, bindInfoJson, expiration, TimeUnit.SECONDS);
        boolean setBx = redissonUtil.setObject(bxBindInfoKey, bindInfoJson, expiration, TimeUnit.SECONDS);
        log.info("vccId: {}, binding set ax: {}: {}, set bx: {}: {}", vccId, axBindInfoKey, setAx, bxBindInfoKey, setBx);

        // 发mq
        BindRecycleDTO bindRecycleDTO = axbBindConverter.bindInfoAxb2BindRecycleDTO(privateBindInfoAxb);
        bindRecycleDTO.setNumType(businessType);
        if (log.isInfoEnabled()) {
            log.info("vccId: {}, send mq: {}", vccId, JSON.toJSONString(bindRecycleDTO));
        }
        privateMqProducer.sendLazy(Optional.of(bindRecycleDTO), (int) expiration);
        return BindOperationResultVO.success("绑定成功");
    }

    /**
     * 构造PrivateBindInfoAxb 绑定信息
     *
     * @param bindOperationDTO 绑定入参
     * @param vccId            企业id
     * @return 绑定信息
     */
    private PrivateBindInfoAxb getPrivateBindInfoAxb(BindOperationDTO bindOperationDTO, String vccId) {
        // 主叫放音
        String callerIvr = PinyinUtil.getPinyin(Convert.toStr(bindOperationDTO.getCallerIvrName(), ""), "");
        // 被叫放音
        String calledIvr = PinyinUtil.getPinyin(Convert.toStr(bindOperationDTO.getCalledIvrName(), ""), "");
        // 有效期
        String effectTime = bindOperationDTO.getEffectTime();
        DateTime expireTime = DateUtil.parse(effectTime, DatePattern.PURE_DATETIME_PATTERN);
        long expiration = DateUtil.between(DateUtil.date(), expireTime, DateUnit.SECOND);

        return PrivateBindInfoAxb.builder()
                .telA(bindOperationDTO.getaPhone())
                .telX(bindOperationDTO.getxPhone())
                .telB(bindOperationDTO.getbPhone())
                .enableRecord(bindOperationDTO.getIsRadioRecord())
                .maxDuration(7200)
                .expiration(expiration)
                .expireTime(expireTime)
                .audioACallX(callerIvr)
                .audioBCalledX(calledIvr)
                .audioBCallX(callerIvr)
                .audioACalledX(calledIvr)
                .userData(bindOperationDTO.getUserData())
                .requestId(bindOperationDTO.getMessageId())
                .vccId(vccId)
                .bindId(bindOperationDTO.getBindId())
                .areaCode(bindOperationDTO.getAreaCode())
                .wholeArea(0)
                .cityCode(bindOperationDTO.getAreaCode())
                .type(0)
                .recordFileFormat(RecordFileFormatEnum.wav.name())
                .model(ModelEnum.TEL_X.getCode())
                .recordMode(RecordModeEnum.BINAURAL.getCode())
                .dualRecordMode(DualRecordModeEnum.CALLER_LEFT.getCode())
                .createTime(DateUtil.date())
                .updateTime(DateUtil.date())
                .build();
    }


    @Override
    public Integer getOperationType() {
        return OPERATION_TYPE;
    }
}
