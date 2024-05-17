package com.cqt.hmyc.web.bind.service.axebn;

import cn.hutool.core.collection.CollUtil;
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
import com.cqt.hmyc.web.cache.NumberPoolAxebnCache;
import com.cqt.hmyc.web.corpinfo.service.CorpBusinessService;
import com.cqt.model.bind.axebn.dto.AxebnBindIdKeyInfoDTO;
import com.cqt.model.bind.axebn.dto.AxebnExtBindInfoDTO;
import com.cqt.model.bind.axebn.entity.PrivateBindInfoAxebn;
import com.cqt.model.bind.axebn.vo.AxebnBindingVO;
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
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * @author linshiqiang
 * @date 2022/3/7 16:10
 */
@Service
@Slf4j
public class AxebnAsyncService {

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
    private AxebnBindConverter axebnBindConverter;

    @Resource
    private DoubleRedisProperties redisProperties;

    @Resource
    private BindPushService bindPushService;

    @Resource
    private CorpBusinessService corpBusinessService;

    @Resource
    private HideProperties hideProperties;

    /**
     * 绑定
     */
    public Object saveBindInfo(PrivateBindInfoAxebn bindInfoAxebn, String numType, List<String> telbList, List<Object> extNumList) {
        String vccId = bindInfoAxebn.getVccId();
        String requestId = bindInfoAxebn.getRequestId();
        String telX = bindInfoAxebn.getTelX();
        String bindId = bindInfoAxebn.getBindId();
        long expiration = bindInfoAxebn.getExpiration();
        bindInfoAxebn.setMaxDuration(hideProperties.getMaxDuration());
        Optional<PrivateCorpBusinessInfoDTO> businessInfoOptional = corpBusinessService.getCorpBusinessInfo(vccId);
        if (businessInfoOptional.isPresent()) {
            PrivateCorpBusinessInfoDTO businessInfoDTO = businessInfoOptional.get();
            bindInfoAxebn.setType(ObjectUtil.isNull(bindInfoAxebn.getType()) ? businessInfoDTO.getSmsFlag() : bindInfoAxebn.getType());
            bindInfoAxebn.setEnableRecord(ObjectUtil.isNull(bindInfoAxebn.getEnableRecord()) ? businessInfoDTO.getRecordFlag() : bindInfoAxebn.getEnableRecord());
        }

        String bindInfoJson = JSON.toJSONString(bindInfoAxebn);
        AxebnBindingVO bindingVO = axebnBindConverter.bindInfoAxebn2bindingVO(bindInfoAxebn);
        String requestJson = JSON.toJSONString(bindingVO);
        AxebnBindIdKeyInfoDTO bindIdKeyInfoDTO = axebnBindConverter.bindInfoAxebn2bindIdKeyInfoDTO(bindInfoAxebn);
        String bindIdKeyJson = JSON.toJSONString(bindIdKeyInfoDTO);

        // 已绑定的关系string:
        String bindIdKey = PrivateCacheUtil.getBindIdKey(vccId, numType, bindId);
        // 已请求绑定的关系string
        String requestIdKey = PrivateCacheUtil.getRequestIdKey(vccId, numType, requestId);
        // X号码和分机号E的绑定关系string


        RBatch batch = redissonClient.createBatch();
        batch.getBucket(bindIdKey).setAsync(bindIdKeyJson, expiration, TimeUnit.SECONDS);
        batch.getBucket(requestIdKey).setAsync(requestJson, expiration, TimeUnit.SECONDS);
        AxebnExtBindInfoDTO axebnExtBindInfoDTO = axebnBindConverter.bindInfoAxebn2AxebnExtBindInfoDTO(bindInfoAxebn);
        int size = telbList.size();
        for (int i = 0; i < size; i++) {
            String telB = telbList.get(i);
            // tel_b和tel_x绑定关系string
            String bindInfoKey = PrivateCacheUtil.getBindInfoKey(vccId, numType, telB, telX);
            batch.getBucket(bindInfoKey).setAsync(bindInfoJson, expiration, TimeUnit.SECONDS);
            if (!StrUtil.isEmptyIfStr(extNumList.get(i))) {
                String ext = extNumList.get(i).toString();
                axebnExtBindInfoDTO.setBindTelB(telB);
                axebnExtBindInfoDTO.setBindExtNum(ext);
                String extBindInfoKey = PrivateCacheUtil.getExtBindInfoKey(vccId, numType, telX, ext);
                batch.getBucket(extBindInfoKey).setAsync(JSON.toJSONString(axebnExtBindInfoDTO), expiration, TimeUnit.SECONDS);
            }
        }
        BatchResult<?> batchResult = batch.execute();
        log.info("{}, requestId: {}, redis保存绑定关系结果: {}", numType, requestId, batchResult.getResponses());
        if (redisProperties.getCluster2().getActive()) {

            otherExecutor.execute(() -> {
                try {
                    RBatch batch2 = redissonClient2.createBatch();
                    batch2.getBucket(bindIdKey).setAsync(bindIdKeyJson, expiration, TimeUnit.SECONDS);
                    batch2.getBucket(requestIdKey).setAsync(requestJson, expiration, TimeUnit.SECONDS);
                    for (int i = 0; i < size; i++) {
                        String telB = telbList.get(i);
                        // tel_b和tel_x绑定关系string
                        String bindInfoKey = PrivateCacheUtil.getBindInfoKey(vccId, numType, telB, telX);
                        batch2.getBucket(bindInfoKey).setAsync(bindInfoJson, expiration, TimeUnit.SECONDS);
                        if (!StrUtil.isEmptyIfStr(extNumList.get(i))) {
                            String ext = extNumList.get(i).toString();
                            axebnExtBindInfoDTO.setBindTelB(telB);
                            axebnExtBindInfoDTO.setBindExtNum(ext);
                            String extBindInfoKey = PrivateCacheUtil.getExtBindInfoKey(vccId, numType, telX, ext);
                            batch2.getBucket(extBindInfoKey).setAsync(JSON.toJSONString(axebnExtBindInfoDTO), expiration, TimeUnit.SECONDS);
                        }
                    }
                    BatchResult<?> batchResult2 = batch2.execute();
                    log.info("{}, requestId: {}, 异地redis保存绑定关系结果: {}", numType, requestId, batchResult2.getResponses());
                } catch (Exception e) {
                    log.error("{} 异地redis操作异常: {}", numType, e.getMessage());
                }
            });
        }
        sendAxebnBindRecycleInfoToMq(Optional.empty(), bindInfoAxebn, OperateTypeEnum.INSERT.name(), vccId, numType);
        return bindingVO;
    }

    /**
     * 解绑
     */
    public void unbind(AxebnBindIdKeyInfoDTO bindIdKeyInfoDTO, UnBindDTO unbindDTO, String numType) {
        String bindId = unbindDTO.getBindId();
        String vccId = unbindDTO.getVccId();
        String requestId = bindIdKeyInfoDTO.getRequestId();
        String telX = bindIdKeyInfoDTO.getTelX();
        String cityCode = bindIdKeyInfoDTO.getCityCode();
        // 已绑定的关系string:
        String bindIdKey = PrivateCacheUtil.getBindIdKey(vccId, numType, bindId);
        // 已请求绑定的关系string
        String requestIdKey = PrivateCacheUtil.getRequestIdKey(vccId, numType, requestId);

        List<String> extNumList = StrUtil.split(bindIdKeyInfoDTO.getTelXExt(), ",");
        List<String> telbList = StrUtil.split(bindIdKeyInfoDTO.getTelB(), ",");

        Optional<ArrayList<String>> poolOptional = NumberPoolAxebnCache.getPool(vccId, cityCode);

        RBatch batch = redissonClient.createBatch();
        batch.getBucket(bindIdKey).deleteAsync();
        batch.getBucket(requestIdKey).deleteAsync();
        int size = telbList.size();
        for (int i = 0; i < size; i++) {
            String telB = telbList.get(i);
            // tel_b和tel_x绑定关系string
            String bindInfoKey = PrivateCacheUtil.getBindInfoKey(vccId, numType, telB, telX);
            batch.getBucket(bindInfoKey).deleteAsync();
            if (!StrUtil.isEmptyIfStr(extNumList.get(i))) {
                String ext = extNumList.get(i);
                String extBindInfoKey = PrivateCacheUtil.getExtBindInfoKey(vccId, numType, telX, ext);
                batch.getBucket(extBindInfoKey).deleteAsync();
            }
            if (poolOptional.isPresent()) {
                ArrayList<String> pool = poolOptional.get();
                if (pool.contains(telX)) {
                    String usablePoolSlotKey = PrivateCacheUtil.getUsablePoolSlotKey(vccId, numType, cityCode, telB);
                    batch.getSet(usablePoolSlotKey).addAsync(telX);
                }
            }
        }
        String poolUnUsedExtKey = PrivateCacheUtil.getUsableExtPoolKey(vccId, numType, telX);
        if (poolOptional.isPresent()) {
            ArrayList<String> pool = poolOptional.get();
            if (pool.contains(telX)) {
                batch.getSet(poolUnUsedExtKey).addAllAsync(extNumList);
            }
        }

        BatchResult<?> batchResult = batch.execute();
        log.info("{}, requestId: {}, redis解绑结果: {}", numType, requestId, batchResult.getResponses());
        if (redisProperties.getCluster2().getActive()) {
            otherExecutor.execute(() -> {
                try {
                    RBatch batch2 = redissonClient2.createBatch();
                    batch2.getBucket(bindIdKey).deleteAsync();
                    batch2.getBucket(requestIdKey).deleteAsync();
                    for (int i = 0; i < size; i++) {
                        String telB = telbList.get(i);
                        // tel_b和tel_x绑定关系string
                        String bindInfoKey = PrivateCacheUtil.getBindInfoKey(vccId, numType, telB, telX);
                        batch2.getBucket(bindInfoKey).deleteAsync();
                        if (!StrUtil.isEmptyIfStr(extNumList.get(i))) {
                            String ext = extNumList.get(i);
                            String extBindInfoKey = PrivateCacheUtil.getExtBindInfoKey(vccId, numType, telX, ext);
                            batch2.getBucket(extBindInfoKey).deleteAsync();
                        }
                        if (poolOptional.isPresent()) {
                            ArrayList<String> pool = poolOptional.get();
                            if (pool.contains(telX)) {
                                String usablePoolSlotKey = PrivateCacheUtil.getUsablePoolSlotKey(vccId, numType, cityCode, telB);
                                batch2.getSet(usablePoolSlotKey).addAsync(telX);
                            }
                        }
                    }
                    if (poolOptional.isPresent()) {
                        ArrayList<String> pool = poolOptional.get();
                        if (pool.contains(telX)) {
                            batch2.getSet(poolUnUsedExtKey).addAllAsync(extNumList);
                        }
                    }

                    BatchResult<?> batchResult2 = batch2.execute();
                    log.info("{}, requestId: {}, 异地redis解绑结果: {}", numType, requestId, batchResult2.getResponses());
                } catch (Exception e) {
                    log.error("{} 异地redis操作异常: {}", numType, e.getMessage());
                }
            });
        }
        PrivateBindInfoAxebn bindInfoAxebn = axebnBindConverter.bindIdKeyInfoDTO2BindInfoAxebn(bindIdKeyInfoDTO);
        sendAxebnBindRecycleInfoToMq(Optional.empty(), bindInfoAxebn, OperateTypeEnum.DELETE.name(), vccId, numType);
    }

    /**
     * 修改有效期
     */
    public Result updateExpirationBind(AxebnBindIdKeyInfoDTO bindIdKeyInfoDTO, UpdateExpirationDTO updateExpirationDTO, String numType) {
        String bindId = updateExpirationDTO.getBindId();
        String vccId = updateExpirationDTO.getVccId();
        String requestId = bindIdKeyInfoDTO.getRequestId();
        String telX = bindIdKeyInfoDTO.getTelX();
        List<String> extNumList = StrUtil.split(bindIdKeyInfoDTO.getTelXExt(), ",");
        List<String> telbList = StrUtil.split(bindIdKeyInfoDTO.getTelB(), ",");
        if (CollUtil.isEmpty(extNumList)) {
            return Result.fail(ErrorCodeEnum.NOT_BIND.getCode(), ErrorCodeEnum.NOT_BIND.getMessage());
        }

        // 已绑定的关系string:
        String bindIdKey = PrivateCacheUtil.getBindIdKey(vccId, numType, bindId);
        // 已请求绑定的关系string
        String requestIdKey = PrivateCacheUtil.getRequestIdKey(vccId, numType, requestId);
        // X号码和分机号的绑定关系string:
        String extBindInfoKey = PrivateCacheUtil.getExtBindInfoKey(vccId, numType, telX, extNumList.get(0));
        String extBindInfoJson = redissonUtil.getString(extBindInfoKey);
        if (StrUtil.isBlank(extBindInfoJson)) {
            return Result.fail(ErrorCodeEnum.NOT_BIND.getCode(), ErrorCodeEnum.NOT_BIND.getMessage());
        }
        long expiration = updateExpirationDTO.getExpiration();
        DateTime expireTime = DateUtil.offset(DateUtil.date(), DateField.SECOND, Convert.toInt(expiration));
        // X-E 绑定关系, 修改expiration和expireTime
        PrivateBindInfoAxebn bindInfoAxebn = JSON.parseObject(extBindInfoJson, PrivateBindInfoAxebn.class);
        bindInfoAxebn.setExpireTime(expireTime);
        bindInfoAxebn.setExpiration(updateExpirationDTO.getExpiration());
        String newExtBindInfoJson = JSON.toJSONString(bindInfoAxebn);

        RBatch batch = redissonClient.createBatch();
        batch.getBucket(bindIdKey).expireAsync(expiration, TimeUnit.SECONDS);
        batch.getBucket(requestIdKey).expireAsync(expiration, TimeUnit.SECONDS);
        int size = telbList.size();
        for (int i = 0; i < size; i++) {
            String telB = telbList.get(i);
            // tel_b和tel_x绑定关系string
            String bindInfoKey = PrivateCacheUtil.getBindInfoKey(vccId, numType, telB, telX);
            batch.getBucket(bindInfoKey).expireAsync(expiration, TimeUnit.SECONDS);
            if (!StrUtil.isEmptyIfStr(extNumList.get(i))) {
                String ext = extNumList.get(i);
                String extBindInfoKey1 = PrivateCacheUtil.getExtBindInfoKey(vccId, numType, telX, ext);
                batch.getBucket(extBindInfoKey1).setAsync(newExtBindInfoJson, expiration, TimeUnit.SECONDS);
            }
        }
        BatchResult<?> batchResult = batch.execute();
        log.info("{}, requestId: {}, redis修改有效期系结果: {}", numType, requestId, batchResult.getResponses());
        if (redisProperties.getCluster2().getActive()) {
            otherExecutor.execute(() -> {
                try {
                    RBatch batch2 = redissonClient2.createBatch();
                    batch2.getBucket(bindIdKey).expireAsync(expiration, TimeUnit.SECONDS);
                    batch2.getBucket(requestIdKey).expireAsync(expiration, TimeUnit.SECONDS);
                    for (int i = 0; i < size; i++) {
                        String telB = telbList.get(i);
                        // tel_b和tel_x绑定关系string
                        String bindInfoKey = PrivateCacheUtil.getBindInfoKey(vccId, numType, telB, telX);
                        batch2.getBucket(bindInfoKey).expireAsync(expiration, TimeUnit.SECONDS);
                        if (!StrUtil.isEmptyIfStr(extNumList.get(i))) {
                            String ext = extNumList.get(i);
                            String extBindInfoKey1 = PrivateCacheUtil.getExtBindInfoKey(vccId, numType, telX, ext);
                            batch2.getBucket(extBindInfoKey1).setAsync(newExtBindInfoJson, expiration, TimeUnit.SECONDS);
                        }
                    }
                    BatchResult<?> batchResult2 = batch2.execute();
                    log.info("{}, requestId: {}, redis修改有效期系结果: {}", numType, requestId, batchResult2.getResponses());
                } catch (Exception e) {
                    log.error("{} 异地redis操作异常: {}", numType, e.getMessage());
                }
            });
        }
        sendAxebnBindRecycleInfoToMq(Optional.empty(), bindInfoAxebn, OperateTypeEnum.UPDATE.name(), vccId, numType);
        return Result.ok();
    }

    /**
     * AXEBN 回收号码推mq
     */
    private void sendAxebnBindRecycleInfoToMq(Optional<BindRecycleDTO> bindRecycleDtoOptional, PrivateBindInfoAxebn bindInfoAxebn,
                                              String operateType, String vccId, String type) {

        if (!bindRecycleDtoOptional.isPresent() && !OperateTypeEnum.DELETE.name().equals(operateType)) {

            BindRecycleDTO recycleDTO = axebnBindConverter.bindInfoAxebn2BindRecycleDTO(bindInfoAxebn);
            recycleDTO.setNumType(type);
            recycleDTO.setOperateType(operateType);
            bindRecycleDtoOptional = Optional.of(recycleDTO);
        }
        MqBindInfoBO mqBindInfoBO = MqBindInfoBO.builder()
                .operateType(operateType)
                .vccId(vccId)
                .numType(type)
                .privateBindInfoAxebn(bindInfoAxebn)
                .build();

        if (OperateTypeEnum.DELETE.name().equals(operateType)) {
            bindPushService.pushUnBind(mqBindInfoBO);
        }

        saveBindService.saveBind(bindRecycleDtoOptional, Convert.toInt(bindInfoAxebn.getExpiration()), mqBindInfoBO);
    }
}
