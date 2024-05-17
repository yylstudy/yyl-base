package com.cqt.cloudcc.manager.service.impl;

import cn.hutool.core.util.StrUtil;
import com.cqt.base.util.CacheUtil;
import com.cqt.cloudcc.manager.service.IvrServiceInfoService;
import com.cqt.mapper.IvrServiceInfoMapper;
import com.cqt.model.queue.entity.IvrServiceInfo;
import com.cqt.starter.redis.util.RedissonUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * @author linshiqiang
 * date:  2023-10-09 14:41
 * @since 7.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class IvrServiceInfoServiceImpl implements IvrServiceInfoService {

    private final IvrServiceInfoMapper ivrServiceInfoMapper;

    private final RedissonUtil redissonUtil;

    @Override
    public IvrServiceInfo getIvrServiceInfo(String serveId) {
        if (StrUtil.isEmpty(serveId)) {
            return null;
        }
        // ivr服务配置读取redis
        String ivrServiceKey = CacheUtil.getIvrServiceKey(serveId);
        try {
            IvrServiceInfo ivrServiceInfo = redissonUtil.get(ivrServiceKey, IvrServiceInfo.class);
            if (Objects.nonNull(ivrServiceInfo)) {
                return ivrServiceInfo;
            }
            ivrServiceInfo = ivrServiceInfoMapper.selectById(serveId);
            if (Objects.nonNull(ivrServiceInfo)) {
                redissonUtil.setSerialize(ivrServiceKey, ivrServiceInfo);
            }
            return ivrServiceInfo;
        } catch (Exception e) {
            log.error("[IvrServiceInfo] serveId: {}, error: ", serveId, e);
        }
        return null;
    }
}
