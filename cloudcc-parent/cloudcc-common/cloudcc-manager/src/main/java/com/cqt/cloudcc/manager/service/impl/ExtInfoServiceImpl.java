package com.cqt.cloudcc.manager.service.impl;

import com.cqt.base.util.CacheUtil;
import com.cqt.cloudcc.manager.service.ExtInfoService;
import com.cqt.mapper.ExtInfoMapper;
import com.cqt.model.ext.dto.ExtStatusDTO;
import com.cqt.model.ext.entity.ExtInfo;
import com.cqt.starter.redis.util.RedissonUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * @author linshiqiang
 * date:  2023-08-22 10:07
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ExtInfoServiceImpl implements ExtInfoService {

    private final RedissonUtil redissonUtil;

    private final ObjectMapper objectMapper;

    private final ExtInfoMapper extInfoMapper;

    @Override
    public ExtInfo getExtInfo(String companyCode, String extId) throws Exception {
        String extInfoKey = CacheUtil.getExtInfoKey(extId);
        try {
            ExtInfo extInfo = redissonUtil.get(extInfoKey, ExtInfo.class);
            if (Objects.nonNull(extInfo)) {
                return extInfo;
            }
        } catch (Exception e) {
            log.error("[查询分机信息] key: {}, 异常: ", extInfoKey, e);
        }
        // redis异常或无缓存查库
        ExtInfo extInfo = extInfoMapper.selectById(extId);
        if (Objects.nonNull(extInfo)) {
            boolean res = redissonUtil.setString(extInfoKey, objectMapper.writeValueAsString(extInfo));
            log.info("[查询分机信息] extId: {}, 查询db, 回写redis: {}", extId, res);
        }
        return extInfo;
    }

    @Override
    public ExtStatusDTO getActualExtStatus(String companyCode, String extId) {
        String extStatusKey = CacheUtil.getExtStatusKey(companyCode, extId);
        try {
            return redissonUtil.get(extStatusKey, ExtStatusDTO.class);
        } catch (Exception e) {
            log.error("[getActualExtStatus] key: {}, error: ", extStatusKey, e);
        }
        return null;
    }

    @Override
    public void updateActualExtStatus(ExtStatusDTO extStatusDTO) {
        try {
            String extId = extStatusDTO.getExtId();
            String extStatusKey = CacheUtil.getExtStatusKey(extStatusDTO.getCompanyCode(), extId);
            String extStatusJson = objectMapper.writeValueAsString(extStatusDTO);
            boolean set = redissonUtil.setString(extStatusKey, extStatusJson);
            log.info("[分机状态] key: {}, 保存redis结果: {}", extStatusKey, set);
        } catch (Exception e) {
            log.error("[修改分机状态] 异常: ", e);
        }
    }
}
