package com.cqt.hmyc.web.bind.service.axe;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cqt.common.constants.TableNameConstant;
import com.cqt.common.util.BindIdUtil;
import com.cqt.common.util.PrivateCacheUtil;
import com.cqt.hmyc.web.bind.mapper.axe.PrivateBindInfoAxeMapper;
import com.cqt.hmyc.web.cache.request.RequestCache;
import com.cqt.hmyc.web.cache.request.RequestDelayed;
import com.cqt.model.bind.axe.dto.AxeBindIdKeyInfoDTO;
import com.cqt.model.bind.axe.entity.PrivateBindInfoAxe;
import com.cqt.model.bind.axe.vo.AxeBindingVO;
import com.cqt.model.common.properties.HideProperties;
import com.cqt.redis.util.RedissonUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.api.hint.HintManager;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * @author linshiqiang
 * @date 2021/9/9 15:51
 * AXE redis 缓存操作
 */
@Service
@Slf4j
public class AxeBindCacheService {

    private final RedissonUtil redissonUtil;

    @Resource
    private RedissonClient redissonClient;

    @Resource
    private HideProperties hideProperties;

    @Resource
    private PrivateBindInfoAxeMapper privateBindInfoAxeMapper;

    @Resource
    private AxeBindConverter axeBindConverter;

    public AxeBindCacheService(RedissonUtil redissonUtil) {
        this.redissonUtil = redissonUtil;
    }

    /**
     * 查询AXE 绑定关系 by requestId
     */
    public Optional<AxeBindingVO> getBindInfoByRequestId(String vccId, String requestId, String type) {
        if (!hideProperties.getSwitchs().getCheckAxeRequestId()) {
            return Optional.empty();
        }
        String bindInfoStr;
        try {
            String axeRequestIdKey = PrivateCacheUtil.getRequestIdKey(vccId, type, requestId);
            RBucket<Object> bucket = redissonClient.getBucket(axeRequestIdKey);
            boolean trySet = bucket.trySet(StrUtil.EMPTY_JSON, hideProperties.getSetnxTimeout(), TimeUnit.SECONDS);
            if (trySet) {
                // 设置成功的 是第一次
                return Optional.empty();
            } else {
                // 已存在key
                bindInfoStr = redissonUtil.getString(axeRequestIdKey);
                if (StrUtil.EMPTY_JSON.equals(bindInfoStr)) {
                    // 等待
                    CompletableFuture<String> completableFuture = new CompletableFuture<>();
                    String threadName = Thread.currentThread().getName();
                    RequestCache.REQUEST_ID_INFO_CACHE.put(axeRequestIdKey + threadName, completableFuture);
                    // 发送jdk延时队列
                    RequestCache.REQUEST_DELAYED_QUEUE.put(new RequestDelayed(hideProperties.getRequestTimeout(), TimeUnit.MILLISECONDS, axeRequestIdKey, threadName));
                    // 阻塞等待结果, 达到hideProperties.getMaxRequestTimeout(), 超时 抛出异常, 查询数据库
                    bindInfoStr = completableFuture.get(hideProperties.getMaxRequestTimeout(), TimeUnit.MILLISECONDS);
                    log.info("requestId: {}, 重复请求获取结果完成.", requestId);
                }
            }
        } catch (Exception e) {
            log.error("AXE redis getBindInfoByRequestId操作异常: ", e);
            return Optional.empty();
        }
        if (StrUtil.isEmpty(bindInfoStr)) {

            return Optional.empty();
        }

        return Optional.of(JSON.parseObject(bindInfoStr, AxeBindingVO.class));
    }

    /**
     * 查询 AxeBindingVO 返回结果 by requestId
     */
    private Optional<AxeBindingVO> getBindingVoByDb(String vccId, String requestId) {
        try {
            LambdaQueryWrapper<PrivateBindInfoAxe> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.select(PrivateBindInfoAxe::getBindId, PrivateBindInfoAxe::getTelX, PrivateBindInfoAxe::getTelXExt);
            queryWrapper.eq(PrivateBindInfoAxe::getRequestId, requestId);
            queryWrapper.gt(PrivateBindInfoAxe::getExpireTime, DateUtil.date());
            queryWrapper.last("limit 1");
            try (HintManager hintManager = HintManager.getInstance()) {
                hintManager.addTableShardingValue(TableNameConstant.PRIVATE_BIND_INFO_AXE, vccId);
                PrivateBindInfoAxe bindInfo = privateBindInfoAxeMapper.selectOne(queryWrapper);
                AxeBindingVO bindingVO = axeBindConverter.bindInfo2BindingVO(bindInfo);
                return Optional.ofNullable(bindingVO);
            }
        } catch (Exception e) {
            log.error("AXE getBindingVoByDb error: {}", e.getMessage());
        }
        return Optional.empty();
    }

    public boolean setUniqueExtBind(String vccId, String areaCode, String telX, String extNum) {
        String uniqueExtBindKey = PrivateCacheUtil.getUniqueExtBindKey(vccId, areaCode, telX, extNum);
        RBucket<Long> bucket = redissonClient.getBucket(uniqueExtBindKey);
        return bucket.trySet(System.currentTimeMillis(), 60, TimeUnit.MINUTES);
    }

    /**
     * 分配分机号
     */
    public Optional<String> getExtNum(String vccId, String type, String areaCode, String telX) {
        if (StrUtil.isEmpty(telX)) {
            return Optional.empty();
        }
        //  1. 从未使用分机号list 取出第一个元素  {vcc_id}:{type}:pool:ext:{area_code}:{tel_x}
        String usableExtPoolListKey = PrivateCacheUtil.getUsableExtPoolListKey(vccId, type, areaCode, telX);
        String extNum = redissonUtil.getDequeFirst(usableExtPoolListKey);
        if (StrUtil.isEmpty(extNum)) {
            return Optional.empty();
        }
        // 分机号是否被使用过
        String extBindInfoKey = PrivateCacheUtil.getExtBindInfoKey(vccId, type, telX, extNum);
        Boolean existString = redissonUtil.isExistString(extBindInfoKey);
        if (existString) {
            // 分机号已被使用, 递归查找, 直到找不到
            // 是否要查db
            return getExtNum(vccId, type, areaCode, telX);
        }

        return Optional.of(extNum);
    }

    /**
     * 检查指定X和分机号，是否可分配
     */
    public Optional<String> checkExtNumAndTelX(String vccId, String type, String areaCode, String telX, String extNum) {
        if (StrUtil.isNotEmpty(extNum)) {
            // 1. 分机号是否被使用过
            String extBindInfoKey = PrivateCacheUtil.getExtBindInfoKey(vccId, type, telX, extNum);
            Boolean existString = redissonUtil.isExistString(extBindInfoKey);
            if (existString) {
                return Optional.empty();
            }
            String usableExtPoolListKey = PrivateCacheUtil.getUsableExtPoolListKey(vccId, type, areaCode, telX);
            // 2. 移除分机号
            Boolean removed = redissonUtil.removeDequeValue(usableExtPoolListKey, extNum);
            if (!Boolean.TRUE.equals(removed)) {
                log.debug("vccId: {}, areaCode: {}, telX: {}, extNum: {}, 移除分机号失败", vccId, areaCode, telX, extNum);
                return Optional.empty();
            }
            return Optional.of(extNum);
        }

        return getExtNum(vccId, type, areaCode, telX);
    }

    public Optional<AxeBindIdKeyInfoDTO> getBindInfoByBindId(String vccId, String bindId, String type) {
        String bindInfoStr;
        try {
            String bindIdKey = PrivateCacheUtil.getBindIdKey(vccId, type, bindId);
            bindInfoStr = redissonUtil.getString(bindIdKey);
        } catch (Exception e) {
            log.error("redis get操作异常: ", e);
            // 查db
            return getBindIdKeyInfoDtoByDb(vccId, bindId);
        }
        if (StrUtil.isBlank(bindInfoStr)) {
            if (hideProperties.getSwitchs().getUnbindQueryDb()) {
                return getBindIdKeyInfoDtoByDb(vccId, bindId);
            }
            return Optional.empty();
        }
        return Optional.of(JSON.parseObject(bindInfoStr, AxeBindIdKeyInfoDTO.class));
    }

    private Optional<AxeBindIdKeyInfoDTO> getBindIdKeyInfoDtoByDb(String vccId, String bindId) {
        try {
            try (HintManager hintManager = HintManager.getInstance()) {
                String numberHash = BindIdUtil.getNumberHashByBindId(bindId);
                String sharingKey = vccId + StrUtil.AT + numberHash;
                hintManager.addDatabaseShardingValue(TableNameConstant.PRIVATE_BIND_INFO_AXE, numberHash);
                hintManager.addTableShardingValue(TableNameConstant.PRIVATE_BIND_INFO_AXE, sharingKey);
                LambdaQueryWrapper<PrivateBindInfoAxe> queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.eq(PrivateBindInfoAxe::getBindId, bindId);
                queryWrapper.gt(PrivateBindInfoAxe::getExpireTime, DateUtil.date());
                queryWrapper.last("limit 1");
                PrivateBindInfoAxe bindInfo = privateBindInfoAxeMapper.selectOne(queryWrapper);
                AxeBindIdKeyInfoDTO bindIdKeyInfoDTO = axeBindConverter.bindInfo2BindIdKeyInfoDTO(bindInfo);
                // TODO 回写redis
                return Optional.ofNullable(bindIdKeyInfoDTO);
            }
        } catch (Exception e) {
            log.error("AXE getBindIdKeyInfoDtoByDb error: {}", e.getMessage());
        }
        return Optional.empty();
    }
}
