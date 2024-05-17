package com.cqt.hmyc.web.bind.service.ax;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cqt.common.constants.TableNameConstant;
import com.cqt.common.enums.PoolTypeEnum;
import com.cqt.common.util.PrivateCacheUtil;
import com.cqt.hmyc.web.bind.mapper.ax.PrivateBindInfoAxMapper;
import com.cqt.hmyc.web.cache.request.RequestCache;
import com.cqt.hmyc.web.cache.request.RequestDelayed;
import com.cqt.hmyc.web.numpool.mapper.PrivateNumberInfoMapper;
import com.cqt.model.bind.ax.dto.AxBindIdKeyInfoDTO;
import com.cqt.model.bind.ax.entity.PrivateBindInfoAx;
import com.cqt.model.bind.ax.vo.AxBindingVO;
import com.cqt.model.common.properties.HideProperties;
import com.cqt.redis.util.RedissonUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.api.hint.HintManager;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * @author linshiqiang
 * @date 2022/3/15 11:37
 * AX 缓存操作
 */
@Service
@Slf4j
public class AxBindCacheService {

    @Resource
    private RedissonUtil redissonUtil;

    @Resource
    private HideProperties hideProperties;

    @Qualifier(value = "redissonClient")
    @Autowired
    private RedissonClient redissonClient;

    @Resource
    private PrivateBindInfoAxMapper privateBindInfoAxMapper;

    @Resource
    private PrivateNumberInfoMapper privateNumberInfoMapper;

    @Resource
    private AxBindConverter axBindConverter;

    /**
     * 查询AX 绑定关系 by requestId
     */
    public Optional<AxBindingVO> getAxBindInfoByRequestId(String vccId, String requestId, String numType) {
        String bindInfoStr;
        try {

            String requestIdKey = PrivateCacheUtil.getRequestIdKey(vccId, numType, requestId);
            RBucket<Object> bucket = redissonClient.getBucket(requestIdKey);
            boolean trySet = bucket.trySet(StrUtil.EMPTY_JSON, hideProperties.getSetnxTimeout(), TimeUnit.SECONDS);
            if (trySet) {
                // 设置成功的 是第一次
                return Optional.empty();
            } else {
                // 已存在key
                bindInfoStr = redissonUtil.getString(requestIdKey);
                if (StrUtil.EMPTY_JSON.equals(bindInfoStr)) {
                    CompletableFuture<String> completableFuture = new CompletableFuture<>();
                    String threadName = Thread.currentThread().getName();
                    RequestCache.REQUEST_ID_INFO_CACHE.put(requestIdKey + threadName, completableFuture);
                    // 发送jdk延时队列
                    RequestCache.REQUEST_DELAYED_QUEUE.put(new RequestDelayed(hideProperties.getRequestTimeout(), TimeUnit.MILLISECONDS, requestIdKey, threadName));
                    // 阻塞等待结果, 达到hideProperties.getMaxRequestTimeout(), 超时 抛出异常, 查询数据库
                    bindInfoStr = completableFuture.get(hideProperties.getMaxRequestTimeout(), TimeUnit.MILLISECONDS);
                    log.info("requestId: {}, 重复请求获取结果完成.", requestId);
                }
            }
        } catch (Exception e) {
            log.info("redis get操作异常: ", e);
            // 查db
            return getAxBindingVoByDb(vccId, requestId);
        }
        if (StrUtil.isEmpty(bindInfoStr)) {
            return Optional.empty();
        }
        return Optional.ofNullable(JSON.parseObject(bindInfoStr, AxBindingVO.class));
    }

    private Optional<AxBindingVO> getAxBindingVoByDb(String vccId, String requestId) {
        try (HintManager hintManager = HintManager.getInstance()) {
            hintManager.addTableShardingValue(TableNameConstant.PRIVATE_BIND_INFO_AX, vccId);
            LambdaQueryWrapper<PrivateBindInfoAx> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(PrivateBindInfoAx::getRequestId, requestId);
            queryWrapper.gt(PrivateBindInfoAx::getExpireTime, DateUtil.date());
            queryWrapper.last("limit 1");
            PrivateBindInfoAx privateBindInfoAx = privateBindInfoAxMapper.selectOne(queryWrapper);
            return Optional.ofNullable(axBindConverter.bindInfoAx2AxBindingVO(privateBindInfoAx));
        }
    }

    /**
     * 获取X号码
     */
    public String getTelX(String vccId, String numType, String areaCode) {
        //  1. 从未使用set移出一个ext
        String axUsablePoolKey = PrivateCacheUtil.getAxUsablePoolKey(vccId, numType, areaCode);
        return redissonUtil.removeSetRandom(axUsablePoolKey);
    }

    public Boolean isExistX(String vccId, String numType, String areaCode, String telX) {
        //  1. 从未使用分机号set移出一个ext
        String axUsablePoolKey = PrivateCacheUtil.getAxUsablePoolKey(vccId, numType, areaCode);
        return redissonUtil.removeSet(axUsablePoolKey, telX);
    }

    /**
     * 查询AX 绑定关系 by bindId
     */
    public Optional<AxBindIdKeyInfoDTO> getAxBindInfoByBindId(String vccId, String numType, String bindId) {
        String bindInfoStr;
        try {
            bindInfoStr = redissonUtil.getString(PrivateCacheUtil.getBindIdKey(vccId, numType, bindId));
        } catch (Exception e) {
            log.error("redis get操作异常: ", e);
            //  查db
            return getBindIdKeyInfoDtoByDb(vccId, bindId);
        }
        if (StrUtil.isBlank(bindInfoStr)) {
            return getBindIdKeyInfoDtoByDb(vccId, bindId);
        }
        return Optional.ofNullable(JSON.parseObject(bindInfoStr, AxBindIdKeyInfoDTO.class));
    }

    public Optional<AxBindIdKeyInfoDTO> getBindIdKeyInfoDtoByDb(String vccId, String bindId) {
        try (HintManager hintManager = HintManager.getInstance()) {
            hintManager.addTableShardingValue(TableNameConstant.PRIVATE_BIND_INFO_AX, vccId);
            LambdaQueryWrapper<PrivateBindInfoAx> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(PrivateBindInfoAx::getBindId, bindId);
            queryWrapper.gt(PrivateBindInfoAx::getExpireTime, DateUtil.date());
            queryWrapper.last("limit 1");
            PrivateBindInfoAx privateBindInfoAx = privateBindInfoAxMapper.selectOne(queryWrapper);
            // TODO 回写redis
            return Optional.ofNullable(axBindConverter.bindInfoAx2AxBindIdKeyInfoDTO(privateBindInfoAx));
        }
    }

    public String randomDistributeY(String vccId, String areaCode) {
        String axUsablePoolKey = PrivateCacheUtil.getAxUsablePoolKey(vccId, PoolTypeEnum.AX_Y.name(), areaCode);
        try {
            String number = redissonUtil.getSetRandom(axUsablePoolKey);
            if (StrUtil.isNotEmpty(number)) {
                return number;
            }
        } catch (Exception e) {
            log.error("AX vccId: {}, areaCode: {}, 随机分配Y号码 redis异常: ", vccId, areaCode, e);
        }

        // db
        List<String> axyNumberList = privateNumberInfoMapper.getAxyNumberList(vccId, areaCode, PoolTypeEnum.AX_Y.name(), 1);
        if (CollUtil.isNotEmpty(axyNumberList)) {
            int index = RandomUtil.randomInt(axyNumberList.size());
            return axyNumberList.get(index);
        }
        return null;
    }
}
