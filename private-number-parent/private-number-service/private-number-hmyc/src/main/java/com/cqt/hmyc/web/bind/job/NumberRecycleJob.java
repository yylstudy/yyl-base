package com.cqt.hmyc.web.bind.job;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cqt.common.constants.TableNameConstant;
import com.cqt.common.enums.BusinessTypeEnum;
import com.cqt.common.util.BindIdUtil;
import com.cqt.common.util.PrivateCacheUtil;
import com.cqt.hmyc.web.bind.mapper.PrivateRecyclePushFailMapper;
import com.cqt.hmyc.web.bind.mapper.ax.PrivateBindInfoAxMapper;
import com.cqt.hmyc.web.bind.mapper.axb.PrivateBindInfoAxbMapper;
import com.cqt.hmyc.web.bind.mapper.axe.PrivateBindInfoAxeMapper;
import com.cqt.hmyc.web.bind.service.ax.AxBindConverter;
import com.cqt.hmyc.web.bind.service.axb.AxbBindConverter;
import com.cqt.hmyc.web.bind.service.axe.AxeBindConverter;
import com.cqt.hmyc.web.bind.service.recycle.NumberRecycleService;
import com.cqt.hmyc.web.cache.NumberExtPoolCache;
import com.cqt.hmyc.web.corpinfo.mapper.PrivateCorpBusinessInfoMapper;
import com.cqt.hmyc.web.numpool.mapper.PrivateNumberInfoMapper;
import com.cqt.model.bind.ax.entity.PrivateBindInfoAx;
import com.cqt.model.bind.axb.entity.PrivateBindInfoAxb;
import com.cqt.model.bind.axe.entity.PrivateBindInfoAxe;
import com.cqt.model.bind.dto.BindRecycleDTO;
import com.cqt.model.bind.entity.PrivateRecyclePushFail;
import com.cqt.model.corpinfo.entity.PrivateCorpBusinessInfo;
import com.cqt.model.numpool.entity.PrivateNumberInfo;
import com.cqt.xxljob.annotations.XxlJobRegister;
import com.cqt.xxljob.enums.ExecutorRouteStrategyEnum;
import com.cqt.xxljob.util.XxlJobUtil;
import com.google.common.collect.Lists;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.api.hint.HintManager;
import org.redisson.api.RList;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @author linshiqiang
 * date 2021/11/1 14:17
 * 推送延时队列 异常时 重新回收
 */
@Slf4j
@Component
public class NumberRecycleJob {

    private static final Integer BATCH_COUNT = 20000;

    private static final Integer EXTENSION_COUNT = 10000;

    private static final Integer EXTENSION_LENGTH = 4;

    private final PrivateBindInfoAxMapper privateBindInfoAxMapper;

    private final PrivateBindInfoAxbMapper privateBindInfoAxbMapper;

    private final PrivateBindInfoAxeMapper privateBindInfoAxeMapper;

    private final PrivateRecyclePushFailMapper privateRecyclePushFailMapper;

    private final PrivateNumberInfoMapper privateNumberInfoMapper;

    private final PrivateCorpBusinessInfoMapper privateCorpBusinessInfoMapper;

    private final AxBindConverter axBindConverter;

    private final AxbBindConverter axbBindConverter;

    private final AxeBindConverter axeBindConverter;

    private final NumberRecycleService numberRecycleService;

    private final RedissonClient redissonClient;

    private final RedissonClient redissonClient2;

    public NumberRecycleJob(PrivateBindInfoAxMapper privateBindInfoAxMapper,
                            PrivateBindInfoAxbMapper privateBindInfoAxbMapper,
                            PrivateBindInfoAxeMapper privateBindInfoAxeMapper,
                            PrivateRecyclePushFailMapper privateRecyclePushFailMapper,
                            PrivateNumberInfoMapper privateNumberInfoMapper,
                            PrivateCorpBusinessInfoMapper privateCorpBusinessInfoMapper,
                            AxBindConverter axBindConverter,
                            AxbBindConverter axbBindConverter,
                            AxeBindConverter axeBindConverter,
                            NumberRecycleService numberRecycleService,
                            @Qualifier("redissonClient")
                            RedissonClient redissonClient,
                            @Qualifier("redissonClient2")
                            @Autowired(required = false)
                            RedissonClient redissonClient2) {
        this.privateBindInfoAxMapper = privateBindInfoAxMapper;
        this.privateBindInfoAxbMapper = privateBindInfoAxbMapper;
        this.privateBindInfoAxeMapper = privateBindInfoAxeMapper;
        this.privateRecyclePushFailMapper = privateRecyclePushFailMapper;
        this.privateNumberInfoMapper = privateNumberInfoMapper;
        this.privateCorpBusinessInfoMapper = privateCorpBusinessInfoMapper;
        this.axBindConverter = axBindConverter;
        this.axbBindConverter = axbBindConverter;
        this.axeBindConverter = axeBindConverter;
        this.numberRecycleService = numberRecycleService;
        this.redissonClient = redissonClient;
        this.redissonClient2 = redissonClient2;
    }

    @XxlJobRegister(jobDesc = "AXB回收昨日过期绑定关系",
            cron = "0 10 3 * * ?",
            triggerStatus = 1,
            executorRouteStrategy = ExecutorRouteStrategyEnum.SHARDING_BROADCAST)
    @XxlJob("dealAxbRecycleFailJobHandler")
    public void dealAxbRecycleFailJobHandler() {
        try {
            // 查询AXB号码
            List<PrivateNumberInfo> numberInfoList = getNumberList(BusinessTypeEnum.AXB.name());
            log.info("AXB号码总数: {}", numberInfoList.size());
            if (CollUtil.isEmpty(numberInfoList)) {
                return;
            }
            Map<String, String> businessTypeMap = getBusinessTypeMap();
            List<PrivateNumberInfo> shardList = XxlJobUtil.getShardList(numberInfoList);
            log.info("当前分片AXB号码有: {}", shardList.size());
            for (PrivateNumberInfo numberInfo : numberInfoList) {
                String vccId = numberInfo.getVccId();
                String number = numberInfo.getNumber();
                try {
                    if (isMatch(businessTypeMap, vccId, BusinessTypeEnum.AXB)) {
                        recycleAxb(vccId, number);
                    }
                } catch (Exception e) {
                    log.error("AXB vccId: {}, number: {}, 绑定关系回收处理异常: ", vccId, number, e);
                }
            }
            // 按企业再过滤下
            if (XxlJobHelper.getShardIndex() == 0) {
                List<String> vccIdList = getVccIdList(BusinessTypeEnum.AXB);
                for (String vccId : vccIdList) {
                    try {
                        if (isMatch(businessTypeMap, vccId, BusinessTypeEnum.AXB)) {
                            recycleAxb(vccId, "");
                        }
                    } catch (Exception e) {
                        log.error("AXB vccId: {}, 绑定关系回收处理异常: ", vccId, e);
                    }
                }
            }
        } catch (Exception e) {
            log.error("dealAxbRecycleFailJobHandler task run error: ", e);
        }
    }

    private Map<String, String> getBusinessTypeMap() {
        LambdaQueryWrapper<PrivateCorpBusinessInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.select(PrivateCorpBusinessInfo::getVccId, PrivateCorpBusinessInfo::getBusinessType);
        return privateCorpBusinessInfoMapper.selectList(queryWrapper)
                .stream()
                .filter(item -> StrUtil.isNotEmpty(item.getBusinessType()))
                .collect(Collectors.toMap(PrivateCorpBusinessInfo::getVccId, PrivateCorpBusinessInfo::getBusinessType));
    }

    private Boolean isMatch(Map<String, String> businessTypeMap, String vccId, BusinessTypeEnum businessTypeEnum) {
        String businessType = businessTypeMap.get(vccId);
        if (StrUtil.isEmpty(businessType)) {
            return false;
        }
        return ReUtil.isMatch(businessType, businessTypeEnum.name());
    }

    private void recycleAxb(String vccId, String number) {
        LambdaQueryWrapper<PrivateBindInfoAxb> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(StrUtil.isNotEmpty(number), PrivateBindInfoAxb::getTelX, number);
        queryWrapper.lt(PrivateBindInfoAxb::getExpireTime, DateUtil.offsetHour(DateUtil.date(), -1));
        Integer count;
        try (HintManager hintManager = HintManager.getInstance()) {
            hintManager.addTableShardingValue(TableNameConstant.PRIVATE_BIND_INFO_AXB, vccId);
            count = privateBindInfoAxbMapper.selectCount(queryWrapper);
        }
        if (count == 0) {
            log.info("AXB vccId: {}, 号码: {}, 没有已过期的绑定关系", vccId, number);
            return;
        }
        log.info("AXB vccId: {}, 号码: {}, 已过期的绑定关系有: {}", vccId, number, count);
        int page = count / BATCH_COUNT + 1;
        queryWrapper.last("limit " + BATCH_COUNT);
        for (int i = 0; i < page; i++) {
            List<PrivateBindInfoAxb> bindInfoAxbList;
            try (HintManager hintManager = HintManager.getInstance()) {
                hintManager.addTableShardingValue(TableNameConstant.PRIVATE_BIND_INFO_AXB, vccId);
                bindInfoAxbList = privateBindInfoAxbMapper.selectList(queryWrapper);
            }
            if (CollUtil.isNotEmpty(bindInfoAxbList)) {
                for (PrivateBindInfoAxb bindInfoAxb : bindInfoAxbList) {
                    BindRecycleDTO bindRecycleDTO = axbBindConverter.bindInfoAxb2BindRecycleDTO(bindInfoAxb);
                    bindRecycleDTO.setNumType(BusinessTypeEnum.AXB.name());
                    numberRecycleService.recycleNumber(bindRecycleDTO);
                }
            }
        }
        log.info("AXB vccId: {}, 号码: {}, 处理已过期绑定关系: {}", vccId, number, count);
    }

    @XxlJobRegister(jobDesc = "AXE回收昨日过期绑定关系",
            cron = "0 20 3 * * ?",
            triggerStatus = 1,
            executorRouteStrategy = ExecutorRouteStrategyEnum.SHARDING_BROADCAST)
    @XxlJob("dealAxeRecycleFailJobHandler")
    public void dealAxeRecycleFailJobHandler() {
        try {
            // 查询AXE号码
            List<PrivateNumberInfo> numberInfoList = getNumberList(BusinessTypeEnum.AXE.name());
            log.info("AXE号码总数: {}", numberInfoList.size());
            if (CollUtil.isEmpty(numberInfoList)) {
                return;
            }
            List<PrivateNumberInfo> infoList = XxlJobUtil.getShardList(numberInfoList);
            log.info("当前分片AXE号码有: {}", infoList.size());
            for (PrivateNumberInfo numberInfo : infoList) {
                // 查询号码绑定数据, 最多10000条
                List<PrivateBindInfoAxe> bindInfoAxeList = null;
                try (HintManager hintManager = HintManager.getInstance()) {
                    String numberHash = BindIdUtil.getHash(numberInfo.getNumber());
                    String sharingKey = numberInfo.getVccId() + StrUtil.AT + numberHash;
                    hintManager.addDatabaseShardingValue(TableNameConstant.PRIVATE_BIND_INFO_AXE, numberHash);
                    hintManager.addTableShardingValue(TableNameConstant.PRIVATE_BIND_INFO_AXE, sharingKey);
                    LambdaQueryWrapper<PrivateBindInfoAxe> queryWrapper = new LambdaQueryWrapper<>();
                    queryWrapper.eq(PrivateBindInfoAxe::getTelX, numberInfo.getNumber());
                    // 查询一小时前过期的
                    queryWrapper.lt(PrivateBindInfoAxe::getExpireTime, DateUtil.offset(DateUtil.date(), DateField.HOUR, -1));
                    queryWrapper.last("limit " + EXTENSION_COUNT);
                    bindInfoAxeList = privateBindInfoAxeMapper.selectList(queryWrapper);
                } catch (Exception e) {
                    log.error("axe select err: ", e);
                }
                if (CollUtil.isEmpty(bindInfoAxeList)) {
                    log.info("AXE vccId: {}, 号码: {}, 没有已过期的绑定关系", numberInfo.getVccId(), numberInfo.getNumber());
                    continue;
                }
                for (PrivateBindInfoAxe bindInfo : bindInfoAxeList) {
                    BindRecycleDTO bindRecycleDTO = axeBindConverter.bindInfo2BindRecycleDTO(bindInfo);
                    bindRecycleDTO.setNumType(BusinessTypeEnum.AXE.name());
                    numberRecycleService.recycleNumber(bindRecycleDTO);
                }
                log.info("AXE vccId: {}, 号码: {}, 处理已过期绑定关系: {}", numberInfo.getVccId(), numberInfo.getNumber(), bindInfoAxeList.size());
            }
        } catch (Exception e) {
            log.error("dealAxeRecycleFailJobHandler task run error: ", e);
        }
    }

    @XxlJobRegister(jobDesc = "AX回收昨日过期绑定关系",
            cron = "0 30 3 * * ?",
            triggerStatus = 1,
            executorRouteStrategy = ExecutorRouteStrategyEnum.ROUND)
    @XxlJob("dealAxRecycleFailJobHandler")
    public void dealAxRecycleFailJobHandler() {
        try {
            // 查询AX号码
            List<PrivateNumberInfo> numberInfoList = getNumberList(BusinessTypeEnum.AX.name());
            log.info("AX号码总数: {}", numberInfoList.size());
            if (CollUtil.isEmpty(numberInfoList)) {
                return;
            }
            for (PrivateNumberInfo numberInfo : numberInfoList) {
                List<PrivateBindInfoAx> bindInfoAxList = null;
                try (HintManager hintManager = HintManager.getInstance()) {
                    hintManager.addTableShardingValue(TableNameConstant.PRIVATE_BIND_INFO_AX, numberInfo.getVccId());
                    LambdaQueryWrapper<PrivateBindInfoAx> queryWrapper = new LambdaQueryWrapper<>();
                    queryWrapper.eq(PrivateBindInfoAx::getTelX, numberInfo.getNumber());
                    // 查询一小时前过期的
                    queryWrapper.lt(PrivateBindInfoAx::getExpireTime, DateUtil.offsetHour(DateUtil.date(), -1));
                    queryWrapper.last("limit " + EXTENSION_COUNT);
                    bindInfoAxList = privateBindInfoAxMapper.selectList(queryWrapper);
                } catch (Exception e) {
                    log.error("ax select err: ", e);
                }
                if (CollUtil.isEmpty(bindInfoAxList)) {
                    log.info("AX vccId: {}, 号码: {}, 没有已过期的绑定关系", numberInfo.getVccId(), numberInfo.getNumber());
                    continue;
                }
                for (PrivateBindInfoAx bindInfoAx : bindInfoAxList) {
                    BindRecycleDTO bindRecycleDTO = axBindConverter.bindInfoAx2BindRecycleDTO(bindInfoAx);
                    bindRecycleDTO.setNumType(BusinessTypeEnum.AX.name());
                    numberRecycleService.recycleNumber(bindRecycleDTO);
                }
                log.info("AX vccId: {}, 号码: {}, 处理已过期绑定关系: {}", numberInfo.getVccId(), numberInfo.getNumber(), bindInfoAxList.size());
            }
        } catch (Exception e) {
            log.error("dealAxRecycleFailJobHandler task run error: ", e);
        }
    }

    @XxlJobRegister(jobDesc = "处理发送mq失败的绑定关系数据(mysql)",
            cron = "0 40 3 * * ?",
            triggerStatus = 1,
            executorRouteStrategy = ExecutorRouteStrategyEnum.ROUND)
    @XxlJob("dealPushMqFailDataJobHandler")
    public void dealPushMqFailDataJobHandler() {
        try {
            Integer count = privateRecyclePushFailMapper.selectCount(null);
            int page = count / BATCH_COUNT + 1;

            for (int i = 0; i < page; i++) {
                LambdaQueryWrapper<PrivateRecyclePushFail> queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.last("limit " + BATCH_COUNT);
                List<PrivateRecyclePushFail> pushFailList = privateRecyclePushFailMapper.selectList(queryWrapper);

                for (PrivateRecyclePushFail pushFail : pushFailList) {
                    BindRecycleDTO bindRecycleDTO = axbBindConverter.privateRecyclePushFail2BindRecycleDTO(pushFail);
                    numberRecycleService.recycleNumber(bindRecycleDTO);
                }
            }
        } catch (Exception e) {
            log.error("dealAxRecycleFailJobHandler task run error: ", e);
        }
    }

    @XxlJobRegister(jobDesc = "检查AXE分机号是否有回收失败",
            cron = "0 10 5 * * ?",
            executorRouteStrategy = ExecutorRouteStrategyEnum.SHARDING_BROADCAST)
    @XxlJob("checkAxeExtensionNumberJobHandler")
    public void checkAxeExtensionNumber() {
        // 查询AXE号码
        List<PrivateNumberInfo> numberInfoList = getNumberList(BusinessTypeEnum.AXE.name());
        log.info("AXE号码总数: {}", numberInfoList.size());
        if (CollUtil.isEmpty(numberInfoList)) {
            return;
        }
        List<PrivateNumberInfo> shardList = XxlJobUtil.getShardList(numberInfoList);
        log.info("当前分片AXE号码有: {}", shardList.size());
        for (PrivateNumberInfo numberInfo : shardList) {
            // db 已保存的分机号
            dealExtensionSingle(numberInfo);
        }
    }

    public void dealExtensionSingle(PrivateNumberInfo numberInfo) {
        List<String> dbUnRecycleExtensionList = getNumberOfExt(numberInfo.getVccId(), numberInfo.getNumber());
        checkExtension(numberInfo.getVccId(), numberInfo.getAreaCode(), numberInfo.getNumber(),
                dbUnRecycleExtensionList,
                redissonClient);
        if (ObjectUtil.isNotEmpty(redissonClient2)) {
            checkExtension(numberInfo.getVccId(), numberInfo.getAreaCode(), numberInfo.getNumber(),
                    dbUnRecycleExtensionList,
                    redissonClient2);
        }
        dbUnRecycleExtensionList.clear();
    }

    /**
     * 1. 查表中X号码的分机号列表,
     * 2. 10000个分机号 差 1. 正常可用的分机号
     * 3. 查询redis X号码的分机号列表,
     * 4. 2.差3., 少了哪些
     */
    private void checkExtension(String vccId, String areaCode, String number, List<String> dbUnRecycleExtensionList,
                                RedissonClient redissonClient) {
        // 全部分机号
        List<String> totalList = NumberExtPoolCache.X_EXT_POOL_10000_LIST;
        if (CollUtil.isEmpty(totalList)) {
            return;
        }

        // redis未使用分机号
        String usableExtPoolListKey = PrivateCacheUtil.getUsableExtPoolListKey(vccId,
                BusinessTypeEnum.AXE.name(), areaCode, number);
        // 本地redis
        RList<String> rList = redissonClient.getList(usableExtPoolListKey);
        List<String> redisUsableExtensionList = rList.readAll()
                .stream()
                .distinct()
                .collect(Collectors.toList());
        if (redisUsableExtensionList.size() == EXTENSION_COUNT) {
            return;
        }
        // db未被使用
        List<String> usabelList = CollUtil.subtractToList(totalList, dbUnRecycleExtensionList);
        List<String> addList = CollUtil.subtractToList(usabelList, redisUsableExtensionList);
        if (CollUtil.isEmpty(addList)) {
            return;
        }
        addList = addList.stream()
                .distinct()
                .filter(e -> StrUtil.isNotEmpty(e) && e.length() == EXTENSION_LENGTH)
                .collect(Collectors.toList());
        // 添加到redis
        boolean set = redissonClient.getDeque(usableExtPoolListKey).addAll(addList);
        log.info("AXE, vccId: {}, areaCode: {}, number: {}, 需要回收的分机号个数: {}, result: {}", vccId, areaCode,
                number, addList.size(), set);
        redisUsableExtensionList.clear();
        usabelList.clear();
        addList.clear();
    }

    private List<String> getNumberOfExt(String vccId, String number) {
        try (HintManager hintManager = HintManager.getInstance()) {
            String numberHash = BindIdUtil.getHash(number);
            String sharingKey = vccId + StrUtil.AT + numberHash;
            hintManager.addDatabaseShardingValue(TableNameConstant.PRIVATE_BIND_INFO_AXE, numberHash);
            hintManager.addTableShardingValue(TableNameConstant.PRIVATE_BIND_INFO_AXE, sharingKey);
            LambdaQueryWrapper<PrivateBindInfoAxe> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(PrivateBindInfoAxe::getTelX, number);
            queryWrapper.select(PrivateBindInfoAxe::getTelXExt);
            queryWrapper.last("limit " + EXTENSION_COUNT);
            // db已使用分机号
            return privateBindInfoAxeMapper.selectList(queryWrapper)
                    .stream()
                    .map(PrivateBindInfoAxe::getTelXExt)
                    .distinct()
                    .collect(Collectors.toList());

        }
    }

    /**
     * 查询业务模式已分配的x号码
     */
    @SuppressWarnings("unchecked")
    private List<PrivateNumberInfo> getNumberList(String poolType) {
        LambdaQueryWrapper<PrivateNumberInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PrivateNumberInfo::getPoolType, poolType);
        queryWrapper.eq(PrivateNumberInfo::getAllocationFlag, 1);
        queryWrapper.select(PrivateNumberInfo::getVccId, PrivateNumberInfo::getNumber, PrivateNumberInfo::getAreaCode);
        queryWrapper.orderByAsc(PrivateNumberInfo::getNumber);
        return privateNumberInfoMapper.selectList(queryWrapper);
    }

    /**
     * 获取开通此业务模式的企业id
     */
    private List<String> getVccIdList(BusinessTypeEnum businessTypeEnum) {
        LambdaQueryWrapper<PrivateCorpBusinessInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.select(PrivateCorpBusinessInfo::getVccId, PrivateCorpBusinessInfo::getBusinessType);
        List<PrivateCorpBusinessInfo> list = privateCorpBusinessInfoMapper.selectList(queryWrapper);
        return list.stream()
                .filter(item -> StrUtil.isNotEmpty(item.getBusinessType()))
                .filter(item -> ReUtil.isMatch(item.getBusinessType(), businessTypeEnum.name()))
                .map(PrivateCorpBusinessInfo::getVccId)
                .collect(Collectors.toList());
    }

    public Map<String, String> getExtensionCount() {
        List<PrivateNumberInfo> numberInfoList = getNumberList("AXE");
        Map<String, String> result = new ConcurrentHashMap<>(16);

        List<List<PrivateNumberInfo>> list = Lists.partition(numberInfoList, 100);

        List<Boolean> booleanList = list.stream()
                .map(e -> {
                    return CompletableFuture.supplyAsync(() -> {
                        for (PrivateNumberInfo numberInfo : e) {
                            String usableExtPoolListKey = PrivateCacheUtil.getUsableExtPoolListKey(numberInfo.getVccId(),
                                    BusinessTypeEnum.AXE.name(), numberInfo.getAreaCode(), numberInfo.getNumber());
                            int local = redissonClient.getList(usableExtPoolListKey).size();
                            int back = redissonClient2.getList(usableExtPoolListKey).size();
                            result.put(usableExtPoolListKey, local + StrUtil.AT + back);
                        }
                        return true;
                    });
                }).collect(Collectors.toList())
                .stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList());
        log.info("result: {}", booleanList);
        return result;
    }
}
