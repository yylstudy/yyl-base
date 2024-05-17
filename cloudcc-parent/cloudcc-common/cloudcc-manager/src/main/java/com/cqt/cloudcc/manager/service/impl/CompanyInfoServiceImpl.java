package com.cqt.cloudcc.manager.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cqt.base.contants.CommonConstant;
import com.cqt.base.enums.DefaultToneEnum;
import com.cqt.base.util.CacheUtil;
import com.cqt.cloudcc.manager.service.CompanyInfoService;
import com.cqt.cloudcc.manager.service.FileInfoService;
import com.cqt.mapper.CompanyInfoMapper;
import com.cqt.mapper.PlatformConfigMapper;
import com.cqt.model.company.entity.CompanyInfo;
import com.cqt.starter.redis.util.RedissonUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author linshiqiang
 * date:  2023-08-28 14:04
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CompanyInfoServiceImpl implements CompanyInfoService {

    private final RedissonUtil redissonUtil;

    private final CompanyInfoMapper companyInfoMapper;

    private final PlatformConfigMapper platformConfigMapper;

    private final ObjectMapper objectMapper;

    private final FileInfoService fileInfoService;

    @Override
    public CompanyInfo getCompanyInfoDTO(String companyCode) {
        String companyInfoKey = CacheUtil.getCompanyInfoKey(companyCode);
        try {
            CompanyInfo companyInfo = redissonUtil.get(companyInfoKey, CompanyInfo.class);
            if (Objects.nonNull(companyInfo)) {
                return companyInfo;
            }
        } catch (Exception e) {
            log.error("[查询企业信息] key: {}, 异常: ", companyInfoKey, e);
        }
        CompanyInfo companyInfo = companyInfoMapper.selectById(companyCode);
        if (Objects.nonNull(companyInfo)) {
            // 回写redis
            try {
                boolean set = redissonUtil.setString(companyInfoKey, objectMapper.writeValueAsString(companyInfo));
                log.info("[查询企业信息] companyCode: {}, 查询db, 回写redis: {}", companyCode, set);
            } catch (JsonProcessingException e) {
                log.error("[查询企业信息] key: {}, 回写异常: ", companyInfoKey, e);
            }
        }
        return companyInfo;
    }

    @Override
    public Set<String> getEnableCompanyCode() {
        String enableCompanyCodeKey = CacheUtil.getEnableCompanyCodeKey();
        try {
            Set<String> companyCodeSet = redissonUtil.getSet(enableCompanyCodeKey);
            if (CollUtil.isNotEmpty(companyCodeSet)) {
                return companyCodeSet;
            }
        } catch (Exception e) {
            log.error("[查询企业code] redis查询异常: ", e);
        }
        // 为空需要查询db
        LambdaQueryWrapper<CompanyInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.select(CompanyInfo::getCompanyCode);
        Set<String> set = companyInfoMapper.selectList(queryWrapper)
                .stream()
                .filter(e -> Objects.nonNull(e.getState()))
                .filter(item -> item.getState() == 1)
                .map(CompanyInfo::getCompanyCode)
                .collect(Collectors.toSet());
        if (CollUtil.isNotEmpty(set)) {
            redissonUtil.addAllSet(enableCompanyCodeKey, set);
        }
        return set;
    }

    @Override
    public Set<String> getAllCompanyCode() {
        return companyInfoMapper.selectList(null)
                .stream()
                .map(CompanyInfo::getCompanyCode)
                .collect(Collectors.toSet());
    }

    @Override
    public String getPlatformDefaultTone(DefaultToneEnum defaultTone) {
        // 查询redis平台默认音效
        String toneId = "";
        try {
            String platformDefaultToneKey = CacheUtil.getPlatformDefaultToneKey(defaultTone.getCode());
            toneId = redissonUtil.get(platformDefaultToneKey);
            if (StrUtil.isNotEmpty(toneId)) {
                return fileInfoService.getFilePath(CommonConstant.GLOBAL_COMPANY_CODE, toneId);
            }
        } catch (Exception e) {
            log.error("[查询平台默认音效] redis查询异常: ", e);
        }
        // 查询平台默认音效 db
        toneId = platformConfigMapper.getDefaultToneId(defaultTone.getCode());
        if (StrUtil.isNotEmpty(toneId)) {
            String platformDefaultToneKey = CacheUtil.getPlatformDefaultToneKey(defaultTone.getCode());
            redissonUtil.set(platformDefaultToneKey, toneId);
            return fileInfoService.getFilePath(CommonConstant.GLOBAL_COMPANY_CODE, toneId);
        }
        return null;
    }
}
