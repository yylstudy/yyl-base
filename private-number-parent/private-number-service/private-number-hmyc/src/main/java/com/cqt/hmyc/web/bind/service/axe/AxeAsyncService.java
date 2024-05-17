package com.cqt.hmyc.web.bind.service.axe;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.cqt.common.enums.BusinessTypeEnum;
import com.cqt.common.enums.CallbackFlagEnum;
import com.cqt.common.enums.ErrorCodeEnum;
import com.cqt.common.enums.OperateTypeEnum;
import com.cqt.common.util.PrivateCacheUtil;
import com.cqt.hmyc.web.bind.service.push.BindPushService;
import com.cqt.hmyc.web.bind.service.recycle.SaveBindService;
import com.cqt.hmyc.web.bind.service.recycle.db.DbOperationStrategyManager;
import com.cqt.hmyc.web.cache.NumberPoolAxeCache;
import com.cqt.model.bind.axe.dto.AxeBindIdKeyInfoDTO;
import com.cqt.model.bind.axe.entity.PrivateBindInfoAxe;
import com.cqt.model.bind.axe.vo.AxeBindingVO;
import com.cqt.model.bind.bo.MqBindInfoBO;
import com.cqt.model.bind.dto.BindRecycleDTO;
import com.cqt.model.bind.dto.UnBindDTO;
import com.cqt.model.bind.dto.UpdateExpirationDTO;
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
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * @author linshiqiang
 * @date 2022/2/16 13:51
 */
@Service
@Slf4j
public class AxeAsyncService {

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
    private AxeBindConverter axeBindConverter;

    @Resource
    private SaveBindService saveBindService;

    @Resource
    private BindPushService bindPushService;

    @Resource
    private DbOperationStrategyManager dbOperationStrategyManager;

    @Resource
    private HideProperties hideProperties;

    /**
     * AXE保存绑定关系
     */
    public AxeBindingVO saveBindInfo(PrivateBindInfoAxe bindInfo, PrivateCorpBusinessInfoDTO businessInfoDTO, String type) {
        String requestId = bindInfo.getRequestId();
        String vccId = bindInfo.getVccId();
        String usableTelX = bindInfo.getTelX();
        String bindId = bindInfo.getBindId();
        String usableExt = bindInfo.getTelXExt();
        // 已绑定的关系string:
        String bindIdKey = PrivateCacheUtil.getBindIdKey(vccId, type, bindId);
        // 已请求绑定的关系string
        String requestIdKey = PrivateCacheUtil.getRequestIdKey(vccId, type, requestId);
        // X号码和分机号的绑定关系string:
        String extBindInfoKey = PrivateCacheUtil.getExtBindInfoKey(vccId, type, usableTelX, usableExt);

        bindInfo.setType(ObjectUtil.isNull(bindInfo.getType()) ? businessInfoDTO.getSmsFlag() : bindInfo.getType());
        bindInfo.setEnableRecord(ObjectUtil.isNull(bindInfo.getEnableRecord()) ? businessInfoDTO.getRecordFlag() : bindInfo.getEnableRecord());
        bindInfo.setCreateTime(DateUtil.date());
        String bindInfoJson = JSON.toJSONString(bindInfo);
        long expiration = bindInfo.getExpiration();
        long extBindExpire = getExtBindExpire(expiration);
        AxeBindingVO axeBindingVO = axeBindConverter.bindInfo2BindingVO(bindInfo);
        String requestJson = JSON.toJSONString(axeBindingVO);
        AxeBindIdKeyInfoDTO bindIdKeyInfoDTO = axeBindConverter.bindInfo2BindIdKeyInfoDTO(bindInfo);
        String bindIdKeyJson = JSON.toJSONString(bindIdKeyInfoDTO);
        RBatch batch = redissonClient.createBatch();
        batch.getBucket(bindIdKey).setAsync(bindIdKeyJson, expiration, TimeUnit.SECONDS);
        batch.getBucket(requestIdKey).setAsync(requestJson, extBindExpire, TimeUnit.SECONDS);
        batch.getBucket(extBindInfoKey).setAsync(bindInfoJson, extBindExpire, TimeUnit.SECONDS);
        BatchResult<?> batchResult = batch.execute();
        log.info("AXE设置绑定, vccId: {}, bindId: {}, redis执行结果: {}", vccId, bindId, batchResult.getResponses());

        if (redisProperties.getCluster2().getActive()) {
            otherExecutor.execute(() -> {
                try {
                    RBatch batch2 = redissonClient2.createBatch();
                    batch2.getBucket(bindIdKey).setAsync(bindIdKeyJson, expiration, TimeUnit.SECONDS);
                    batch2.getBucket(requestIdKey).setAsync(requestJson, extBindExpire, TimeUnit.SECONDS);
                    batch2.getBucket(extBindInfoKey).setAsync(bindInfoJson, extBindExpire, TimeUnit.SECONDS);
                    batch2.execute();
                } catch (Exception e) {
                    log.error("AXE 设置绑定异地redis操作异常: ", e);
                }
            });
        }
        sendBindRecycleInfoToMq(Optional.empty(), bindInfo, OperateTypeEnum.INSERT.name(), vccId, type);
        return axeBindingVO;
    }

    private long getExtBindExpire(long expiration) {
        long extBindExpire = expiration;
        if (Boolean.TRUE.equals(hideProperties.getSwitchs().getAxeCustomExpire())) {
            Duration axeBindExpire = hideProperties.getTimeout().getAxeBindExpire();
            if (Objects.nonNull(axeBindExpire)) {
                extBindExpire = Math.min(expiration, axeBindExpire.getSeconds());
            }
        }
        return extBindExpire;
    }

    /**
     * axe 解绑
     */
    public void unbind(AxeBindIdKeyInfoDTO bindIdKeyInfoDTO, UnBindDTO unBindDTO, String type) {
        String vccId = unBindDTO.getVccId();
        String bindId = unBindDTO.getBindId();
        String areaCode = bindIdKeyInfoDTO.getAreaCode();
        String cityCode = bindIdKeyInfoDTO.getCityCode();
        String telX = bindIdKeyInfoDTO.getTelX();
        String extNum = bindIdKeyInfoDTO.getTelXExt();
        String requestId = bindIdKeyInfoDTO.getRequestId();
        String bindIdKey = PrivateCacheUtil.getBindIdKey(vccId, type, bindId);
        String requestIdKey = PrivateCacheUtil.getRequestIdKey(vccId, type, requestId);
        String extBindInfoKey = PrivateCacheUtil.getExtBindInfoKey(vccId, type, telX, extNum);
        // 未使用分机号set
        String usableExtPoolListKey = PrivateCacheUtil.getUsableExtPoolListKey(vccId, type, cityCode, telX);

        Optional<List<String>> poolOptional = NumberPoolAxeCache.getPool(vccId, areaCode);

        RBatch batch = redissonClient.createBatch();
        batch.getBucket(bindIdKey).deleteAsync();
        batch.getBucket(requestIdKey).deleteAsync();
        batch.getBucket(extBindInfoKey).deleteAsync();
        if (poolOptional.isPresent()) {
            List<String> pool = poolOptional.get();
            if (pool.contains(telX)) {
                batch.getDeque(usableExtPoolListKey).addLastAsync(extNum);
            }
        }
        BatchResult<?> batchResult = batch.execute();
        log.info("AXE解绑, vccId: {}, bindId: {}, redis执行结果: {}", vccId, bindId, batchResult.getResponses());

        if (redisProperties.getCluster2().getActive()) {
            otherExecutor.execute(() -> {
                try {
                    RBatch batch2 = redissonClient2.createBatch();
                    batch2.getBucket(bindIdKey).deleteAsync();
                    batch2.getBucket(requestIdKey).deleteAsync();
                    batch2.getBucket(extBindInfoKey).deleteAsync();
                    if (poolOptional.isPresent()) {
                        List<String> pool = poolOptional.get();
                        if (pool.contains(telX)) {
                            batch2.getDeque(usableExtPoolListKey).addLastAsync(extNum);
                        }
                    }
                    batch2.execute();
                } catch (Exception e) {
                    log.error("AXE解绑异地redis操作异常: ", e);
                }
            });
        }
        PrivateBindInfoAxe bindInfo = axeBindConverter.bindIdKeyInfoDTO2bindInfo(bindIdKeyInfoDTO);
        sendBindRecycleInfoToMq(Optional.empty(), bindInfo, OperateTypeEnum.DELETE.name(), vccId, type);
    }

    /**
     * axe 修改有效期
     */
    public Result updateExpirationBind(AxeBindIdKeyInfoDTO bindIdKeyInfoDTO, UpdateExpirationDTO updateExpirationDTO, String type) {
        String vccId = updateExpirationDTO.getVccId();
        String requestId = bindIdKeyInfoDTO.getRequestId();
        String bindId = updateExpirationDTO.getBindId();
        String telX = bindIdKeyInfoDTO.getTelX();
        String extNum = bindIdKeyInfoDTO.getTelXExt();
        long expiration = updateExpirationDTO.getExpiration();
        // 已绑定的关系string:
        String bindIdKey = PrivateCacheUtil.getBindIdKey(vccId, type, bindId);
        // 已请求绑定的关系string
        String requestIdKey = PrivateCacheUtil.getRequestIdKey(vccId, type, requestId);
        // X号码和分机号的绑定关系string:
        String extBindInfoKey = PrivateCacheUtil.getExtBindInfoKey(vccId, type, telX, extNum);
        String axBindInfoJson = redissonUtil.getString(extBindInfoKey);
        if (StrUtil.isBlank(axBindInfoJson)) {
            log.warn("AXE修改有效期, vccId: {}, bindId: {}, XE绑定关系不存在", vccId, bindId);
            return Result.fail(ErrorCodeEnum.NOT_BIND.getCode(), ErrorCodeEnum.NOT_BIND.getMessage());
        }
        DateTime expireTime = DateUtil.offset(DateUtil.date(), DateField.SECOND, Convert.toInt(updateExpirationDTO.getExpiration()));
        PrivateBindInfoAxe bindInfo = JSON.parseObject(axBindInfoJson, PrivateBindInfoAxe.class);
        bindInfo.setExpireTime(expireTime);
        bindInfo.setExpiration(updateExpirationDTO.getExpiration());
        String newBindInfoJson = JSON.toJSONString(bindInfo);
        long extBindExpire = getExtBindExpire(expiration);

        RBatch batch = redissonClient.createBatch();
        batch.getBucket(bindIdKey).expireAsync(expiration, TimeUnit.SECONDS);
        batch.getBucket(requestIdKey).expireAsync(extBindExpire, TimeUnit.SECONDS);
        batch.getBucket(extBindInfoKey).setAsync(newBindInfoJson, extBindExpire, TimeUnit.SECONDS);
        BatchResult<?> batchResult = batch.execute();
        log.info("AXE修改有效期, vccId: {}, bindId: {}, redis执行结果: {}", vccId, bindId, batchResult.getResponses());

        if (redisProperties.getCluster2().getActive()) {
            otherExecutor.execute(() -> {
                try {
                    RBatch batch2 = redissonClient2.createBatch();
                    batch2.getBucket(bindIdKey).expireAsync(expiration, TimeUnit.SECONDS);
                    batch2.getBucket(requestIdKey).expireAsync(extBindExpire, TimeUnit.SECONDS);
                    batch2.getBucket(extBindInfoKey).setAsync(newBindInfoJson, extBindExpire, TimeUnit.SECONDS);
                    batch2.execute();
                } catch (Exception e) {
                    log.error("AXE修改有效期异地redis操作异常: ", e);
                }
            });
        }
        sendBindRecycleInfoToMq(Optional.empty(), bindInfo, OperateTypeEnum.UPDATE.name(), vccId, type);
        return Result.ok();
    }


    /**
     * AXE 回收号码推mq
     */
    private void sendBindRecycleInfoToMq(Optional<BindRecycleDTO> bindRecycleDtoOptional, PrivateBindInfoAxe bindInfo,
                                         String operateType, String vccId, String type) {

        if (!bindRecycleDtoOptional.isPresent() && !OperateTypeEnum.DELETE.name().equals(operateType)) {

            BindRecycleDTO recycleDTO = axeBindConverter.bindInfo2BindRecycleDTO(bindInfo);
            recycleDTO.setNumType(type);
            recycleDTO.setOperateType(operateType);
            bindRecycleDtoOptional = Optional.of(recycleDTO);
        }
        MqBindInfoBO mqBindInfoBO = MqBindInfoBO.builder()
                .operateType(operateType)
                .vccId(vccId)
                .numType(type)
                .cityCode(bindInfo.getCityCode())
                .privateBindInfoAxe(bindInfo)
                .build();

        if (OperateTypeEnum.DELETE.name().equals(operateType)) {
            bindPushService.pushUnBind(mqBindInfoBO);
        }

        saveBindService.saveBind(bindRecycleDtoOptional, Convert.toInt(bindInfo.getExpiration()), mqBindInfoBO);
    }

    /**
     * 设置回呼B号码
     *
     * @param bindInfo 通过XE查询到的绑定关系
     */
    @Async("saveExecutor")
    public void setupTelB(PrivateBindInfoAxe bindInfo, String telB, String businessType) {
        if (!CallbackFlagEnum.CALLBACK.getCode().equals(bindInfo.getCallbackFlag())) {
            return;
        }
        // callback_flag=1时设置AX回呼绑定关系string
        String vccId = bindInfo.getVccId();
        String axBindInfoKey = PrivateCacheUtil.getBindInfoKey(vccId, businessType, bindInfo.getTel(), bindInfo.getTelX());
        // 查询AX是否存在
        String axBindInfo = redissonUtil.getString(axBindInfoKey);
        if (StrUtil.isNotEmpty(axBindInfo)) {
            // AX不为空, 判断AX的bindId和传来的XE里的绑定关系是否一致, B号码也一致, 说明B打分机号多次, 不需要重复设置
            PrivateBindInfoAxe axBindInfoAxe = JSON.parseObject(axBindInfo, PrivateBindInfoAxe.class);
            if (StrUtil.isNotEmpty(bindInfo.getBindId())
                    && bindInfo.getBindId().equals(axBindInfoAxe.getBindId())
                    && StrUtil.isNotEmpty(bindInfo.getTelB())
                    && bindInfo.getTelB().equals(axBindInfoAxe.getTelB())) {
                return;
            }
        }

        // 更新AX绑定关系, 设置tel_b
        bindInfo.setTelB(telB);
        long callbackExpiration = DateUtil.between(DateUtil.date(), bindInfo.getCallbackExpireTime(), DateUnit.SECOND, false);
        if (callbackExpiration > 0) {
            log.info("AXE, vccId: {}, bindId: {}, 设置AX回呼B: {}", vccId, bindInfo.getBindId(), telB);
            redissonUtil.setObject(axBindInfoKey, JSON.toJSONString(bindInfo), callbackExpiration, TimeUnit.SECONDS);
            // 修改数据库
            MqBindInfoBO mqBindInfoBO = MqBindInfoBO.builder()
                    .operateType(OperateTypeEnum.UPDATE.name())
                    .vccId(vccId)
                    .numType(BusinessTypeEnum.AXE.name())
                    .privateBindInfoAxe(bindInfo)
                    .build();
            dbOperationStrategyManager.operate(mqBindInfoBO);
        }
    }
}
