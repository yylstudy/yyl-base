package com.cqt.hmyc.web.bind.service.ax;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.cqt.common.enums.ErrorCodeEnum;
import com.cqt.common.enums.OperateTypeEnum;
import com.cqt.common.util.PrivateCacheUtil;
import com.cqt.hmyc.web.bind.service.push.BindPushService;
import com.cqt.hmyc.web.bind.service.recycle.SaveBindService;
import com.cqt.hmyc.web.cache.NumberPoolAxCache;
import com.cqt.hmyc.web.corpinfo.service.CorpBusinessService;
import com.cqt.model.bind.ax.dto.AxBindIdKeyInfoDTO;
import com.cqt.model.bind.ax.dto.SetUpTelDTO;
import com.cqt.model.bind.ax.entity.PrivateBindInfoAx;
import com.cqt.model.bind.ax.vo.AxBindingVO;
import com.cqt.model.bind.bo.MqBindInfoBO;
import com.cqt.model.bind.dto.BindRecycleDTO;
import com.cqt.model.bind.dto.UnBindDTO;
import com.cqt.model.bind.dto.UpdateExpirationDTO;
import com.cqt.model.common.Result;
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
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * @author linshiqiang
 * @date 2022/3/15 11:47
 */
@Service
@Slf4j
public class AxAsyncService {


    @Qualifier(value = "redissonClient")
    @Autowired
    private RedissonClient redissonClient;

    @Qualifier(value = "redissonClient2")
    @Autowired(required = false)
    private RedissonClient redissonClient2;

    @Resource
    private RedissonUtil redissonUtil;

    @Resource(name = "otherExecutor")
    private ThreadPoolTaskExecutor otherExecutor;

    @Resource
    private SaveBindService saveBindService;

    @Resource
    private AxBindConverter bindConverter;

    @Resource
    private DoubleRedisProperties redisProperties;

    @Resource
    private BindPushService bindPushService;

    @Resource
    private CorpBusinessService corpBusinessService;

    public AxBindingVO saveBindInfo(PrivateBindInfoAx bindInfoAx, String numType) {
        String vccId = bindInfoAx.getVccId();
        String requestId = bindInfoAx.getRequestId();
        String telX = bindInfoAx.getTelX();
        String bindId = bindInfoAx.getBindId();
        // 已绑定的关系string:
        String bindIdKey = PrivateCacheUtil.getBindIdKey(vccId, numType, bindId);
        // 已请求绑定的关系string
        String requestIdKey = PrivateCacheUtil.getRequestIdKey(vccId, numType, requestId);
        // X 号码绑定关系
        String telxBindInfoKey = PrivateCacheUtil.getTelxBindInfoKey(vccId, numType, telX);

        Optional<PrivateCorpBusinessInfoDTO> businessInfoOptional = corpBusinessService.getCorpBusinessInfo(vccId);
        if (businessInfoOptional.isPresent()) {
            PrivateCorpBusinessInfoDTO businessInfoDTO = businessInfoOptional.get();
            bindInfoAx.setType(ObjectUtil.isNull(bindInfoAx.getType()) ? businessInfoDTO.getSmsFlag() : bindInfoAx.getType());
            bindInfoAx.setEnableRecord(ObjectUtil.isNull(bindInfoAx.getEnableRecord()) ? businessInfoDTO.getRecordFlag() : bindInfoAx.getEnableRecord());
        }
        bindInfoAx.setCreateTime(DateUtil.date());
        String bindInfoJson = JSON.toJSONString(bindInfoAx);
        long expiration = bindInfoAx.getExpiration();

        AxBindingVO bindingVO = bindConverter.bindInfoAx2AxBindingVO(bindInfoAx);
        String requestJson = JSON.toJSONString(bindingVO);
        AxBindIdKeyInfoDTO bindIdKeyInfoDTO = bindConverter.bindInfoAx2AxBindIdKeyInfoDTO(bindInfoAx);
        String bindIdKeyJson = JSON.toJSONString(bindIdKeyInfoDTO);
        RBatch batch = redissonClient.createBatch();
        batch.getBucket(bindIdKey).setAsync(bindIdKeyJson, expiration, TimeUnit.SECONDS);
        batch.getBucket(requestIdKey).setAsync(requestJson, expiration, TimeUnit.SECONDS);
        batch.getBucket(telxBindInfoKey).setAsync(bindInfoJson, expiration, TimeUnit.SECONDS);
        BatchResult<?> batchResult = batch.execute();
        log.info("AX, requestId: {}, redis保存绑定关系结果: {}", requestId, batchResult.getResponses());

        if (redisProperties.getCluster2().getActive()) {
            otherExecutor.execute(() -> {
                try {
                    RBatch batch2 = redissonClient2.createBatch();
                    batch2.getBucket(bindIdKey).setAsync(bindIdKeyJson, expiration, TimeUnit.SECONDS);
                    batch2.getBucket(requestIdKey).setAsync(requestJson, expiration, TimeUnit.SECONDS);
                    batch2.getBucket(telxBindInfoKey).setAsync(bindInfoJson, expiration, TimeUnit.SECONDS);
                    batch2.execute();
                } catch (Exception e) {
                    log.error("AX, 异地redis操作异常: {}", e.getMessage());
                }
            });
        }
        sendAxBindRecycleInfoToMq(Optional.empty(), bindInfoAx, OperateTypeEnum.INSERT.name(), vccId, numType, true);
        return bindingVO;
    }


    /**
     * AXE 回收号码推mq
     */
    private void sendAxBindRecycleInfoToMq(Optional<BindRecycleDTO> bindRecycleDtoOptional, PrivateBindInfoAx bindInfoAx,
                                           String operateType, String vccId, String type, Boolean pushMq) {

        if (pushMq && !bindRecycleDtoOptional.isPresent() && !OperateTypeEnum.DELETE.name().equals(operateType)) {

            BindRecycleDTO recycleDTO = bindConverter.bindInfoAx2BindRecycleDTO(bindInfoAx);
            recycleDTO.setNumType(type);
            recycleDTO.setOperateType(operateType);
            bindRecycleDtoOptional = Optional.of(recycleDTO);
        }
        MqBindInfoBO mqBindInfoBO = MqBindInfoBO.builder()
                .operateType(operateType)
                .vccId(vccId)
                .numType(type)
                .privateBindInfoAx(bindInfoAx)
                .build();

        if (OperateTypeEnum.DELETE.name().equals(operateType)) {
            bindPushService.pushUnBind(mqBindInfoBO);
        }

        saveBindService.saveBind(bindRecycleDtoOptional, Convert.toInt(bindInfoAx.getExpiration()), mqBindInfoBO);
    }

    /**
     * 解绑
     */
    public void unbind(AxBindIdKeyInfoDTO bindIdKeyInfoDTO, UnBindDTO unBindDTO, String numType) {

        String bindId = unBindDTO.getBindId();
        String vccId = unBindDTO.getVccId();
        String requestId = bindIdKeyInfoDTO.getRequestId();
        String telX = bindIdKeyInfoDTO.getTelX();
        String cityCode = bindIdKeyInfoDTO.getCityCode();
        // 已绑定的关系string:
        String bindIdKey = PrivateCacheUtil.getBindIdKey(vccId, numType, bindId);
        // 已请求绑定的关系string
        String requestIdKey = PrivateCacheUtil.getRequestIdKey(vccId, numType, requestId);
        // X 号码绑定关系
        String telxBindInfoKey = PrivateCacheUtil.getTelxBindInfoKey(vccId, numType, telX);
        // 未使用X号码
        String axUsablePoolKey = PrivateCacheUtil.getAxUsablePoolKey(vccId, numType, cityCode);

        RBatch batch = redissonClient.createBatch();
        batch.getBucket(bindIdKey).deleteAsync();
        batch.getBucket(requestIdKey).deleteAsync();
        batch.getBucket(telxBindInfoKey).deleteAsync();
        Optional<List<String>> poolOptional = NumberPoolAxCache.getPool(vccId, cityCode);
        if (poolOptional.isPresent()) {
            List<String> numPool = poolOptional.get();
            if (numPool.contains(telX)) {
                batch.getSet(axUsablePoolKey).addAsync(telX);
            }
        }
        BatchResult<?> batchResult = batch.execute();
        log.info("AX, requestId: {}, redis解除绑定关系结果: {}", bindIdKeyInfoDTO.getRequestId(), batchResult.getResponses());

        if (redisProperties.getCluster2().getActive()) {
            otherExecutor.execute(() -> {
                try {
                    RBatch batch2 = redissonClient2.createBatch();
                    batch2.getBucket(bindIdKey).deleteAsync();
                    batch2.getBucket(requestIdKey).deleteAsync();
                    batch2.getBucket(telxBindInfoKey).deleteAsync();
                    if (poolOptional.isPresent()) {
                        List<String> numPool = poolOptional.get();
                        if (numPool.contains(telX)) {
                            batch2.getSet(axUsablePoolKey).addAsync(telX);
                        }
                    }
                    batch2.execute();
                } catch (Exception e) {
                    log.error("AX 异地redis操作异常: {}", e.getMessage());
                }
            });
        }
        PrivateBindInfoAx bindInfoAx = bindConverter.bindIdKeyInfoDTO2BindInfoAx(bindIdKeyInfoDTO);
        sendAxBindRecycleInfoToMq(Optional.empty(), bindInfoAx, OperateTypeEnum.DELETE.name(), vccId, numType, false);
    }

    /**
     * 修改有效期
     */
    public Result updateExpiration(AxBindIdKeyInfoDTO bindIdKeyInfoDTO, UpdateExpirationDTO updateExpirationDTO, String numType) {
        String bindId = updateExpirationDTO.getBindId();
        String vccId = updateExpirationDTO.getVccId();
        String requestId = bindIdKeyInfoDTO.getRequestId();
        String telX = bindIdKeyInfoDTO.getTelX();
        // 已绑定的关系string:
        String bindIdKey = PrivateCacheUtil.getBindIdKey(vccId, numType, bindId);
        // 已请求绑定的关系string
        String requestIdKey = PrivateCacheUtil.getRequestIdKey(vccId, numType, requestId);
        // X 号码绑定关系
        String telxBindInfoKey = PrivateCacheUtil.getTelxBindInfoKey(vccId, numType, telX);

        String bindInfoJson = redissonUtil.getString(telxBindInfoKey);
        if (StrUtil.isBlank(bindInfoJson)) {
            return Result.fail(ErrorCodeEnum.NOT_BIND.getCode(), ErrorCodeEnum.NOT_BIND.getMessage());
        }
        long expiration = updateExpirationDTO.getExpiration();
        DateTime expireTime = DateUtil.offset(DateUtil.date(), DateField.SECOND, Convert.toInt(expiration));
        PrivateBindInfoAx bindInfoAx = JSON.parseObject(bindInfoJson, PrivateBindInfoAx.class);
        bindInfoAx.setExpireTime(expireTime);
        bindInfoAx.setExpiration(updateExpirationDTO.getExpiration());
        String newBindInfoJson = JSON.toJSONString(bindInfoAx);

        RBatch batch = redissonClient.createBatch();
        batch.getBucket(bindIdKey).expireAsync(expiration, TimeUnit.SECONDS);
        batch.getBucket(requestIdKey).expireAsync(expiration, TimeUnit.SECONDS);
        batch.getBucket(telxBindInfoKey).setAsync(newBindInfoJson, expiration, TimeUnit.SECONDS);
        BatchResult<?> batchResult = batch.execute();
        log.info("AX requestId: {}, redis延长绑定关系结果: {}", bindIdKeyInfoDTO.getRequestId(), batchResult.getResponses());

        if (redisProperties.getCluster2().getActive()) {
            otherExecutor.execute(() -> {
                try {
                    RBatch batch2 = redissonClient2.createBatch();
                    batch2.getBucket(bindIdKey).expireAsync(expiration, TimeUnit.SECONDS);
                    batch2.getBucket(requestIdKey).expireAsync(expiration, TimeUnit.SECONDS);
                    batch2.getBucket(telxBindInfoKey).setAsync(newBindInfoJson, expiration, TimeUnit.SECONDS);
                    batch2.execute();
                } catch (Exception e) {
                    log.error("AX 异地redis操作异常: {}", e.getMessage());
                }
            });
        }

        sendAxBindRecycleInfoToMq(Optional.empty(), bindInfoAx, OperateTypeEnum.UPDATE.name(), vccId, numType, true);
        return Result.ok();
    }

    /**
     * 设置B号码
     */
    public void setupTelB(AxBindIdKeyInfoDTO bindIdKeyInfoDTO, SetUpTelDTO setUpTelB, String numType) {
        String vccId = setUpTelB.getVccId();
        String telX = bindIdKeyInfoDTO.getTelX();
        // X 号码绑定关系
        String telxBindInfoKey = PrivateCacheUtil.getTelxBindInfoKey(vccId, numType, telX);

        String bindInfoJson = redissonUtil.getString(telxBindInfoKey);
        if (StrUtil.isBlank(bindInfoJson)) {
            return;
        }
        PrivateBindInfoAx bindInfoAx = JSON.parseObject(bindInfoJson, PrivateBindInfoAx.class);
        Long expiration = bindInfoAx.getExpiration();
        bindInfoAx.setTelB(setUpTelB.getTelB());
        String newBindInfoJson = JSON.toJSONString(bindInfoAx);
        redissonUtil.setObject(telxBindInfoKey,newBindInfoJson, expiration, TimeUnit.SECONDS);
        sendAxBindRecycleInfoToMq(Optional.empty(), bindInfoAx, OperateTypeEnum.UPDATE.name(), vccId, numType, false);
    }
}
