package com.cqt.recycle.web.corpinfo.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.cloud.nacos.NacosConfigProperties;
import com.alibaba.fastjson.JSON;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.exception.NacosException;
import com.cqt.common.constants.PrivateCacheConstant;
import com.cqt.common.enums.ErrorCodeEnum;
import com.cqt.common.future.AsyncTask;
import com.cqt.common.util.PrivateCacheUtil;
import com.cqt.model.common.Result;
import com.cqt.model.common.properties.HideProperties;
import com.cqt.model.corpinfo.dto.SupplierWeight;
import com.cqt.model.corpinfo.dto.SyncCorpInfoDTO;
import com.cqt.model.corpinfo.entity.PrivateCorpBusinessInfo;
import com.cqt.model.numpool.dto.SyncRemoteDTO;
import com.cqt.model.numpool.vo.SyncResultVO;
import com.cqt.model.supplier.PrivateCorpAreaDistributionStrategy;
import com.cqt.recycle.config.AreaLocationConfig;
import com.cqt.recycle.config.LocalCache;
import com.cqt.recycle.web.corpinfo.event.BusinessEvent;
import com.cqt.recycle.web.corpinfo.mapper.PrivateCorpAreaDistributionStrategyMapper;
import com.cqt.recycle.web.corpinfo.mapper.PrivateCorpBusinessInfoMapper;
import com.cqt.redis.util.RedissonUtil;
import com.cqt.xxljob.annotations.XxlJobRegister;
import com.cqt.xxljob.enums.ExecutorRouteStrategyEnum;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author linshiqiang
 * @since 2022/2/24 10:43
 * 业务配置同步
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CorpBusinessService {

    private final NacosConfigProperties nacosConfigProperties;

    private final HideProperties hideProperties;

    private final RedissonUtil redissonUtil;

    private final PrivateCorpBusinessInfoMapper privateCorpBusinessInfoMapper;

    private final PrivateCorpAreaDistributionStrategyMapper privateCorpAreaDistributionStrategyMapper;

    private final ApplicationContext applicationContext;

    private final ObjectMapper objectMapper;

    private final ThreadPoolTaskExecutor recycleExecutor;

    private final AsyncTask<SyncResultVO, SyncRemoteDTO> asyncTask;

    private final ConfigService configService;

    private final ConfigService backConfigService;

    public Result sync(SyncCorpInfoDTO syncCorpInfoDTO) throws JsonProcessingException {
        if (log.isInfoEnabled()) {
            log.info("sync corp info params: {}", objectMapper.writeValueAsString(syncCorpInfoDTO));
        }
        String vccId = syncCorpInfoDTO.getVccId();
        String businessType = "";
        if (StrUtil.isNotEmpty(vccId)) {
            PrivateCorpBusinessInfo businessInfo = privateCorpBusinessInfoMapper.selectById(vccId);
            if (!Optional.ofNullable(businessInfo).isPresent()) {
                return Result.fail(ErrorCodeEnum.VCC_ID_NOT_EXIST.getCode(), ErrorCodeEnum.VCC_ID_NOT_EXIST.getMessage());
            }
            businessType = businessInfo.getBusinessType();
        }

        String syncCorpInfoUrl = hideProperties.getSyncCorpInfoUrl();
        List<String> serverIps = hideProperties.getServerIps();
        List<SyncResultVO> list = remoteSync(serverIps, syncCorpInfoUrl, syncCorpInfoDTO);
        // 判断是否有失败的
        List<SyncResultVO> failList = list.stream().filter(item -> !item.getSuccess()).collect(Collectors.toList());
        Result result = Result.ok(list);
        if (CollUtil.isNotEmpty(failList)) {
            result.setCode(-1);
            result.setMessage("存在调用失败的服务!");
        }

        // 同步企业业务配置信息到双机房nacos, 并创建相应绑定关系表
        applicationContext.publishEvent(new BusinessEvent(this, vccId, businessType));

        return result;
    }

    public List<SyncResultVO> remoteSync(List<String> serverIps, String syncCorpInfoUrl, SyncCorpInfoDTO syncCorpInfoDTO) {
        // 删除redis
        boolean delKey = redissonUtil.delKey(PrivateCacheUtil.geVccInfoKey(syncCorpInfoDTO.getVccId()));
        log.info("delete redis key: {}, {}", delKey, PrivateCacheUtil.geVccInfoKey(syncCorpInfoDTO.getVccId()));

        List<SyncRemoteDTO> syncRemoteDtoList = serverIps.stream().map(ip -> SyncRemoteDTO.builder()
                .url(String.format(syncCorpInfoUrl, ip, syncCorpInfoDTO.getVccId()))
                .ip(ip)
                .build()).collect(Collectors.toList());

        RequestRemoteTaskLoaderImpl requestRemoteTaskLoader = new RequestRemoteTaskLoaderImpl();

        return asyncTask.sendAsyncBatch(syncRemoteDtoList, requestRemoteTaskLoader, recycleExecutor);
    }

    public Result switchPlace(String place) throws NacosException {
        log.info("绑定关系接口开始切换到机房: {}", place);
        Map<String, String> locationCache = LocalCache.AREA_LOCATION_CACHE;

        TreeMap<String, String> switchMap = new TreeMap<>();
        for (String areaCode : locationCache.keySet()) {
            switchMap.put(areaCode, place);
        }
        String jsonString = JSON.toJSONString(switchMap, true);
        // 发布本地nacos
        boolean publishConfig = configService.publishConfig(AreaLocationConfig.DATA_ID, "iccp", jsonString, "json");
        log.info("发布本地nacos 地市所属机房配置结果：{}", publishConfig);

        if (StrUtil.isNotEmpty(hideProperties.getBackNacos())) {
            boolean b = backConfigService.publishConfig(AreaLocationConfig.DATA_ID, "iccp", jsonString, "json");
            log.info("发布back nacos 地市所属机房配置结果：{}", b);
        }
        return Result.ok();
    }

    @XxlJobRegister(jobDesc = "定时同步企业地市供应商权重配置到nacos",
            cron = "0 0 1 * * ? *",
            triggerStatus = 0,
            executorRouteStrategy = ExecutorRouteStrategyEnum.ROUND)
    @XxlJob("syncSupplierStrategy")
    public Result syncSupplierStrategy(String strategyId, String operateType) {
        RLock lock = redissonUtil.getLock(PrivateCacheConstant.SYNC_DISTRIBUTION_STRATEGY_LOCK_KEY);
        try {
            boolean tryLock = lock.tryLock(3, TimeUnit.SECONDS);
            if (!tryLock) {
                return Result.fail(500, "同步操作正在进行, 请勿频繁操作!");
            }
            List<PrivateCorpAreaDistributionStrategy> list = privateCorpAreaDistributionStrategyMapper.selectList(null);
            Map<String, List<SupplierWeight>> map = new HashMap<>(32);
            for (PrivateCorpAreaDistributionStrategy strategy : list) {
                List<SupplierWeight> value = JSON.parseArray(strategy.getStrategyInfo(), SupplierWeight.class);
                map.put(strategy.getStrategyId(), value);
            }
            String jsonString = JSON.toJSONString(map, true);
            // 发布本地nacos
            String dataId = hideProperties.getSupplierDistributionStrategyDataId();
            String group = nacosConfigProperties.getGroup();
            boolean publishConfig = configService.publishConfig(dataId, group, jsonString, "json");
            log.info("发布本地nacos 号码分配 地市按供应商权重配置信息结果：{}", publishConfig);

            if (StrUtil.isNotEmpty(hideProperties.getBackNacos()) && ObjectUtil.isNotEmpty(backConfigService)) {
                boolean b = backConfigService.publishConfig(dataId, group, jsonString, "json");
                log.info("发布back nacos 号码分配 地市按供应商权重配置信息结果：{}", b);
            }
        } catch (Exception e) {
            log.error("syncSupplierStrategy to nacos error: ", e);
            throw new RuntimeException(e);
        } finally {
            lock.unlock();
        }
        return Result.ok();
    }

}
