package com.cqt.hmyc.web.bind.service.axb;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cqt.common.constants.TableNameConstant;
import com.cqt.common.enums.BusinessTypeEnum;
import com.cqt.common.enums.InitFlagEnum;
import com.cqt.common.util.PrivateCacheUtil;
import com.cqt.hmyc.web.bind.mapper.axb.PrivateBindAxbInitUserTelPoolMapper;
import com.cqt.hmyc.web.bind.mapper.axb.PrivateBindInfoAxbMapper;
import com.cqt.hmyc.web.cache.request.RequestCache;
import com.cqt.hmyc.web.cache.request.RequestDelayed;
import com.cqt.model.bind.axb.dto.AxbBindIdKeyInfoDTO;
import com.cqt.model.bind.axb.entity.PrivateBindAxbInitUserTelPool;
import com.cqt.model.bind.axb.entity.PrivateBindInfoAxb;
import com.cqt.model.bind.axb.vo.AxbBindingVO;
import com.cqt.model.common.properties.HideProperties;
import com.cqt.redis.config.DoubleRedisProperties;
import com.cqt.redis.util.RedissonUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.api.hint.HintManager;
import org.redisson.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * @author linshiqiang
 * @date 2021/9/9 15:51
 */
@Service
@Slf4j
public class AxbBindCacheService {

    private static final String TYPE = BusinessTypeEnum.AXB.name();

    @Resource
    private RedissonUtil redissonUtil;

    @Qualifier(value = "redissonClient")
    @Autowired
    private RedissonClient redissonClient;

    @Qualifier(value = "redissonClient2")
    @Autowired(required = false)
    private RedissonClient redissonClient2;

    @Resource
    private DoubleRedisProperties redisProperties;

    @Resource
    private HideProperties hideProperties;

    @Resource(name = "saveExecutor")
    private ThreadPoolTaskExecutor saveExecutor;

    @Resource
    private PrivateBindAxbInitUserTelPoolMapper bindAxbInitUserTelPoolMapper;

    @Resource
    private PrivateBindInfoAxbMapper privateBindInfoAxbMapper;

    @Resource
    private AxbBindConverter axbBindConverter;

    /**
     * 查询AXB 绑定关系 by requestId
     */
    public Optional<AxbBindingVO> getAxbBindInfoByRequestId(String vccId, String requestId) {
        String bindInfoStr;
        try {
            String axbRequestIdKey = PrivateCacheUtil.getRequestIdKey(vccId, TYPE, requestId);
            RBucket<Object> bucket = redissonClient.getBucket(axbRequestIdKey);
            boolean trySet = bucket.trySet(StrUtil.EMPTY_JSON, hideProperties.getSetnxTimeout(), TimeUnit.SECONDS);
            if (trySet) {
                // 设置成功的 是第一次
                return Optional.empty();
            } else {
                // 已存在key
                bindInfoStr = redissonUtil.getString(axbRequestIdKey);
                if (StrUtil.EMPTY_JSON.equals(bindInfoStr)) {
                    CompletableFuture<String> completableFuture = new CompletableFuture<>();
                    String threadName = Thread.currentThread().getName();
                    RequestCache.REQUEST_ID_INFO_CACHE.put(axbRequestIdKey + threadName, completableFuture);
                    // 发送jdk延时队列
                    RequestCache.REQUEST_DELAYED_QUEUE.put(new RequestDelayed(hideProperties.getRequestTimeout(), TimeUnit.MILLISECONDS, axbRequestIdKey, threadName));
                    // 阻塞等待结果, 达到hideProperties.getMaxRequestTimeout(), 超时 抛出异常, 查询数据库
                    bindInfoStr = completableFuture.get(hideProperties.getMaxRequestTimeout(), TimeUnit.MILLISECONDS);
                    log.info("requestId: {}, 重复请求获取结果完成.", requestId);
                }
            }
        } catch (Exception e) {
            log.error("redis get操作异常: ", e);
            // 查db
            return getAxbBindingVoByDb(vccId, requestId);

        }
        if (StrUtil.isEmpty(bindInfoStr)) {
            return Optional.empty();
        }
        return Optional.ofNullable(JSON.parseObject(bindInfoStr, AxbBindingVO.class));
    }

    /**
     * 查询AxbBindingVO返回结果 by requestId
     */
    private Optional<AxbBindingVO> getAxbBindingVoByDb(String vccId, String requestId) {
        try {
            LambdaQueryWrapper<PrivateBindInfoAxb> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.select(PrivateBindInfoAxb::getBindId, PrivateBindInfoAxb::getTelX);
            queryWrapper.eq(PrivateBindInfoAxb::getRequestId, requestId);
            queryWrapper.gt(PrivateBindInfoAxb::getExpireTime, DateUtil.date());
            queryWrapper.last("limit 1");
            try (HintManager hintManager = HintManager.getInstance()) {
                hintManager.addTableShardingValue(TableNameConstant.PRIVATE_BIND_INFO_AXB, vccId);
                PrivateBindInfoAxb bindInfoAxb = privateBindInfoAxbMapper.selectOne(queryWrapper);
                AxbBindingVO axbBindingVO = axbBindConverter.bindInfoAxb2AxbBindingVO(bindInfoAxb);
                return Optional.ofNullable(axbBindingVO);
            }
        } catch (Exception e) {
            log.error("getAxbBindingVoByDb error: {}", e.getMessage());
        }
        return Optional.empty();
    }


    /**
     * 查询AXB 绑定关系 by bindId
     */
    public Optional<AxbBindIdKeyInfoDTO> getAxbBindInfoByBindId(String vccId, String bindId) {
        String bindInfoStr;
        try {
            bindInfoStr = redissonUtil.getString(PrivateCacheUtil.getBindIdKey(vccId, TYPE, bindId));
        } catch (Exception e) {
            log.error("redis get操作异常: ", e);
            // 查db
            return getAxbBindIdKeyInfoDtoByDb(vccId, bindId);
        }
        if (StrUtil.isEmpty(bindInfoStr)) {
            return getAxbBindIdKeyInfoDtoByDb(vccId, bindId);
        }
        return Optional.ofNullable(JSON.parseObject(bindInfoStr, AxbBindIdKeyInfoDTO.class));
    }

    public Optional<AxbBindIdKeyInfoDTO> getAxbBindIdKeyInfoDtoByDb(String vccId, String bindId) {
        try {
            try (HintManager hintManager = HintManager.getInstance()) {
                hintManager.addTableShardingValue(TableNameConstant.PRIVATE_BIND_INFO_AXB, vccId);
                LambdaQueryWrapper<PrivateBindInfoAxb> queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.eq(PrivateBindInfoAxb::getBindId, bindId);
                queryWrapper.gt(PrivateBindInfoAxb::getExpireTime, DateUtil.date());
                queryWrapper.last("limit 1");
                PrivateBindInfoAxb bindInfoAxb = privateBindInfoAxbMapper.selectOne(queryWrapper);
                AxbBindIdKeyInfoDTO bindIdKeyInfoDTO = axbBindConverter.bindInfoAxb2AxbBindIdKeyInfoDTO(bindInfoAxb);
                // TODO 回写redis
                return Optional.ofNullable(bindIdKeyInfoDTO);
            }
        } catch (Exception e) {
            log.error("getAxbBindIdKeyInfoDtoByDb error: {}", e.getMessage());
        }
        return Optional.empty();
    }

    /**
     * 多个set 移除同一个元素, pipeline
     */
    public void removeSetByKeys(Object random, String... keys) {

        try {
            RBatch batch = redissonClient.createBatch();
            for (String key : keys) {
                batch.getSet(key).removeAsync(random);
            }
            batch.execute();
            if (redisProperties.getCluster2().getActive()) {
                RBatch batch2 = redissonClient2.createBatch();
                for (String key : keys) {
                    batch2.getSet(key).removeAsync(random);
                }
                RFuture<BatchResult<?>> executeAsync = batch2.executeAsync();
                log.info("{}, 异地移除元素池结果: {}", keys, executeAsync.get().getResponses());
            }
        } catch (Exception e) {
            log.error("redisson removeSetByKeys 操作异常: ", e);
        }
    }

    /**
     * 初始化标识
     * 可用号码池里 sadd  mt:pool:{A}:{area_code}
     */
    public void initPool(String vccId, String initKey, String poolKey, Set<String> poolSet, String tel, String areaCode,
                         String flag, Boolean directSecond) {

        try {
            RBatch batch = redissonClient.createBatch();
            batch.getBucket(initKey).setAsync(flag);
            batch.getSet(poolKey).addAllAsync(poolSet);
            // 初始化不能异步
            batch.execute();

            if (redisProperties.getCluster2().getActive()) {
                try {
                    RBatch batch2 = redissonClient2.createBatch();
                    batch2.getBucket(initKey).setAsync(flag);
                    batch2.getSet(poolKey).addAllAsync(poolSet);
                    RFuture<BatchResult<?>> executeAsync = batch2.executeAsync();
                    log.info("{}, 异地初始化号码池结果: {}", poolKey, executeAsync.get().getResponses());
                } catch (Exception e) {
                    log.error("AXb 异地 initPool: {}, 操作异常: ", poolKey, e);
                }
            }
            saveExecutor.execute(() -> {
                try {
                    try (HintManager hintManager = HintManager.getInstance()) {
                        hintManager.addTableShardingValue(TableNameConstant.PRIVATE_BIND_AXB_INIT_USER_TEL_POOL, vccId);
                        saveBindInitUserTelPool(vccId, tel, areaCode, flag, directSecond);
                    }
                } catch (Exception e) {
                    log.error("{}, {}. {}, {}, 持久化初始化的用户号码异常: {}", tel, areaCode, flag, directSecond, e.getMessage());
                }
            });
        } catch (Exception e) {
            log.error("AXb initPool: {}, 操作异常: ", poolKey, e);
        }
    }

    /**
     * 保存初始化AB号码
     */
    private void saveBindInitUserTelPool(String vccId, String tel, String areaCode, String flag, Boolean directSecond) {

        String id = vccId + ":" + areaCode + ":" + tel;
        PrivateBindAxbInitUserTelPool privateBindAxbInitUserTelPool = bindAxbInitUserTelPoolMapper.selectVersion(id);
        // 保存初始化的用户号码
        if (InitFlagEnum.FIRST_INIT.getCode().equals(flag) || InitFlagEnum.THREE_INIT.getCode().equals(flag)) {
            // 第一次初始化
            PrivateBindAxbInitUserTelPool bindInitUserTelPool = PrivateBindAxbInitUserTelPool.builder()
                    .id(id)
                    .initFlag(Convert.toInt(flag))
                    .tel(tel)
                    .areaCode(areaCode)
                    .vccId(vccId)
                    .createTime(DateUtil.date())
                    .updateTime(DateUtil.date())
                    .build();
            if (privateBindAxbInitUserTelPool == null) {
                bindInitUserTelPool.setVersion(1);
                int insert = bindAxbInitUserTelPoolMapper.insert(bindInitUserTelPool);
                if (insert == 0) {
                    log.error("新增第一次初始化的用户号码失败: {}", bindInitUserTelPool);
                }
                return;
            }
            bindInitUserTelPool.setVersion(privateBindAxbInitUserTelPool.getVersion());
            int update = bindAxbInitUserTelPoolMapper.updateById(bindInitUserTelPool);
            if (update == 0) {
                log.error("update第一次初始化的用户号码失败: {}", bindInitUserTelPool);
            }
            return;
        }
        PrivateBindAxbInitUserTelPool bindInitUserTelPool = PrivateBindAxbInitUserTelPool.builder()
                .id(id)
                .initFlag(Convert.toInt(flag))
                .updateTime(DateUtil.date())
                .build();
        if (directSecond) {
            bindInitUserTelPool.setVersion(1);
            bindInitUserTelPool.setTel(tel);
            bindInitUserTelPool.setVccId(vccId);
            bindInitUserTelPool.setCreateTime(DateUtil.date());
            bindInitUserTelPool.setAreaCode(areaCode);
            if (privateBindAxbInitUserTelPool == null) {
                int insert = bindAxbInitUserTelPoolMapper.insert(bindInitUserTelPool);
                if (insert != 1) {
                    log.error("新增直接第二次初始化的用户号码失败: {}, 第一次初始化就使用了备池, init_flag=2", bindInitUserTelPool);
                }
                return;
            }
            bindInitUserTelPool.setVersion(privateBindAxbInitUserTelPool.getVersion());
            int update = bindAxbInitUserTelPoolMapper.updateById(bindInitUserTelPool);
            if (update == 0) {
                log.error("update直接第二次初始化的用户号码失败: {}", bindInitUserTelPool);
            }
            return;
        }
        if (privateBindAxbInitUserTelPool != null) {
            bindInitUserTelPool.setVersion(privateBindAxbInitUserTelPool.getVersion());
        }
        int update = bindAxbInitUserTelPoolMapper.updateById(bindInitUserTelPool);
        if (update == 0) {
            log.error("修改初始化的用户号码失败: {}, 初始化就使用了备池, init_flag=2", bindInitUserTelPool);
        }
    }


}
