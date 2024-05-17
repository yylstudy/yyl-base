package com.cqt.hmyc.web.bind.service.axebn;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.cqt.common.constants.SystemConstant;
import com.cqt.common.util.PrivateCacheUtil;
import com.cqt.hmyc.web.cache.request.RequestCache;
import com.cqt.hmyc.web.cache.request.RequestDelayed;
import com.cqt.model.bind.axebn.dto.AxebnBindIdKeyInfoDTO;
import com.cqt.model.bind.axebn.vo.AxebnBindingVO;
import com.cqt.model.common.properties.HideProperties;
import com.cqt.redis.config.DoubleRedisProperties;
import com.cqt.redis.util.RedissonUtil;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author linshiqiang
 * @date 2022/3/7 14:39
 */
@Service
@Slf4j
public class AxebnBindCacheService {

    @Resource
    private RedissonUtil redissonUtil;

    @Qualifier(value = "redissonClient")
    @Autowired
    private RedissonClient redissonClient;

    @Qualifier(value = "redissonClient2")
    @Autowired(required = false)
    private RedissonClient redissonClient2;

    @Resource
    private HideProperties hideProperties;

    @Resource
    private DoubleRedisProperties redisProperties;

    public Optional<AxebnBindingVO> getAxebnBindInfoByRequestId(String vccId, String requestId, String numType) {
        String bindInfoStr = "";
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
                    log.info("requestId: {}, 重复请求获取结果完成.", requestId);
                }
            }
        } catch (Exception e) {
            log.info("redis get操作异常: ", e);
            // TODO 查db
        }
        if (StrUtil.isNotEmpty(bindInfoStr)) {

            return Optional.ofNullable(JSON.parseObject(bindInfoStr, AxebnBindingVO.class));
        }
        return Optional.empty();
    }

    public void initPoolTelB(String vccId, String numType, String areaCode, String telB, List<String> poolList) {
        String initFlagKey = PrivateCacheUtil.getInitFlagKey(vccId, numType, areaCode, telB);
        String initFlag = redissonUtil.getString(initFlagKey);
        if (!"1".equals(initFlag)) {
            String usablePoolKey = PrivateCacheUtil.getUsablePoolSlotKey(vccId, numType, areaCode, telB);
            // 初始化
            RBatch batch = redissonClient.createBatch();
            batch.getBucket(initFlagKey).setAsync("1");
            batch.getSet(usablePoolKey).addAllAsync(poolList);
            // 初始化不能异步
            batch.execute();

            if (redisProperties.getCluster2().getActive()) {
                try {
                    RBatch batch2 = redissonClient2.createBatch();
                    batch2.getBucket(initFlagKey).setAsync("1");
                    batch2.getSet(usablePoolKey).addAllAsync(poolList);
                    RFuture<BatchResult<?>> executeAsync = batch2.executeAsync();
                    log.info("{}, 异地初始化号码池结果: {}", usablePoolKey, executeAsync.get().getResponses());
                } catch (Exception e) {
                    log.error("AXEBN BX 异地 initPool: {}, 操作异常: ", usablePoolKey, e);
                }
            }

            // TODO 保存初始化号码到表里
        }
    }

    public Set<Object> getExtNum(String vccId, String numType, String telX, Integer count) {
        //  1. 从未使用分机号set移出一个ext
        String poolUnUsedExtKey = PrivateCacheUtil.getUsableExtPoolKey(vccId, numType, telX);
        return redissonUtil.removeSetRandomByCount(poolUnUsedExtKey, count);
    }

    public Optional<String> getInter(List<String> telList, String vccId, String numType, String areaCode) {
        List<Object> keys = telList.stream()
                .map(item -> PrivateCacheUtil.getUsablePoolSlotKey(vccId, numType, areaCode, item))
                .collect(Collectors.toList());
        RScript script = redissonClient.getScript();
        // TODO lua脚本 要缓存, 使用evalSha, 改成差集方式
        Object eval = script.eval(RScript.Mode.READ_WRITE, getInterScript(telList.size()), RScript.ReturnType.VALUE, keys);
        if (ObjectUtil.isEmpty(eval)) {
            return Optional.empty();
        }
        String telX = Convert.toStr(eval);
        // 异地移除
        if (redisProperties.getCluster2().getActive()) {
            try {
                RBatch batch = redissonClient2.createBatch();
                for (Object key : keys) {
                    batch.getSet(key.toString()).removeAsync(telX);
                }
                batch.executeAsync();
            } catch (Exception e) {
                log.error("{}, 异地 getInter操作异常: ", numType, e);
            }
        }

        return Optional.ofNullable(Convert.toStr(eval));
    }

    public String getInterScript(Integer size) {
        List<String> keysList = new ArrayList<>();
        List<String> sremList = new ArrayList<>();

        for (int i = 1; i <= size; i++) {
            keysList.add("KEYS[" + i + "]");
            sremList.add("redis.call('srem',KEYS[" + i + "],x);");
        }
        String keys = String.join(",", keysList);

        String srem = String.join(" ", sremList);

        String script = "local set = redis.call('sinter'," + keys + ");\n" +
                "if next(set) ~=nil  then\n" +
                "\tlocal x = set[1];\n" +
                "\t" + srem + "\n" +
                "\treturn x;\n" +
                "end;\n" +
                "return nil;";
        log.info(script);
        return script;
    }

    public Optional<AxebnBindIdKeyInfoDTO> getAxebnBindInfoByBindId(String vccId, String numType, String bindId) {
        String bindInfoStr = "";
        try {
            bindInfoStr = redissonUtil.getString(PrivateCacheUtil.getBindIdKey(vccId, numType, bindId));
        } catch (Exception e) {
            log.error("redis get操作异常: ", e);
            // TODO 查db
        }
        if (StrUtil.isBlank(bindInfoStr)) {
            return Optional.empty();
        }
        return Optional.ofNullable(JSON.parseObject(bindInfoStr, AxebnBindIdKeyInfoDTO.class));
    }

    public Long getTtl(String key) {
        return redissonUtil.getTtl(key);
    }
}
