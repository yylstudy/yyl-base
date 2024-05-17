package com.cqt.cloudcc.manager.service.impl;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cqt.base.enums.CallDirectionEnum;
import com.cqt.base.util.CacheUtil;
import com.cqt.cloudcc.manager.service.NumberInfoService;
import com.cqt.mapper.NumberInfoMapper;
import com.cqt.model.number.entity.NumberInfo;
import com.cqt.starter.redis.util.RedissonUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;

/**
 * @author linshiqiang
 * date:  2023-09-04 15:54
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NumberInfoServiceImpl implements NumberInfoService {

    private final RedissonUtil redissonUtil;

    private final NumberInfoMapper numberInfoMapper;

    @Override
    public Optional<NumberInfo> getNumberInfo(String number) {
        String numberInfoKey = CacheUtil.getNumberInfoKey(number);
        try {
            NumberInfo numberInfo = redissonUtil.get(numberInfoKey, NumberInfo.class);
            if (Objects.nonNull(numberInfo)) {
                return Optional.of(numberInfo);
            }
        } catch (Exception e) {
            log.error("[查询号码信息] 号码: {}, 异常: ", number, e);
        }
        // 查DB
        LambdaQueryWrapper<NumberInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(NumberInfo::getNumber, number);
        queryWrapper.last(" limit 1");
        return Optional.ofNullable(numberInfoMapper.selectOne(queryWrapper));
    }

    @Override
    public Integer getClientPriority(String companyCode, String callerNumber) {
        String clientPriorityKey = CacheUtil.getClientPriorityKey(companyCode, callerNumber);
        // 不查数据库, 默认Integer.MAX_VALUE
        return Convert.toInt(redissonUtil.get(clientPriorityKey), Integer.MAX_VALUE);
    }

    @Override
    public Boolean checkBlackNumber(String companyCode, String number, CallDirectionEnum callDirection) {
        Boolean isInGlobal = checkGlobalBlacklist(number, callDirection);
        if (isInGlobal) {
            return true;
        }
        return checkCompanyBlacklist(companyCode, number, callDirection);
    }

    private Boolean checkGlobalBlacklist(String number, CallDirectionEnum callDirection) {
        String blacklistGlobalKey = CacheUtil.getBlacklistGlobalKey(number);
        String globalType = redissonUtil.get(blacklistGlobalKey);
        if (StrUtil.isEmpty(globalType)) {
            return false;
        }
        if (CallDirectionEnum.ALL.getCode().equals(Integer.parseInt(globalType))) {
            return true;
        }
        if (CallDirectionEnum.OUTBOUND.equals(callDirection)) {
            return CallDirectionEnum.OUTBOUND.getCode().equals(Integer.parseInt(globalType));
        }
        if (CallDirectionEnum.INBOUND.equals(callDirection)) {
            return CallDirectionEnum.INBOUND.getCode().equals(Integer.parseInt(globalType));
        }
        return false;
    }

    private Boolean checkCompanyBlacklist(String companyCode, String number, CallDirectionEnum callDirection) {
        String blacklistCompanyKey = CacheUtil.getBlacklistCompanyKey(companyCode, number);
        String companyType = redissonUtil.get(blacklistCompanyKey);
        if (StrUtil.isEmpty(companyType)) {
            return false;
        }
        if (CallDirectionEnum.ALL.getCode().equals(Integer.parseInt(companyType))) {
            return true;
        }
        if (CallDirectionEnum.OUTBOUND.equals(callDirection)) {
            return CallDirectionEnum.OUTBOUND.getCode().equals(Integer.parseInt(companyType));
        }
        if (CallDirectionEnum.INBOUND.equals(callDirection)) {
            return CallDirectionEnum.INBOUND.getCode().equals(Integer.parseInt(companyType));
        }
        return false;
    }
}
