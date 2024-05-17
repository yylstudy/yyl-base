package com.cqt.hmyc.web.cache;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.StopWatch;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cqt.common.constants.PrivateCacheConstant;
import com.cqt.common.enums.AxbPoolTypeEnum;
import com.cqt.common.enums.PoolTypeEnum;
import com.cqt.common.util.PrivateCacheUtil;
import com.cqt.hmyc.web.corpinfo.mapper.PrivateCorpBusinessInfoMapper;
import com.cqt.hmyc.web.corpinfo.service.CorpBusinessService;
import com.cqt.hmyc.web.numpool.mapper.PrivateNumberInfoMapper;
import com.cqt.model.common.properties.HideProperties;
import com.cqt.model.corpinfo.dto.ExpireTimeDTO;
import com.cqt.model.corpinfo.dto.PrivateCorpBusinessInfoDTO;
import com.cqt.model.corpinfo.entity.PrivateCorpBusinessInfo;
import com.cqt.model.numpool.entity.PrivateNumberInfo;
import com.cqt.redis.config.DoubleRedisProperties;
import com.cqt.redis.util.RedissonUtil;
import com.cqt.xxljob.annotations.XxlJobRegister;
import com.cqt.xxljob.enums.ExecutorRouteStrategyEnum;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RScript;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author linshiqiang
 * @date 2021/9/10 13:37
 */
@Service
@Slf4j
public class SyncNumberPoolJob {

    private final PrivateCorpBusinessInfoMapper privateCorpBusinessInfoMapper;

    private final PrivateNumberInfoMapper privateNumberInfoMapper;

    private final RedissonUtil redissonUtil;

    private final DoubleRedisProperties redisProperties;

    private final HideProperties hideProperties;

    private final CorpBusinessService corpBusinessService;

    @Qualifier(value = "redissonClient")
    @Autowired
    private RedissonClient redissonClient;

    @Qualifier(value = "redissonClient2")
    @Autowired(required = false)
    private RedissonClient redissonClient2;

    public SyncNumberPoolJob(PrivateCorpBusinessInfoMapper privateCorpBusinessInfoMapper, PrivateNumberInfoMapper privateNumberInfoMapper,
                             RedissonUtil redissonUtil, DoubleRedisProperties redisProperties, HideProperties hideProperties, CorpBusinessService corpBusinessService) {
        this.privateCorpBusinessInfoMapper = privateCorpBusinessInfoMapper;
        this.privateNumberInfoMapper = privateNumberInfoMapper;
        this.redissonUtil = redissonUtil;
        this.redisProperties = redisProperties;
        this.hideProperties = hideProperties;
        this.corpBusinessService = corpBusinessService;
    }

    @XxlJobRegister(jobDesc = "刷新JVM本地号码池缓存",
            cron = "0 0 6 1/7 * ?",
            triggerStatus = 1,
            executorRouteStrategy = ExecutorRouteStrategyEnum.SHARDING_BROADCAST)
    @XxlJob("refreshNumberPool")
    @PostConstruct
    public void refreshNumberPool() {

        LambdaQueryWrapper<PrivateNumberInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PrivateNumberInfo::getAllocationFlag, 1);
        queryWrapper.eq(PrivateNumberInfo::getState, 0);
        List<PrivateNumberInfo> numberInfoList = privateNumberInfoMapper.selectList(queryWrapper);
        if (CollUtil.isEmpty(numberInfoList)) {
            log.info("db 号码池为空, 请先导入号码池!");
            return;
        }
        // pool_type和business_type 不能为空
        numberInfoList = numberInfoList.stream()
                .filter(item -> StrUtil.isNotEmpty(item.getPoolType()) && StrUtil.isNotEmpty(item.getBusinessType()) && StrUtil.isNotEmpty(item.getAreaCode()) && StrUtil.isNotEmpty(item.getVccId()))
                .collect(Collectors.toList());
        StopWatch watch = new StopWatch();
        watch.start();
        // 根据vccId分组
        Map<String, List<PrivateNumberInfo>> vccIdGroupMap = numberInfoList.stream()
                .collect(Collectors.groupingBy(PrivateNumberInfo::getVccId));
        vccIdGroupMap.forEach((vccId, privateNumberInfoList) -> {
            // 根据号码池分组
            Map<String, List<PrivateNumberInfo>> numTypeGroupMap = privateNumberInfoList.stream()
                    .collect(Collectors.groupingBy(PrivateNumberInfo::getPoolType));

            numTypeGroupMap.forEach((poolType, businessList) -> {
                // 根据地市编码分组
                Map<String, List<PrivateNumberInfo>> areaCodeGroupMap = businessList.stream()
                        .collect(Collectors.groupingBy(PrivateNumberInfo::getAreaCode));

                areaCodeGroupMap.forEach((areaCode, poolList) -> {
                    // 号码类型
                    ArrayList<String> numberList = new ArrayList<>();
                    HashSet<String> masterSet = new HashSet<>();
                    HashSet<String> slaveSet = new HashSet<>();
                    poolList.forEach(item -> {
                        // 号码归属业务模式
                        NumberTypeCache.put(item.getNumber(), item.getBusinessType());
                        numberList.add(item.getNumber());

                        if (AxbPoolTypeEnum.MASTER.name().equals(item.getPlace())) {
                            masterSet.add(item.getNumber());
                        } else {
                            slaveSet.add(item.getNumber());
                        }

                        // X号码的分机号 未使用list
                        if (PoolTypeEnum.AXE.name().equals(poolType)) {
                            if (hideProperties.getSwitchs().getInitExtFlag()) {
                                intiExtPool(vccId, poolType, areaCode, item.getNumber());
                            }
                        }

                        if (PoolTypeEnum.AXEBN.name().equals(poolType)) {
                            if (hideProperties.getSwitchs().getInitExtFlag()) {
                                intiExtPool(vccId, poolType, areaCode, item.getNumber());
                            }

                        }
                        if (PoolTypeEnum.AXBN.name().equals(poolType)) {
                            String key = String.format(PrivateCacheConstant.USABLE_AXBN_POOL_SLOT_KEY, vccId, poolType.toLowerCase(), areaCode);
                            redissonUtil.setSetString(key, numberList);
                            log.info("更新axbn号码池, vccId: {}, poolType: {}, areaCode: {}, numberList: {}", vccId, poolType, areaCode, numberList.size());
                        }

                    });
                    // 号码池
                    if (PoolTypeEnum.AXB.name().equals(poolType)) {
                        NumberPoolAxbCache.addPool(AxbPoolTypeEnum.ALL, vccId, areaCode, new HashSet<>(numberList));
                        NumberPoolAxbCache.addPool(AxbPoolTypeEnum.MASTER, vccId, areaCode, masterSet);
                        NumberPoolAxbCache.addPool(AxbPoolTypeEnum.SLAVE, vccId, areaCode, slaveSet);
                    }

                    if (PoolTypeEnum.AXBN.name().equals(poolType)) {
                        NumberPoolAxbnCache.addPool(vccId, areaCode, new HashSet<>(numberList));
                    }

                    if (PoolTypeEnum.AXG.name().equals(poolType)) {
                        NumberPoolAxgCache.addPool(vccId, areaCode, numberList);
                    }

                    if (PoolTypeEnum.AXYB_X.name().equals(poolType)) {
                        NumberPoolAxybCache.addPoolX(vccId, areaCode, numberList);
                    }

                    if (PoolTypeEnum.AXYB_Y.name().equals(poolType)) {
                        NumberPoolAxybCache.addPoolY(vccId, areaCode, numberList);
                    }

                    if (PoolTypeEnum.AXE.name().equals(poolType)) {
                        NumberPoolAxeCache.addPool(vccId, areaCode, numberList);
                    }

                    if (PoolTypeEnum.AX.name().equals(poolType)) {
                        NumberPoolAxCache.addPool(vccId, areaCode, numberList);
                        // redis
                        if (hideProperties.getSwitchs().getInitExtFlag()) {
                            initXpool(vccId, poolType, areaCode, new HashSet<>(numberList));
                        }
                    }

                });
            });
        });

        watch.stop();
        log.info("当前内存AXB : {} 个, 耗时: {} ms", NumberPoolAxbCache.sizeAll(), watch.getTotalTimeMillis());
        log.info("当前内存AXB master: {} 个", NumberPoolAxbCache.sizeMaster());
        log.info("当前内存AXB slave: {} 个", NumberPoolAxbCache.sizeSlave());

        log.info("当前内存AXE: {} 个", NumberPoolAxeCache.size());
        log.info("当前内存AX: {} 个", NumberPoolAxCache.size());
        log.info("当前内存AXG: {} 个", NumberPoolAxgCache.size());
        log.info("当前内存AXYB_X: {} 个", NumberPoolAxybCache.sizeX());
        log.info("当前内存AXYB_Y: {} 个", NumberPoolAxybCache.sizeY());
        log.info("当前内存AXBN: {} 个", NumberPoolAxbnCache.size());

        log.info("当前内存号码类型关系: {} 个", NumberTypeCache.size());
    }

    /**
     * 刷新企业业务配置信息
     */
    @XxlJobRegister(jobDesc = "刷新JVM本地企业业务配置缓存",
            cron = "0 2 6 1/7 * ?",
            triggerStatus = 1,
            executorRouteStrategy = ExecutorRouteStrategyEnum.SHARDING_BROADCAST)
    @XxlJob("refreshCorpBusiness")
    @PostConstruct
    public void refreshCorpBusiness() {
        // 企业信息
        try {
            List<PrivateCorpBusinessInfo> corpBusinessInfoList = privateCorpBusinessInfoMapper.selectList(null);
            for (PrivateCorpBusinessInfo businessInfo : corpBusinessInfoList) {
                PrivateCorpBusinessInfoDTO privateCorpBusinessInfoDTO = new PrivateCorpBusinessInfoDTO();
                BeanUtil.copyProperties(businessInfo, privateCorpBusinessInfoDTO, true);
                ExpireTimeDTO expireTimeDTO = privateCorpBusinessInfoMapper.selectExpireTime(businessInfo.getVccId());
                if (ObjectUtil.isNotEmpty(expireTimeDTO)) {
                    privateCorpBusinessInfoDTO.setExpireStartTime(expireTimeDTO.getExpireStartTime());
                    privateCorpBusinessInfoDTO.setExpireEndTime(expireTimeDTO.getExpireEndTime());
                }
                CorpBusinessCache.put(businessInfo.getVccId(), privateCorpBusinessInfoDTO);
            }
            log.info("当前存在 {} 个企业", CorpBusinessCache.size());
        } catch (Exception e) {
            log.error("", e);
        }
    }

    @PostConstruct
    public void initExtension() {
        NumberExtPoolCache.init();
    }

    public synchronized void clear() {
        NumberPoolAxbCache.clear();
        NumberPoolAxeCache.clear();
        NumberPoolAxCache.clear();
        NumberPoolAxgCache.clear();
        NumberPoolAxybCache.clear();
        NumberPoolAxbnCache.clear();
        NumberTypeCache.clear();
    }

    public void initXpool(String vccId, String numType, String areaCode, HashSet<String> pool) {

        String axUsablePoolKey = PrivateCacheUtil.getAxUsablePoolKey(vccId, numType, areaCode);
        if (!redissonUtil.isExistSet(axUsablePoolKey)) {

            redissonUtil.setSetString(axUsablePoolKey, pool);
        }
    }

    /**
     * 初始化分机号池子 项目启动时
     */
    public void intiExtPool(String vccId, String numType, String areaCode, String telX) {
        String usableExtPoolListKey = PrivateCacheUtil.getUsableExtPoolListKey(vccId, numType, areaCode, telX);

        if (!redissonUtil.isExistDeque(usableExtPoolListKey)) {
            Optional<PrivateCorpBusinessInfoDTO> businessInfoOptional = corpBusinessService.getCorpBusinessInfo(vccId);
            if (businessInfoOptional.isPresent()) {
                PrivateCorpBusinessInfoDTO businessInfoDTO = businessInfoOptional.get();
                List<String> extNumList = NumberExtPoolCache.getExtNumList(businessInfoDTO.getExtNumCount());
                redissonUtil.setDequeString(usableExtPoolListKey, extNumList);
                return;
            }
            redissonUtil.setSetString(usableExtPoolListKey, NumberExtPoolCache.X_EXT_POOL_10000_LIST);
        }
    }

    //    @PostConstruct
    public void initLuaScript() throws Exception {
        LuaScriptCache.clearScript();
        LuaScriptCache.clearSha1();
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Resource[] resources = resolver.getResources("lua/AXBN.lua");
        Resource resource = resources[0];
        String scriptTempAxbn = IoUtil.readUtf8(resource.getInputStream());

        // AXBN
        for (int i = 3; i <= 8; i++) {
            String keyString = getKeyString(i);
            String addString = getAddString(i);
            String luaScript = String.format(scriptTempAxbn, keyString, addString);
            RScript script = redissonClient.getScript();
            String sha1 = script.scriptLoad(luaScript);
            LuaScriptCache.putScript("AXBN" + i, luaScript);
            LuaScriptCache.putSha1("AXBN" + i, sha1);

            if (redisProperties.getCluster2().getActive()) {
                redissonClient2.getScript().scriptLoad(luaScript);
            }
        }

        // AXB
        String scriptTempAxb = IoUtil.readUtf8(resolver.getResources("lua/AXB.lua")[0].getInputStream());
        String sha1Axb = redissonClient.getScript().scriptLoad(scriptTempAxb);
        LuaScriptCache.putScript("AXB", scriptTempAxb);
        LuaScriptCache.putSha1("AXB", sha1Axb);
        if (redisProperties.getCluster2().getActive()) {
            redissonClient2.getScript().scriptLoad(scriptTempAxb);
        }

        log.info("lua脚本已加载: {}, 个", LuaScriptCache.sizeScript());
        log.info("lua脚本sha已加载: {}, 个", LuaScriptCache.sizeSha1());

    }

    public String getKeyString(Integer keySize) {
        List<String> keysList = new ArrayList<>();
        for (int i = 1; i <= keySize; i++) {
            keysList.add("KEYS[" + i + "]");
        }

        return String.join(",", keysList);
    }

    public String getAddString(Integer keySize) {
        List<String> sremList = new ArrayList<>();

        for (int i = 2; i <= keySize; i++) {
            sremList.add("redis.call('sadd',KEYS[" + i + "],x);");
        }

        return String.join(" ", sremList);
    }

}
