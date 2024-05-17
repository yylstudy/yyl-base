package com.cqt.hmyc.web.bind.service.axb;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.cqt.common.enums.AxbPoolTypeEnum;
import com.cqt.common.enums.ErrorCodeEnum;
import com.cqt.common.enums.NumberTypeEnum;
import com.cqt.common.enums.OperateTypeEnum;
import com.cqt.common.util.BindIdUtil;
import com.cqt.common.util.PrivateCacheUtil;
import com.cqt.hmyc.web.bind.service.push.BindPushService;
import com.cqt.hmyc.web.bind.service.recycle.SaveBindService;
import com.cqt.hmyc.web.cache.NumberPoolAxbCache;
import com.cqt.hmyc.web.corpinfo.service.CorpBusinessService;
import com.cqt.model.bind.axb.dto.AxbBindIdKeyInfoDTO;
import com.cqt.model.bind.axb.entity.PrivateBindInfoAxb;
import com.cqt.model.bind.axb.vo.AxbBindingVO;
import com.cqt.model.bind.bo.MqBindInfoBO;
import com.cqt.model.bind.dto.BindRecycleDTO;
import com.cqt.model.bind.dto.UpdateExpirationDTO;
import com.cqt.model.bind.dto.UpdateTelBindDTO;
import com.cqt.model.common.Result;
import com.cqt.model.common.properties.HideProperties;
import com.cqt.model.corpinfo.dto.PrivateCorpBusinessInfoDTO;
import com.cqt.redis.config.DoubleRedisProperties;
import com.cqt.redis.util.RedissonUtil;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.BatchResult;
import org.redisson.api.RBatch;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * @author linshiqiang
 * @date 2022/2/16 13:51
 */
@Service
@Slf4j
public class AxbAsyncService {

    @Qualifier(value = "redissonClient")
    @Autowired
    private RedissonClient redissonClient;

    @Qualifier(value = "redissonClient2")
    @Autowired(required = false)
    private RedissonClient redissonClient2;

    @Resource
    private RedissonUtil redissonUtil;

    @Resource
    private DoubleRedisProperties redisProperties;

    @Resource(name = "otherExecutor")
    private ThreadPoolTaskExecutor otherExecutor;

    @Resource
    private AxbBindConverter axbBindConverter;

    @Resource
    private HideProperties hideProperties;

    @Resource
    private SaveBindService saveBindService;

    @Resource
    private AxbBindCacheService axbBindCacheService;

    @Resource
    private BindPushService bindPushService;

    @Resource
    private CorpBusinessService corpBusinessService;

    /**
     * 保存绑定关系到双机房redis, Mysql
     */
    public AxbBindingVO saveAxbBindInfo(PrivateBindInfoAxb bindInfoAxb, String numType) {
        String vccId = bindInfoAxb.getVccId();
        String bindId = bindInfoAxb.getBindId();
        String requestId = bindInfoAxb.getRequestId();
        String telA = bindInfoAxb.getTelA();
        String telB = bindInfoAxb.getTelB();
        String telX = bindInfoAxb.getTelX();
        long expiration = bindInfoAxb.getExpiration();

        AxbBindingVO axbBindingVO = axbBindConverter.bindInfoAxb2AxbBindingVO(bindInfoAxb);
        Optional<PrivateCorpBusinessInfoDTO> businessInfoOptional = corpBusinessService.getCorpBusinessInfo(vccId);
        if (businessInfoOptional.isPresent()) {
            PrivateCorpBusinessInfoDTO businessInfoDTO = businessInfoOptional.get();
            bindInfoAxb.setType(ObjectUtil.isNull(bindInfoAxb.getType()) ? businessInfoDTO.getSmsFlag() : bindInfoAxb.getType());
            bindInfoAxb.setEnableRecord(ObjectUtil.isNull(bindInfoAxb.getEnableRecord()) ? businessInfoDTO.getRecordFlag() : bindInfoAxb.getEnableRecord());
        }

        // 已绑定的关系string
        String axbBindIdKey = PrivateCacheUtil.getBindIdKey(vccId, numType, bindId);
        // 已请求绑定的关系string
        String axbRequestIdKey = PrivateCacheUtil.getRequestIdKey(vccId, numType, requestId);

        // 给fs查询的绑定关系string
        String axBindInfoKey = PrivateCacheUtil.getBindInfoKey(vccId, numType, telA, telX);
        String bxBindInfoKey = PrivateCacheUtil.getBindInfoKey(vccId, numType, telB, telX);
        String bindInfoJson = JSON.toJSONString(bindInfoAxb);

        String addBindAxbVoJson = JSON.toJSONString(axbBindingVO);
        AxbBindIdKeyInfoDTO axbBindIdKeyInfoDTO = axbBindConverter.bindInfoAxb2AxbBindIdKeyInfoDTO(bindInfoAxb);
        String axbBindIdKeyJson = JSON.toJSONString(axbBindIdKeyInfoDTO);
        RBatch batch = redissonClient.createBatch();
        batch.getBucket(axbBindIdKey).setAsync(axbBindIdKeyJson, expiration, TimeUnit.SECONDS);
        batch.getBucket(axbRequestIdKey).setAsync(addBindAxbVoJson, expiration, TimeUnit.SECONDS);
        batch.getBucket(axBindInfoKey).setAsync(bindInfoJson, expiration, TimeUnit.SECONDS);
        batch.getBucket(bxBindInfoKey).setAsync(bindInfoJson, expiration, TimeUnit.SECONDS);
        BatchResult<?> execute = batch.execute();
        log.info("save axb: bindId: {}, bind info result: {}", bindId, execute.getResponses());

        if (redisProperties.getCluster2().getActive()) {
            try {
                otherExecutor.execute(() -> {
                    try {
                        RBatch batch2 = redissonClient2.createBatch();
                        batch2.getBucket(axbBindIdKey).setAsync(axbBindIdKeyJson, expiration, TimeUnit.SECONDS);
                        batch2.getBucket(axbRequestIdKey).setAsync(addBindAxbVoJson, expiration, TimeUnit.SECONDS);
                        batch2.getBucket(axBindInfoKey).setAsync(bindInfoJson, expiration, TimeUnit.SECONDS);
                        batch2.getBucket(bxBindInfoKey).setAsync(bindInfoJson, expiration, TimeUnit.SECONDS);
                        batch2.execute();
                    } catch (Exception e) {
                        log.error("bindId: {}, 异地redis异常: {}", bindId, e.getMessage());
                    }
                });
            } catch (Exception e) {
                log.error("AXB设置绑定 saveAxbBindInfoToRedis, 数据: {}, 操作异常: ", bindInfoJson, e);
            }
        }
        sendAxbBindRecycleInfoToMq(Optional.empty(), bindInfoAxb, OperateTypeEnum.INSERT.name(), vccId, numType);
        return axbBindingVO;
    }

    /**
     * 解绑
     */
    public void unBind(AxbBindIdKeyInfoDTO axbBindIdKeyInfoDTO, String numType) {
        String telX = axbBindIdKeyInfoDTO.getTelX();
        String telA = axbBindIdKeyInfoDTO.getTelA();
        String telB = axbBindIdKeyInfoDTO.getTelB();
        String cityCode = axbBindIdKeyInfoDTO.getCityCode();
        String vccId = axbBindIdKeyInfoDTO.getVccId();
        String bindId = axbBindIdKeyInfoDTO.getBindId();
        String requestId = axbBindIdKeyInfoDTO.getRequestId();
        // 判断X号码是否被删除, 不在地市池子里
        Optional<HashSet<String>> poolOptional = NumberPoolAxbCache.getPool(AxbPoolTypeEnum.ALL, vccId, cityCode);

        // 已绑定的关系string
        String axbBindIdKey = PrivateCacheUtil.getBindIdKey(vccId, numType, bindId);
        // 已请求绑定的关系string
        String axbRequestIdKey = PrivateCacheUtil.getRequestIdKey(vccId, numType, requestId);
        // 1. X号码添加到A/B可用号码池中
        String axPool = PrivateCacheUtil.getUsablePoolKey(vccId, numType, cityCode, telA);
        String bxPool = PrivateCacheUtil.getUsablePoolKey(vccId, numType, cityCode, telB);

        // 给fs查询的绑定关系string
        String axBindInfoKey = PrivateCacheUtil.getBindInfoKey(vccId, numType, telA, telX);
        String bxBindInfoKey = PrivateCacheUtil.getBindInfoKey(vccId, numType, telB, telX);
        try {
            RBatch batch = redissonClient.createBatch();
            batch.getBucket(axbBindIdKey).deleteAsync();
            batch.getBucket(axbRequestIdKey).deleteAsync();
            batch.getBucket(axBindInfoKey).deleteAsync();
            batch.getBucket(bxBindInfoKey).deleteAsync();
            if (poolOptional.isPresent()) {
                HashSet<String> pool = poolOptional.get();
                if (pool.contains(telX)) {
                    if (BindIdUtil.isDirectTelX(axbBindIdKeyInfoDTO.getDirectTelX())) {
                        if (log.isInfoEnabled()) {
                            log.info("指定X号码, 不生产号码池: {}", JSON.toJSONString(axbBindIdKeyInfoDTO));
                        }
                    } else {
                        batch.getSet(axPool).addAsync(telX);
                        batch.getSet(bxPool).addAsync(telX);
                    }
                }
            }
            BatchResult<?> execute = batch.execute();
            log.info("del axb bindId: {}, bind info result: {}", bindId, execute.getResponses());

            otherExecutor.execute(() -> {
                if (redisProperties.getCluster2().getActive()) {
                    try {
                        RBatch batch2 = redissonClient2.createBatch();
                        batch2.getBucket(axbBindIdKey).deleteAsync();
                        batch2.getBucket(axbRequestIdKey).deleteAsync();
                        batch2.getBucket(axBindInfoKey).deleteAsync();
                        batch2.getBucket(bxBindInfoKey).deleteAsync();
                        if (poolOptional.isPresent()) {
                            HashSet<String> pool = poolOptional.get();
                            if (pool.contains(telX)) {
                                if (BindIdUtil.isDirectTelX(axbBindIdKeyInfoDTO.getDirectTelX())) {
                                    if (log.isInfoEnabled()) {
                                        log.info("指定X号码, 不生产号码池: {}", JSON.toJSONString(axbBindIdKeyInfoDTO));
                                    }
                                } else {
                                    batch2.getSet(axPool).addAsync(telX);
                                    batch2.getSet(bxPool).addAsync(telX);
                                }
                            }
                        }
                        batch2.execute();
                    } catch (Exception e) {
                        log.error("del axb bindId: {}, 异地redis异常: {}", bindId, e.getMessage());
                    }
                }
            });
            PrivateBindInfoAxb bindInfoAxb = axbBindConverter.bindIdKeyInfoDTO2axbBindIdKeyInfoDTO(axbBindIdKeyInfoDTO);
            sendAxbBindRecycleInfoToMq(Optional.empty(), bindInfoAxb, OperateTypeEnum.DELETE.name(), vccId, numType);
        } catch (Exception e) {
            log.error("AXB解绑 delAxbBind, 数据: {}, 操作异常: ", JSON.toJSONString(axbBindIdKeyInfoDTO), e);
        }
    }

    public void updateAxbExpiration(AxbBindIdKeyInfoDTO axbBindIdKeyInfoDTO, UpdateExpirationDTO updateExpirationDTO, String numType) {
        String vccId = axbBindIdKeyInfoDTO.getVccId();
        String bindId = axbBindIdKeyInfoDTO.getBindId();
        String requestId = axbBindIdKeyInfoDTO.getRequestId();
        String telA = axbBindIdKeyInfoDTO.getTelA();
        String telB = axbBindIdKeyInfoDTO.getTelB();
        String telX = axbBindIdKeyInfoDTO.getTelX();
        long expiration = updateExpirationDTO.getExpiration();
        // 已绑定的关系string
        String axbBindIdKey = PrivateCacheUtil.getBindIdKey(vccId, numType, bindId);
        // 已请求绑定的关系string
        String axbRequestIdKey = PrivateCacheUtil.getRequestIdKey(vccId, numType, requestId);
        // 给fs查询的绑定关系string
        String axBindInfoKey = PrivateCacheUtil.getBindInfoKey(vccId, numType, telA, telX);
        String bxBindInfoKey = PrivateCacheUtil.getBindInfoKey(vccId, numType, telB, telX);

        String bindInfo = redissonUtil.getString(axBindInfoKey);
        if (StrUtil.isEmpty(bindInfo)) {
            log.warn("bindId: {}, axBindInfoKey: {}, 绑定关系不存在", bindId, axBindInfoKey);
            bindInfo = redissonUtil.getString(bxBindInfoKey);
            if (StrUtil.isEmpty(bindInfo)) {
                log.warn("bindId: {}, bxBindInfoKey: {}, 绑定关系不存在", bindId, bxBindInfoKey);
                return;
            }
        }
        PrivateBindInfoAxb infoAxb = JSON.parseObject(bindInfo, PrivateBindInfoAxb.class);
        DateTime expireTime = DateUtil.offset(DateUtil.date(), DateField.SECOND, Convert.toInt(updateExpirationDTO.getExpiration()));
        infoAxb.setExpiration(updateExpirationDTO.getExpiration());
        infoAxb.setExpireTime(expireTime);
        String bindInfoJson = JSON.toJSONString(infoAxb);
        if (ObjectUtil.isNotEmpty(bindInfo)) {
            RBatch batch = redissonClient.createBatch();
            batch.getBucket(axbBindIdKey).expireAsync(expiration, TimeUnit.SECONDS);
            batch.getBucket(axbRequestIdKey).expireAsync(expiration, TimeUnit.SECONDS);
            batch.getBucket(axBindInfoKey).setAsync(bindInfoJson, expiration, TimeUnit.SECONDS);
            batch.getBucket(bxBindInfoKey).setAsync(bindInfoJson, expiration, TimeUnit.SECONDS);
            BatchResult<?> execute = batch.execute();
            log.info("update axb bindId: {}, bind info result: {}", bindId, execute.getResponses());
        }

        otherExecutor.execute(() -> {
            if (redisProperties.getCluster2().getActive()) {
                try {
                    RBatch batch2 = redissonClient2.createBatch();
                    batch2.getBucket(axbBindIdKey).expireAsync(expiration, TimeUnit.SECONDS);
                    batch2.getBucket(axbRequestIdKey).expireAsync(expiration, TimeUnit.SECONDS);
                    batch2.getBucket(axBindInfoKey).setAsync(bindInfoJson, expiration, TimeUnit.SECONDS);
                    batch2.getBucket(bxBindInfoKey).setAsync(bindInfoJson, expiration, TimeUnit.SECONDS);
                    batch2.execute();
                } catch (Exception e) {
                    log.error("update axb bindId: {}, 异地redis异常: {}", bindId, e.getMessage());
                }
            }
        });
        sendAxbBindRecycleInfoToMq(Optional.empty(), infoAxb, OperateTypeEnum.UPDATE.name(), vccId, numType);
    }

    /**
     * 更新已绑定号码
     */
    public Result updateAxbTelBind(AxbBindIdKeyInfoDTO axbBindIdKeyInfoDTO, UpdateTelBindDTO updateTelBindDTO, String numType) {
        String bindId = updateTelBindDTO.getBindId();
        String vccId = updateTelBindDTO.getVccId();
        String telBinded = updateTelBindDTO.getTelBinded();
        String telUpdate = updateTelBindDTO.getTelUpdate();
        String requestId = axbBindIdKeyInfoDTO.getRequestId();
        String telA = axbBindIdKeyInfoDTO.getTelA();
        String telB = axbBindIdKeyInfoDTO.getTelB();
        String telX = axbBindIdKeyInfoDTO.getTelX();
        String areaCode = axbBindIdKeyInfoDTO.getCityCode();
        // ax|bx
        String axBindInfoKey = PrivateCacheUtil.getBindInfoKey(vccId, numType, telA, telX);
        String bxBindInfoKey = PrivateCacheUtil.getBindInfoKey(vccId, numType, telB, telX);
        // bindId
        String axbBindIdKey = PrivateCacheUtil.getBindIdKey(vccId, numType, bindId);
        // new ax|bx
        String newAxBindInfoKey = "";
        String newBxBindInfoKey = "";
        // 查询ax绑定关系
        String axBindJson = Convert.toStr(redissonClient.getBucket(axBindInfoKey).get());
        if (StrUtil.isBlank(axBindJson)) {
            return Result.fail(ErrorCodeEnum.NOT_BIND.getCode(), ErrorCodeEnum.NOT_BIND.getMessage());
        }
        PrivateBindInfoAxb bindInfoAxb = JSON.parseObject(axBindJson, PrivateBindInfoAxb.class);
        boolean existFlag = false;
        boolean updateTelA = false;
        boolean updateTelB = false;
        String otherTel = "";
        if (telBinded.equals(telA)) {
            // 修改A号码
            axbBindIdKeyInfoDTO.setTelA(telUpdate);
            bindInfoAxb.setTelA(telUpdate);
            newAxBindInfoKey = PrivateCacheUtil.getBindInfoKey(vccId, numType, telUpdate, telX);
            existFlag = true;
            updateTelA = true;
            otherTel = telB;
        }
        if (telBinded.equals(telB)) {
            // 修改B号码
            axbBindIdKeyInfoDTO.setTelB(telUpdate);
            bindInfoAxb.setTelB(telUpdate);
            newBxBindInfoKey = PrivateCacheUtil.getBindInfoKey(vccId, numType, telUpdate, telX);
            existFlag = true;
            updateTelB = true;
            otherTel = telA;
        }
        if (!existFlag) {
            return Result.fail(ErrorCodeEnum.TEL_BIND_NOT_EXIST.getCode(), ErrorCodeEnum.TEL_BIND_NOT_EXIST.getMessage());
        }
        // 判断新号码和X是否存在绑定关系,
        if (telA.equals(telUpdate) || telB.equals(telUpdate)) {
            return Result.fail(ErrorCodeEnum.EXIST_VALID_BIND.getCode(), ErrorCodeEnum.EXIST_VALID_BIND.getMessage());
        }
        // 回收旧号码, 新号码发mq
        String axbUsableKey = PrivateCacheUtil.getUsablePoolKey(vccId, numType, areaCode, telBinded);
        String newAxbInitFlagKey = PrivateCacheUtil.getInitFlagKey(vccId, numType, areaCode, telUpdate);
        String newAxbUsableKey = PrivateCacheUtil.getUsablePoolKey(vccId, numType, areaCode, telUpdate);

        // 生成新绑定关系
        if (redissonUtil.isExistString(newAxbInitFlagKey)) {
            // 存在即初始化过, 查询可用号码池是否有这个X号码, 有则X号码没被telUpdate用过, 表示可修改
            Set<String> axPool = redissonUtil.getSet(newAxbUsableKey);
            if (CollUtil.isEmpty(axPool)) {
                // 可用号码池不足, 不能修改
                log.warn("bindId: {}, 号码池不足", bindId);
                return Result.fail(ErrorCodeEnum.POOL_LACK.getCode(), ErrorCodeEnum.POOL_LACK.getMessage());
            }
            // 移除x号码
            redissonUtil.removeSet(newAxbUsableKey, telX);
        } else {
            // 新号码的号码池数量与原绑定关系未修改的号码一致
            String initFlagOtherTelKey = PrivateCacheUtil.getInitFlagKey(vccId, numType, areaCode, otherTel);
            String initFlag = redissonUtil.getString(initFlagOtherTelKey);
            boolean directSecond = false;
            Optional<HashSet<String>> poolOptional;
            if ("1".equals(initFlag)) {
                poolOptional = NumberPoolAxbCache.getPool(AxbPoolTypeEnum.MASTER, vccId, areaCode);
            } else {
                directSecond = true;
                poolOptional = NumberPoolAxbCache.getPool(AxbPoolTypeEnum.ALL, vccId, areaCode);
            }
            // 未初始化过, 初始化ax可用号码池
            // 初始化是否要加锁
            if (poolOptional.isPresent()) {
                HashSet<String> pool = new HashSet<>(poolOptional.get());
                pool.remove(telX);
                axbBindCacheService.initPool(vccId, newAxbInitFlagKey, newAxbUsableKey, pool, telUpdate, areaCode, initFlag, directSecond);
            }
        }

        // ttl 新号码过去时间 算剩余时间
        long expiration = DateUtil.between(DateUtil.date(), bindInfoAxb.getExpireTime(), DateUnit.SECOND);
//        long expiration = bindInfoAxb.getExpiration();
        String bindIdKeyJson = JSON.toJSONString(axbBindIdKeyInfoDTO);
        String bindInfoJson = JSON.toJSONString(bindInfoAxb);
        RBatch batch = redissonClient.createBatch();
        batch.getBucket(axbBindIdKey).setAsync(bindIdKeyJson, expiration, TimeUnit.SECONDS);
        if (StrUtil.isNotEmpty(newAxBindInfoKey)) {
            batch.getBucket(axBindInfoKey).deleteAsync();
            batch.getBucket(newAxBindInfoKey).setAsync(bindInfoJson, expiration, TimeUnit.SECONDS);
            batch.getBucket(bxBindInfoKey).setAsync(bindInfoJson, expiration, TimeUnit.SECONDS);
        }
        if (StrUtil.isNotEmpty(newBxBindInfoKey)) {
            batch.getBucket(bxBindInfoKey).deleteAsync();
            batch.getBucket(newBxBindInfoKey).setAsync(bindInfoJson, expiration, TimeUnit.SECONDS);
            batch.getBucket(axBindInfoKey).setAsync(bindInfoJson, expiration, TimeUnit.SECONDS);
        }
        BatchResult<?> batchResult = batch.execute();
        log.info("AXB, bindId: {}, redis更新已绑定关系结果: {}", bindId, batchResult.getResponses());

        // 回收旧号码
        redissonUtil.addSet(axbUsableKey, telX);
        // 新号码发mq, 延时回收, 新号码的过期时间还是和绑定时一样?, 还是剩余时间
        BindRecycleDTO bindRecycleDTO = BindRecycleDTO.builder()
                .requestId(bindInfoAxb.getRequestId())
                .bindId(bindInfoAxb.getBindId())
                .telX(bindInfoAxb.getTelX())
                .telA(updateTelA ? telUpdate : "")
                .telB(updateTelB ? telUpdate : "")
                .cityCode(bindInfoAxb.getCityCode())
                .expireTime(bindInfoAxb.getExpireTime())
                .bindId(bindInfoAxb.getBindId())
                .vccId(vccId)
                .numType(NumberTypeEnum.AXB.name())
                .operateType(OperateTypeEnum.UPDATE.name())
                .build();
        bindInfoAxb.setExpiration(expiration + 10);
        sendAxbBindRecycleInfoToMq(Optional.of(bindRecycleDTO), bindInfoAxb, OperateTypeEnum.UPDATE.name(), vccId, numType);
        if (redisProperties.getCluster2().getActive()) {
            String finalNewAxBindInfoKey = newAxBindInfoKey;
            String finalNewBxBindInfoKey = newBxBindInfoKey;
            otherExecutor.execute(() -> {
                try {
                    RBatch batch2 = redissonClient2.createBatch();
                    batch2.getBucket(axbBindIdKey).setAsync(bindIdKeyJson, expiration, TimeUnit.SECONDS);
                    if (StrUtil.isNotEmpty(finalNewAxBindInfoKey)) {
                        batch2.getBucket(axBindInfoKey).deleteAsync();
                        batch2.getBucket(finalNewAxBindInfoKey).setAsync(bindInfoJson, expiration, TimeUnit.SECONDS);
                        batch2.getBucket(bxBindInfoKey).setAsync(bindInfoJson, expiration, TimeUnit.SECONDS);

                    }
                    if (StrUtil.isNotEmpty(finalNewBxBindInfoKey)) {
                        batch2.getBucket(bxBindInfoKey).deleteAsync();
                        batch2.getBucket(finalNewBxBindInfoKey).setAsync(bindInfoJson, expiration, TimeUnit.SECONDS);
                        batch2.getBucket(axBindInfoKey).setAsync(bindInfoJson, expiration, TimeUnit.SECONDS);
                    }
                    batch2.execute();
                } catch (Exception e) {
                    log.error("update tel, bindId: {}, 异地redis操作异常: {}", bindId, e.getMessage());
                }
            });
        }

        return Result.okByAreaCode(areaCode);
    }


    /**
     * AXB 回收号码推mq
     */
    private void sendAxbBindRecycleInfoToMq(Optional<BindRecycleDTO> bindRecycleDtoOptional, PrivateBindInfoAxb bindInfoAxb,
                                            String operateType, String vccId, String numType) {

        if (!bindRecycleDtoOptional.isPresent() && !OperateTypeEnum.DELETE.name().equals(operateType)) {

            BindRecycleDTO recycleDTO = axbBindConverter.bindInfoAxb2BindRecycleDTO(bindInfoAxb);
            recycleDTO.setNumType(numType);
            recycleDTO.setOperateType(operateType);
            bindRecycleDtoOptional = Optional.of(recycleDTO);
        }
        MqBindInfoBO mqBindInfoBO = MqBindInfoBO.builder()
                .operateType(operateType)
                .numType(numType)
                .vccId(vccId)
                .privateBindInfoAxb(bindInfoAxb)
                .build();

        //  解绑事件推送
        if (OperateTypeEnum.DELETE.name().equals(operateType)) {
            bindPushService.pushUnBind(mqBindInfoBO);
        }

        saveBindService.saveBind(bindRecycleDtoOptional, Convert.toInt(bindInfoAxb.getExpiration()), mqBindInfoBO);
    }

}
