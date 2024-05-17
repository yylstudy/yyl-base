package com.cqt.client.service;

import cn.hutool.core.util.StrUtil;
import com.cqt.base.util.CacheUtil;
import com.cqt.client.config.nacos.CloudNettyProperties;
import com.cqt.model.common.CloudCallCenterProperties;
import com.cqt.starter.redis.util.RedissonUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author linshiqiang
 * date:  2023-11-23 13:54
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TokenVerifyService {

    private final RedissonUtil redissonUtil;

    private final CloudCallCenterProperties cloudCallCenterProperties;

    private final CloudNettyProperties cloudNettyProperties;

    public boolean checkToken(String sdkToken) {
        if (!cloudCallCenterProperties.getAuth()) {
            return true;
        }
        try {
            String tokenInfoKey = CacheUtil.getTokenInfoKey(sdkToken);
            String tokenInfo = redissonUtil.get(tokenInfoKey);
            if (StrUtil.isEmpty(tokenInfo)) {
                return false;
            }
            String[] info = StrUtil.splitToArray(tokenInfo, StrUtil.COLON);
            String companyCode = info[0];
            String os = info[1];
            String agentId = info[2];
            String tokenKey = CacheUtil.getTokenKey(companyCode, os, agentId);
            String token = redissonUtil.get(tokenKey);
            return StrUtil.isNotEmpty(token) && token.equals(sdkToken);
        } catch (Exception e) {
            log.error("[checkToken] token: {}, error: ", sdkToken, e);
            return true;
        }
    }

    public boolean checkToken(String sdkToken, String msgType) {
        List<String> passAuthList = cloudNettyProperties.getPassAuthList();
        if (passAuthList.contains(msgType)) {
            return true;
        }
        return checkToken(sdkToken);
    }
}
