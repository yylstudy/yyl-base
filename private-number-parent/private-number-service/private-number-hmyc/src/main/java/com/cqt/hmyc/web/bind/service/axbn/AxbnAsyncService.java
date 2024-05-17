package com.cqt.hmyc.web.bind.service.axbn;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.cqt.common.enums.ModeEnum;
import com.cqt.common.enums.OperateTypeEnum;
import com.cqt.common.util.PrivateCacheUtil;
import com.cqt.hmyc.web.bind.service.push.BindPushService;
import com.cqt.hmyc.web.bind.service.recycle.SaveBindService;
import com.cqt.hmyc.web.corpinfo.service.CorpBusinessService;
import com.cqt.model.bind.axbn.dto.AxbnBindIdKeyInfoDTO;
import com.cqt.model.bind.axbn.entity.PrivateBindInfoAxbn;
import com.cqt.model.bind.axbn.vo.AxbnBindingVO;
import com.cqt.model.bind.bo.MqBindInfoBO;
import com.cqt.model.bind.dto.BindRecycleDTO;
import com.cqt.model.bind.dto.UnBindDTO;
import com.cqt.model.bind.dto.UpdateExpirationDTO;
import com.cqt.model.bind.dto.UpdateTelBindDTO;
import com.cqt.model.common.Result;
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
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @author linshiqiang
 * @date 2022/3/22 14:47
 */
@Service
@Slf4j
public class AxbnAsyncService {

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
    private AxbnBindConverter bindConverter;

    @Resource
    private BindPushService bindPushService;

    @Resource
    private CorpBusinessService corpBusinessService;

    @Resource
    private SaveBindService saveBindService;

    /**
     * 保存绑定
     */
    public void saveBinding(PrivateBindInfoAxbn bindInfoAxbn, String numType, Map<String, String> otherMap) {
        String vccId = bindInfoAxbn.getVccId();
        String bindId = bindInfoAxbn.getBindId();
        String requestId = bindInfoAxbn.getRequestId();
        String telA = bindInfoAxbn.getTelA();
        String telB = bindInfoAxbn.getTelB();
        String telX = bindInfoAxbn.getTelX();
        long expiration = bindInfoAxbn.getExpiration();

        // 已绑定的关系string
        String bindIdKey = PrivateCacheUtil.getBindIdKey(vccId, numType, bindId);
        // 已请求绑定的关系string
        String requestIdKey = PrivateCacheUtil.getRequestIdKey(vccId, numType, requestId);
        // ax bx
        String axBindInfoKey = PrivateCacheUtil.getBindInfoKey(vccId, numType, telA, telX);
        String bxBindInfoKey = PrivateCacheUtil.getBindInfoKey(vccId, numType, telB, telX);

        AxbnBindingVO bindingVO = bindConverter.bindInfoAxbn2AxbnBindingVO(bindInfoAxbn);
        String bindingVoJson = JSON.toJSONString(bindingVO);
        AxbnBindIdKeyInfoDTO bindIdKeyInfoDTO = bindConverter.bindInfoAxbn2AxbnBindIdKeyInfoDTO(bindInfoAxbn);
        String bindIdKeyInfoDtoJson = JSON.toJSONString(bindIdKeyInfoDTO);
        bindInfoAxbn.setDisplayNumber(telX);
        String bindInfoAxbnJson = JSON.toJSONString(bindInfoAxbn);

        RBatch batch = redissonClient.createBatch();
        batch.getBucket(bindIdKey).setAsync(bindIdKeyInfoDtoJson, expiration, TimeUnit.SECONDS);
        batch.getBucket(requestIdKey).setAsync(bindingVoJson, expiration, TimeUnit.SECONDS);
        batch.getBucket(axBindInfoKey).setAsync(bindInfoAxbnJson, expiration, TimeUnit.SECONDS);
        batch.getBucket(bxBindInfoKey).setAsync(bindInfoAxbnJson, expiration, TimeUnit.SECONDS);
        if (CollUtil.isNotEmpty(otherMap)) {
            // key: other_b,  value: y号码
            for (Map.Entry<String, String> entry : otherMap.entrySet()) {
                String bNum = entry.getKey();
                String yNum = entry.getValue();
                String otherBxBindInfoKey = PrivateCacheUtil.getBindInfoKey(vccId, numType, bNum, telX);
                bindInfoAxbn.setDisplayNumber(yNum);
                String otherBindInfoAxbnJson = JSON.toJSONString(bindInfoAxbn);
                batch.getBucket(otherBxBindInfoKey).setAsync(otherBindInfoAxbnJson, expiration, TimeUnit.SECONDS);
                if (ModeEnum.ALL_B.getCode().equals(bindInfoAxbn.getMode())) {
                    String otherAyBindInfoKey = PrivateCacheUtil.getBindInfoKey(vccId, numType, telA, yNum);
                    batch.getBucket(otherAyBindInfoKey).setAsync(bindInfoAxbnJson, expiration, TimeUnit.SECONDS);
                }
            }
        }
        BatchResult<?> batchResult = batch.execute();
        log.info("save axbn: {}, bind info result: {}", requestId, batchResult.getResponses());

        if (redisProperties.getCluster2().getActive()) {
            otherExecutor.execute(() -> {
                try {
                    RBatch batch2 = redissonClient2.createBatch();
                    batch2.getBucket(bindIdKey).setAsync(bindIdKeyInfoDtoJson, expiration, TimeUnit.SECONDS);
                    batch2.getBucket(requestIdKey).setAsync(bindingVoJson, expiration, TimeUnit.SECONDS);
                    batch2.getBucket(axBindInfoKey).setAsync(bindInfoAxbnJson, expiration, TimeUnit.SECONDS);
                    batch2.getBucket(bxBindInfoKey).setAsync(bindInfoAxbnJson, expiration, TimeUnit.SECONDS);
                    if (CollUtil.isNotEmpty(otherMap)) {
                        // key: other_b,  value: y号码
                        for (Map.Entry<String, String> entry : otherMap.entrySet()) {
                            String bNum = entry.getKey();
                            String yNum = entry.getValue();
                            String otherBxBindInfoKey = PrivateCacheUtil.getBindInfoKey(vccId, numType, bNum, telX);
                            bindInfoAxbn.setDisplayNumber(yNum);
                            String otherBindInfoAxbnJson = JSON.toJSONString(bindInfoAxbn);
                            batch2.getBucket(otherBxBindInfoKey).setAsync(otherBindInfoAxbnJson, expiration, TimeUnit.SECONDS);
                            if (ModeEnum.ALL_B.getCode().equals(bindInfoAxbn.getMode())) {
                                String otherAyBindInfoKey = PrivateCacheUtil.getBindInfoKey(vccId, numType, telA, yNum);
                                batch2.getBucket(otherAyBindInfoKey).setAsync(bindInfoAxbnJson, expiration, TimeUnit.SECONDS);
                            }
                        }
                    }
                    BatchResult<?> batchResult2 = batch2.execute();
                    log.info("other save axbn: {}, bind info result: {}", requestId, batchResult2.getResponses());
                } catch (Exception e) {
                    log.error("request: {}, 异地redis异常: {}", requestId, e.getMessage());
                }
            });
        }
        sendBindRecycleInfoToMq(Optional.empty(), bindInfoAxbn, OperateTypeEnum.INSERT.name(), vccId, numType);
    }

    /**
     * AXBN 回收号码推mq
     */
    private void sendBindRecycleInfoToMq(Optional<BindRecycleDTO> bindRecycleDtoOptional, PrivateBindInfoAxbn bindInfoAxbn,
                                         String operateType, String vccId, String numType) {

        if (!bindRecycleDtoOptional.isPresent() && !OperateTypeEnum.DELETE.name().equals(operateType)) {

            BindRecycleDTO recycleDTO = bindConverter.bindInfoAxbn2BindRecycleDTO(bindInfoAxbn);
            recycleDTO.setNumType(numType);
            recycleDTO.setOperateType(operateType);
            bindRecycleDtoOptional = Optional.of(recycleDTO);
        }
        MqBindInfoBO mqBindInfoBO = MqBindInfoBO.builder()
                .operateType(operateType)
                .numType(numType)
                .vccId(vccId)
                .privateBindInfoAxbn(bindInfoAxbn)
                .build();

        // 解绑事件推送
        if (OperateTypeEnum.DELETE.name().equals(operateType)) {
            bindPushService.pushUnBind(mqBindInfoBO);
        }

        saveBindService.saveBind(bindRecycleDtoOptional, Convert.toInt(bindInfoAxbn.getExpiration()), mqBindInfoBO);
    }

    /**
     * 解绑
     */
    public void unbind(AxbnBindIdKeyInfoDTO bindIdKeyInfoDTO, UnBindDTO unBindDTO, String numType) {
        String vccId = unBindDTO.getVccId();
        String bindId = bindIdKeyInfoDTO.getBindId();
        String requestId = bindIdKeyInfoDTO.getRequestId();
        String telA = bindIdKeyInfoDTO.getTelA();
        String telB = bindIdKeyInfoDTO.getTelB();
        String telX = bindIdKeyInfoDTO.getTelX();
        String otherBy = bindIdKeyInfoDTO.getOtherBy();
        String cityCode = bindIdKeyInfoDTO.getCityCode();
        Integer mode = bindIdKeyInfoDTO.getMode();

        // 已绑定的关系string
        String bindIdKey = PrivateCacheUtil.getBindIdKey(vccId, numType, bindId);
        // 已请求绑定的关系string
        String requestIdKey = PrivateCacheUtil.getRequestIdKey(vccId, numType, requestId);
        // ax bx
        String axBindInfoKey = PrivateCacheUtil.getBindInfoKey(vccId, numType, telA, telX);
        String bxBindInfoKey = PrivateCacheUtil.getBindInfoKey(vccId, numType, telB, telX);

        // x号码 和y号码
        List<String> xList = new ArrayList<>();
        xList.add(telX);

        // 真实号码 已用X号码池键列表
        List<Object> keyList = new ArrayList<>();
        keyList.add(PrivateCacheUtil.getUsedPoolSlotKey(vccId, numType, cityCode, telA));
        keyList.add(PrivateCacheUtil.getUsedPoolSlotKey(vccId, numType, cityCode, telB));

        BatchResult<?> batchResult = getUnbindBatchResult(numType, vccId, telA, telX, otherBy, cityCode, mode, bindIdKey,
                requestIdKey, axBindInfoKey, bxBindInfoKey, xList, keyList, redissonClient);
        log.info("AXBN, bindId: {}, redis unbind result: {}", bindId, batchResult.getResponses());
        if (redisProperties.getCluster2().getActive()) {
            otherExecutor.execute(() -> {
                try {
                    BatchResult<?> batchResult2 = getUnbindBatchResult(numType, vccId, telA, telX, otherBy, cityCode, mode, bindIdKey,
                            requestIdKey, axBindInfoKey, bxBindInfoKey, xList, keyList, redissonClient2);
                    log.info("AXBN, other bindId: {}, redis unbind result: {}", bindId, batchResult2.getResponses());
                } catch (Exception e) {
                    log.error("AXBN 异地redis操作异常: {}", e.getMessage());
                }
            });
        }
        PrivateBindInfoAxbn bindInfoAxbn = bindConverter.bindIdKeyInfoDTO2BindInfoAxbn(bindIdKeyInfoDTO);
        sendBindRecycleInfoToMq(Optional.empty(), bindInfoAxbn, OperateTypeEnum.DELETE.name(), vccId, numType);
    }

    /**
     * redis 解绑批量操作
     */
    @SuppressWarnings("all")
    private BatchResult<?> getUnbindBatchResult(String numType, String vccId, String telA, String telX, String otherBy, String cityCode,
                                          Integer mode, String bindIdKey, String requestIdKey, String axBindInfoKey,
                                          String bxBindInfoKey, List<String> xList, List<Object> keyList, RedissonClient redissonClient) {
        RBatch batch = redissonClient.createBatch();
        batch.getBucket(bindIdKey).deleteAsync();
        batch.getBucket(requestIdKey).deleteAsync();
        batch.getBucket(axBindInfoKey).deleteAsync();
        batch.getBucket(bxBindInfoKey).deleteAsync();
        if (StrUtil.isNotEmpty(otherBy)) {
            HashMap<String, String> otherMap = JSON.parseObject(otherBy, HashMap.class);
            // key: other_b,  value: y号码
            for (Map.Entry<String, String> entry : otherMap.entrySet()) {
                String bNum = entry.getKey();
                keyList.add(PrivateCacheUtil.getUsedPoolSlotKey(vccId, numType, cityCode, bNum));
                String yNum = entry.getValue();
                xList.add(yNum);
                String otherBxBindInfoKey = PrivateCacheUtil.getBindInfoKey(vccId, numType, bNum, telX);
                batch.getBucket(otherBxBindInfoKey).deleteAsync();
                if (ModeEnum.ALL_B.getCode().equals(mode)) {
                    String otherAyBindInfoKey = PrivateCacheUtil.getBindInfoKey(vccId, numType, telA, yNum);
                    batch.getBucket(otherAyBindInfoKey).deleteAsync();
                }
            }
        }
        for (Object obj : keyList) {
            batch.getSet(obj.toString()).removeAllAsync(xList);
        }
        return batch.execute();
    }


    /**
     * 修改有效期
     */
    @SuppressWarnings("all")
    public Result updateExpiration(AxbnBindIdKeyInfoDTO bindIdKeyInfoDTO, UpdateExpirationDTO updateExpirationDTO, String numType) {
        String bindId = updateExpirationDTO.getBindId();
        String vccId = updateExpirationDTO.getVccId();
        String requestId = bindIdKeyInfoDTO.getRequestId();
        String telX = bindIdKeyInfoDTO.getTelX();
        String telA = bindIdKeyInfoDTO.getTelA();
        String telB = bindIdKeyInfoDTO.getTelB();
        String otherBy = bindIdKeyInfoDTO.getOtherBy();
        long expiration = updateExpirationDTO.getExpiration();
        DateTime expireTime = DateUtil.offset(DateUtil.date(), DateField.SECOND, Convert.toInt(expiration));
        bindIdKeyInfoDTO.setExpiration(expiration);
        bindIdKeyInfoDTO.setExpireTime(expireTime);
        // 已绑定的关系string
        String bindIdKey = PrivateCacheUtil.getBindIdKey(vccId, numType, bindId);
        // 已请求绑定的关系string
        String requestIdKey = PrivateCacheUtil.getRequestIdKey(vccId, numType, requestId);
        // ax bx
        String axBindInfoKey = PrivateCacheUtil.getBindInfoKey(vccId, numType, telA, telX);
        String bxBindInfoKey = PrivateCacheUtil.getBindInfoKey(vccId, numType, telB, telX);
        RBatch batch = redissonClient.createBatch();
        batch.getBucket(bindIdKey).setAsync(JSON.toJSONString(bindIdKeyInfoDTO), expiration, TimeUnit.SECONDS);
        batch.getBucket(requestIdKey).expireAsync(expiration, TimeUnit.SECONDS);
        batch.getBucket(axBindInfoKey).expireAsync(expiration, TimeUnit.SECONDS);
        batch.getBucket(bxBindInfoKey).expireAsync(expiration, TimeUnit.SECONDS);

        if (StrUtil.isNotEmpty(otherBy)) {
            HashMap<String, String> otherMap = JSON.parseObject(otherBy, HashMap.class);
            // key: other_b,  value: y号码
            for (Map.Entry<String, String> entry : otherMap.entrySet()) {
                String bNum = entry.getKey();
                String yNum = entry.getValue();
                String otherBxBindInfoKey = PrivateCacheUtil.getBindInfoKey(vccId, numType, bNum, telX);
                batch.getBucket(otherBxBindInfoKey).expireAsync(expiration, TimeUnit.SECONDS);
                if (ModeEnum.ALL_B.getCode().equals(bindIdKeyInfoDTO.getMode())) {
                    String otherAyBindInfoKey = PrivateCacheUtil.getBindInfoKey(vccId, numType, telA, yNum);
                    batch.getBucket(otherAyBindInfoKey).expireAsync(expiration, TimeUnit.SECONDS);
                }
            }
        }
        BatchResult<?> batchResult = batch.execute();
        log.info("AXBN requestId: {}, redis update expiration result: {}", bindId, batchResult.getResponses());

        if (redisProperties.getCluster2().getActive()) {
            otherExecutor.execute(() -> {
                try {
                    RBatch batch2 = redissonClient2.createBatch();
                    batch2.getBucket(bindIdKey).setAsync(JSON.toJSONString(bindIdKeyInfoDTO), expiration, TimeUnit.SECONDS);
                    batch2.getBucket(requestIdKey).expireAsync(expiration, TimeUnit.SECONDS);
                    batch2.getBucket(axBindInfoKey).expireAsync(expiration, TimeUnit.SECONDS);
                    batch2.getBucket(bxBindInfoKey).expireAsync(expiration, TimeUnit.SECONDS);

                    if (StrUtil.isNotEmpty(otherBy)) {
                        HashMap<String, String> otherMap = JSON.parseObject(otherBy, HashMap.class);
                        // key: other_b,  value: y号码
                        for (Map.Entry<String, String> entry : otherMap.entrySet()) {
                            String bNum = entry.getKey();
                            String yNum = entry.getValue();
                            String otherBxBindInfoKey = PrivateCacheUtil.getBindInfoKey(vccId, numType, bNum, telX);
                            batch2.getBucket(otherBxBindInfoKey).expireAsync(expiration, TimeUnit.SECONDS);
                            if (ModeEnum.ALL_B.getCode().equals(bindIdKeyInfoDTO.getMode())) {
                                String otherAyBindInfoKey = PrivateCacheUtil.getBindInfoKey(vccId, numType, telA, yNum);
                                batch.getBucket(otherAyBindInfoKey).expireAsync(expiration, TimeUnit.SECONDS);
                            }
                        }
                    }
                    BatchResult<?> batchResult2 = batch2.execute();
                    log.info("AXBN requestId: {}, other redis update expiration result: {}", bindId, batchResult2.getResponses());
                } catch (Exception e) {
                    log.error("AX 异地redis操作异常: {}", e.getMessage());
                }
            });
        }
        PrivateBindInfoAxbn bindInfoAxbn = bindConverter.bindIdKeyInfoDTO2BindInfoAxbn(bindIdKeyInfoDTO);
        sendBindRecycleInfoToMq(Optional.empty(), bindInfoAxbn, OperateTypeEnum.UPDATE.name(), vccId, numType);
        return Result.ok();
    }

    public void updateTel(AxbnBindIdKeyInfoDTO bindIdKeyInfoDTO, UpdateTelBindDTO updateTelBindDTO, String numType) {
        String bindId = bindIdKeyInfoDTO.getBindId();
        String vccId = bindIdKeyInfoDTO.getVccId();
        String telX = bindIdKeyInfoDTO.getTelX();
        String telA = bindIdKeyInfoDTO.getTelA();
        String telB = bindIdKeyInfoDTO.getTelB();
        String otherTelB = bindIdKeyInfoDTO.getOtherTelB();
        String otherBy = bindIdKeyInfoDTO.getOtherBy();
        String telBinded = updateTelBindDTO.getTelBinded();
        String telUpdate = updateTelBindDTO.getTelUpdate();
        // 已绑定的关系string
        String bindIdKey = PrivateCacheUtil.getBindIdKey(vccId, numType, bindId);
        // ax bx
        String bindInfoKey = PrivateCacheUtil.getBindInfoKey(vccId, numType, telA, telX);
        String bindInfoJson = redissonUtil.getString(bindInfoKey);
        PrivateBindInfoAxbn bindInfoAxbn = JSON.parseObject(bindInfoJson, PrivateBindInfoAxbn.class);

        // 直接删掉ax绑定, 在添加, 不立即回收, 等源绑定过期再回收,
        // 改tel_a

        if (telBinded.equals(telA)) {
            // x和y都要回收
            bindIdKeyInfoDTO.setTelA(telUpdate);
            bindInfoAxbn.setTelA(telUpdate);
        }

        // 改tel_b
        if (telBinded.equals(telB)) {
            bindIdKeyInfoDTO.setTelB(telUpdate);
            bindInfoAxbn.setTelB(telUpdate);
        }

        // 改other_tel_b
        if (otherTelB.contains(telBinded)) {
            // otherTelB
            String newOtherTelB = otherTelB.replace(telBinded, telUpdate);
            bindIdKeyInfoDTO.setOtherTelB(newOtherTelB);
            bindInfoAxbn.setOtherTelB(newOtherTelB);
            // otherBy  转map 还是replace?
            String newOtherBy = otherBy.replace(telBinded, telUpdate);
            bindIdKeyInfoDTO.setOtherBy(newOtherBy);
            bindInfoAxbn.setOtherBy(newOtherBy);
        }

        // 有效期
        long expiration = DateUtil.between(DateUtil.date(), bindInfoAxbn.getExpireTime(), DateUnit.SECOND, true);

        String otherBys = bindIdKeyInfoDTO.getOtherBy();
        // TODO 更新mysql, redis键
        RBatch batch = redissonClient.createBatch();
//        batch.getBucket(bindIdKey).setAsync(bindIdKeyInfoDtoJson, expiration, TimeUnit.SECONDS);
//        batch.getBucket(requestIdKey).setAsync(bindingVoJson, expiration, TimeUnit.SECONDS);
//        batch.getBucket(axBindInfoKey).setAsync(bindInfoAxbnJson, expiration, TimeUnit.SECONDS);
//        batch.getBucket(bxBindInfoKey).setAsync(bindInfoAxbnJson, expiration, TimeUnit.SECONDS);

    }

    private void updateNewTel() {

    }
}
