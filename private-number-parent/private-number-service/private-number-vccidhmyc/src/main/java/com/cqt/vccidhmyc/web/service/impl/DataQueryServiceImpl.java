package com.cqt.vccidhmyc.web.service.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cqt.common.constants.PrivateCacheConstant;
import com.cqt.common.constants.SystemConstant;
import com.cqt.common.util.PrivateCacheUtil;
import com.cqt.model.corpinfo.entity.PrivateVccCurManage;
import com.cqt.model.numpool.entity.PrivateNumberInfo;
import com.cqt.model.unicom.entity.PrivateCorpInteriorInfo;
import com.cqt.redis.util.RedissonUtil;
import com.cqt.vccidhmyc.web.manager.PrivateNumberInfoMapper;
import com.cqt.vccidhmyc.web.mapper.PrivateCorpInteriorInfoMapper;
import com.cqt.vccidhmyc.web.service.DataQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * @author linshiqiang
 * date:  2023-04-03 15:10
 * 数据查询服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DataQueryServiceImpl implements DataQueryService {

    private final PrivateCorpInteriorInfoMapper privateCorpInteriorInfoMapper;

    private final PrivateNumberInfoMapper privateNumberInfoMapper;

    private final RedissonUtil redissonUtil;

    @Override
    public Optional<String> getImsi(String roamingCalledNum) {
        // 查本地redis
        String imsi = redissonUtil.getString(roamingCalledNum);
        if (StrUtil.isEmpty(imsi)) {
            // 查备用redis
            imsi = redissonUtil.getStringOfBack(roamingCalledNum);
            if (StrUtil.isEmpty(imsi)) {
                log.warn("根据漫游号: {}, 查询imsi, 双机房redis均未查询到", roamingCalledNum);
                return Optional.empty();
            }
        }
        // 回收
        String gtCode = redissonUtil.getString(String.format(PrivateCacheConstant.GT_MSRN, roamingCalledNum));
        if (StrUtil.isNotEmpty(gtCode)) {
            Boolean addSet = redissonUtil.addSet(String.format(PrivateCacheConstant.MSRN_STATUS0_SET, gtCode), roamingCalledNum);
            log.info("漫游号: {}, gtCode: {}, 回收漫游号: {}", roamingCalledNum, gtCode, addSet);
        }
        return Optional.of(imsi);
    }

    @Override
    public String getSecretNoByImsi(String imsi) {
        String secretNo = redissonUtil.getString(imsi);

        // 查db
        if (StrUtil.isEmpty(secretNo)) {
            LambdaQueryWrapper<PrivateNumberInfo> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.select(PrivateNumberInfo::getNumber);
            queryWrapper.eq(PrivateNumberInfo::getImsi, imsi);
            queryWrapper.last("limit 1" );
            PrivateNumberInfo numberInfo = privateNumberInfoMapper.selectOne(queryWrapper);
            if (ObjectUtil.isNotEmpty(numberInfo)) {
                return numberInfo.getNumber();
            }
        }
        return secretNo;
    }

    @Override
    public String getVccIdBySecretNo(String secretNo) {
        if (StrUtil.isEmpty(secretNo)) {
            return "";
        }
        String vccId = redissonUtil.getString(PrivateCacheUtil.getVccIdByNumberKey(secretNo));
        if (StrUtil.isEmpty(vccId)) {
            PrivateNumberInfo privateNumberInfo = privateNumberInfoMapper.selectById(secretNo);
            if (ObjectUtil.isNotEmpty(privateNumberInfo)) {
                vccId =  privateNumberInfo.getVccId();
            }
        }
        log.info("中间号: {}, 归属企业: {}", secretNo, vccId);
        return vccId;
    }

    @Override
    public Integer getCallLimit(String vccId) {
        String corpCallLimitInfoKey = PrivateCacheUtil.getCorpCallLimitInfoKey(vccId);
        String info = redissonUtil.getString(corpCallLimitInfoKey);
        if (StrUtil.isEmpty(info)) {
            return -1;
        }
        PrivateVccCurManage privateVccCurManage = JSON.parseObject(info, PrivateVccCurManage.class);
        String isVcc = privateVccCurManage.getIsVcc();
        // 为0不控制
        if (SystemConstant.ZERO.equals(isVcc)) {
            return -1;
        }
        return privateVccCurManage.getVccNum();
    }

    @Override
    public Optional<PrivateCorpInteriorInfo> getCorpInteriorInfo(String vccId) {

        // redis
        String corpInteriorInfoKey = PrivateCacheUtil.getCorpInteriorInfoKey(vccId, SystemConstant.MID_SERVICE_KEY);
        String info = redissonUtil.getString(corpInteriorInfoKey);
        if (StrUtil.isNotEmpty(info)) {
            return Optional.ofNullable(JSON.parseObject(info, PrivateCorpInteriorInfo.class));
        }

        LambdaQueryWrapper<PrivateCorpInteriorInfo> queryWrapper = new LambdaQueryWrapper<PrivateCorpInteriorInfo>()
                .eq(PrivateCorpInteriorInfo::getVccId, vccId)
                .eq(PrivateCorpInteriorInfo::getServiceKey, SystemConstant.MID_SERVICE_KEY)
                .last(" limit 1");
        PrivateCorpInteriorInfo interiorInfo = privateCorpInteriorInfoMapper.selectOne(queryWrapper);
        return Optional.ofNullable(interiorInfo);
    }
}
