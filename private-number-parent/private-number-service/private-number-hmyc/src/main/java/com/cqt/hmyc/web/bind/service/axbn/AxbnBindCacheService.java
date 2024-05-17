package com.cqt.hmyc.web.bind.service.axbn;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.cqt.common.constants.SystemConstant;
import com.cqt.common.util.PrivateCacheUtil;
import com.cqt.hmyc.web.bind.mapper.axbn.PrivateBindInfoAxbnMapper;
import com.cqt.hmyc.web.cache.LuaScriptCache;
import com.cqt.hmyc.web.cache.request.RequestCache;
import com.cqt.hmyc.web.cache.request.RequestDelayed;
import com.cqt.model.bind.axbn.dto.AxbnBindIdKeyInfoDTO;
import com.cqt.model.bind.axbn.vo.AxbnBindingVO;
import com.cqt.model.common.properties.HideProperties;
import com.cqt.redis.config.DoubleRedisProperties;
import com.cqt.redis.util.RedissonUtil;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBucket;
import org.redisson.api.RScript;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * @author linshiqiang
 * @date 2022/3/22 14:47
 */
@Service
@Slf4j
public class AxbnBindCacheService {

    @Resource
    private RedissonUtil redissonUtil;

    @Resource
    private AxbnBindConverter bindConverter;

    @Resource
    private PrivateBindInfoAxbnMapper privateBindInfoAxbnMapper;

    @Resource
    private HideProperties hideProperties;

    @Qualifier(value = "redissonClient")
    @Autowired
    private RedissonClient redissonClient;

    @Qualifier(value = "redissonClient2")
    @Autowired(required = false)
    private RedissonClient redissonClient2;

    @Resource
    private DoubleRedisProperties redisProperties;

    @Resource(name = "otherExecutor")
    private ThreadPoolTaskExecutor otherExecutor;

    /**
     * 查询AXBN 绑定关系 by requestId
     */
    public Optional<AxbnBindingVO> getBindInfoByRequestId(String vccId, String requestId, String numType) {
        String bindInfoStr;
        try {
            String requestIdKey = PrivateCacheUtil.getRequestIdKey(vccId, numType, requestId);
            RBucket<Object> bucket = redissonClient.getBucket(requestIdKey);
            boolean trySet = bucket.trySet(SystemConstant.EMPTY_OBJECT, hideProperties.getSetnxTimeout(), TimeUnit.SECONDS);
            if (trySet) {
                // 设置成功的 是第一次
                return Optional.empty();
            } else {
                // 已存在key
                bindInfoStr = redissonUtil.getString(requestIdKey);
                if (SystemConstant.EMPTY_OBJECT.equals(bindInfoStr)) {
                    CompletableFuture<String> completableFuture = new CompletableFuture<>();
                    String threadName = Thread.currentThread().getName();
                    RequestCache.REQUEST_ID_INFO_CACHE.put(requestIdKey + threadName, completableFuture);
                    // 发送jdk延时队列
                    RequestCache.REQUEST_DELAYED_QUEUE.put(new RequestDelayed(hideProperties.getRequestTimeout(), TimeUnit.MILLISECONDS, requestIdKey, threadName));
                    // 阻塞等待结果, 达到hideProperties.getMaxRequestTimeout(), 超时 抛出异常, 查询数据库
                    bindInfoStr = completableFuture.get(hideProperties.getMaxRequestTimeout(), TimeUnit.MILLISECONDS);
                    log.info("vccId: {}, requestId: {}, 重复请求获取结果完成.", vccId, requestId);
                }
            }
        } catch (Exception e) {
            log.info("redis get操作异常: ", e);
            // TODO 查db
            return Optional.empty();
        }
        if (StrUtil.isEmpty(bindInfoStr)) {
            return Optional.empty();
        }
        return Optional.ofNullable(JSON.parseObject(bindInfoStr, AxbnBindingVO.class));
    }

    @SuppressWarnings("all")
    public Optional<List<String>> getTelX(List<Object> keysList, Integer index, String numType) {

        int size = keysList.size();
        RScript script = redissonClient.getScript();
        List<Boolean> exists = script.scriptExists(LuaScriptCache.getSha1(numType + size));
        Object object;
        if (!exists.get(0)) {
            object = script.eval(RScript.Mode.READ_WRITE, LuaScriptCache.getScript(numType + size), RScript.ReturnType.MULTI, keysList);
        } else {
            object = script.evalSha(RScript.Mode.READ_WRITE, LuaScriptCache.getSha1(numType + size), RScript.ReturnType.MULTI, keysList);
        }
        if (ObjectUtil.isEmpty(object)) {
            return Optional.empty();
        }
        keysList.remove(0);
        List<String> xList = Convert.toList(String.class, object);
        List<String> list = new ArrayList<>(xList);
        if (redisProperties.getCluster2().getActive()) {
            otherExecutor.execute(() -> {

                try {
                    // 异地 往已使用X号码池里 添加X号码
                    for (Object obj : keysList) {
                        redissonClient2.getSet(Convert.toStr(obj)).addAll(list);
                    }
                } catch (Exception e) {
                    log.error("{}, 键: {}, X号码: {}, 异地保存已使用X号码池异常: ", keysList, xList, numType, e);
                }
            });
        }

        return Optional.of(xList);
    }

    /**
     * 查询 AxbnBindIdKeyInfoDTO by bindId
     */
    public Optional<AxbnBindIdKeyInfoDTO> getBindInfoByBindId(String vccId, String numType, String bindId) {
        String bindInfoStr;
        try {
            bindInfoStr = redissonUtil.getString(PrivateCacheUtil.getBindIdKey(vccId, numType, bindId));
        } catch (Exception e) {
            log.error("redis get操作异常: ", e);
            //  查db
            return Optional.empty();
        }
        if (StrUtil.isBlank(bindInfoStr)) {
            return Optional.empty();
        }
        return Optional.ofNullable(JSON.parseObject(bindInfoStr, AxbnBindIdKeyInfoDTO.class));
    }

    public Long getTtl(String key) {
        return redissonUtil.getTtl(key);
    }
}
