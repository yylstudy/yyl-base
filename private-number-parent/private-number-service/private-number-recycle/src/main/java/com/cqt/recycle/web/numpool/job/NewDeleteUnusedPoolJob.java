package com.cqt.recycle.web.numpool.job;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cqt.common.constants.TableNameConstant;
import com.cqt.common.enums.BusinessTypeEnum;
import com.cqt.common.enums.InitFlagEnum;
import com.cqt.common.enums.NumberTypeEnum;
import com.cqt.common.util.PrivateCacheUtil;
import com.cqt.model.bind.axb.entity.PrivateBindAxbInitUserTelPool;
import com.cqt.model.bind.entity.MtBindInfoAxbHis;
import com.cqt.model.common.properties.HideProperties;
import com.cqt.model.corpinfo.entity.PrivateCorpBusinessInfo;
import com.cqt.recycle.web.corpinfo.mapper.PrivateCorpBusinessInfoMapper;
import com.cqt.recycle.web.numpool.mapper.PrivateBindAxbInitUserTelPoolMapper;
import com.cqt.recycle.web.numpool.mapper.PrivateBindInfoAxbHisMapper;
import com.cqt.recycle.web.numpool.mapper.PrivateCorpNumberPoolMapper;
import com.cqt.redis.util.RedissonUtil;
import com.cqt.xxljob.annotations.XxlJobRegister;
import com.cqt.xxljob.enums.ExecutorRouteStrategyEnum;
import com.google.common.collect.Lists;
import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.api.hint.HintManager;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * @author linshiqiang
 * @date 2021/11/25 10:13
 */
@Component
@Slf4j
public class NewDeleteUnusedPoolJob {

    /**
     * 允许的错误率，错误率越低，所需内存空间就越大
     * fpp 范围：0.0 < fpp < 1
     */
    private static final double FPP = 0.2;

    private static final Integer SIZE = 1000000;

    @Resource
    private RedissonUtil redissonUtil;

    @Resource
    private HideProperties hideProperties;

    @Resource(name = "recycleExecutor")
    private ThreadPoolTaskExecutor recycleExecutor;

    @Resource
    private PrivateCorpBusinessInfoMapper privateCorpBusinessInfoMapper;

    @Resource
    private PrivateCorpNumberPoolMapper privateCorpNumberPoolMapper;

    @Resource
    private PrivateBindInfoAxbHisMapper privateBindInfoAxbHisMapper;

    @Resource
    private PrivateBindAxbInitUserTelPoolMapper privateBindAxbInitUserTelPoolMapper;

    public NewDeleteUnusedPoolJob() {
    }

    @XxlJobRegister(jobDesc = "AXB模式AB号码redis可用池回收",
            cron = "0 0 3 * * ?",
            triggerStatus = 1,
            executorRouteStrategy = ExecutorRouteStrategyEnum.SHARDING_BROADCAST)
    @XxlJob("recycleUsablePoolJobHandler")
    public void recycleUsablePoolJobHandler() {
        int shardIndex = XxlJobHelper.getShardIndex();
        int shardTotal = XxlJobHelper.getShardTotal();
        log.info("分片总数: {}, 当前位置: {}", shardTotal, shardIndex);
        List<String> areaCodeList = privateCorpNumberPoolMapper.selectAreaCode();
        List<String> shardList = new ArrayList<>();
        for (int i = 0; i < areaCodeList.size(); i++) {
            if (i % shardTotal == shardIndex) {
                shardList.add(areaCodeList.get(i));
            }
        }
        if (shardIndex == -1) {
            shardList = areaCodeList;
        }
        log.info("当前分片地市编码: {}", shardList);
        recycleOldNumber(shardList);
    }

    @SuppressWarnings("all")
    public void recycleOldNumber(List<String> areaCodeList) {
        if (!hideProperties.getSwitchs().getScanDbNewFlag()) {
            return;
        }
        long start = System.currentTimeMillis();
        log.info("回收任务开始: {}", DateUtil.now());
        AtomicLong count = new AtomicLong(0);

        // 当前时间0点, 过去 30天
        DateTime endTime = DateUtil.offset(DateUtil.beginOfDay(DateUtil.date()), DateField.SECOND, -1);
        DateTime startTime = DateUtil.offset(endTime, DateField.DAY_OF_YEAR, hideProperties.getOffsetDay());
        DateTime recentStartTime = DateUtil.offset(startTime, DateField.SECOND, 1);
        log.info("发现地市: {}", areaCodeList);

        // 查询企业列表
        List<PrivateCorpBusinessInfo> corpBusinessInfoList = privateCorpBusinessInfoMapper.selectList(null);
        List<String> vccIdList = corpBusinessInfoList.stream()
                .filter(item -> StrUtil.isNotEmpty(item.getBusinessType()))
                .filter(item -> ReUtil.isMatch(item.getBusinessType(), BusinessTypeEnum.AXB.name()))
                .collect(Collectors.toList())
                .stream()
                .map(PrivateCorpBusinessInfo::getVccId)
                .collect(Collectors.toList());
        log.info("发现AXB企业: {} 个", vccIdList);
        for (String vccId : vccIdList) {
            for (String areaCode : areaCodeList) {
                Integer masterSize = Convert.toInt(privateCorpNumberPoolMapper.getMasterNum(vccId, areaCode), 0);
                Integer slaveSize = Convert.toInt(privateCorpNumberPoolMapper.getSlaveNum(vccId, areaCode), 0);
                log.info("vccId: {}, areaCode: {}, 主池数量: {}, 备池池数量: {}", vccId, areaCode, masterSize, slaveSize);
                int allSize = masterSize + slaveSize;
                if (allSize == 0) {
                    log.info("企业: {}, 地市: {}, 无号码", vccId, areaCode);
                    continue;
                }

                // 当前地市axb号码池数量
                // 查询所有AB号码
                List<PrivateBindAxbInitUserTelPool> initUserTelPoolList = null;
                try {
                    initUserTelPoolList = getInitUserTelPoolList(startTime, vccId, areaCode);
                    log.info("vccId: {}, 地市: {}, 有AB用户数: {} 个", vccId, areaCode, initUserTelPoolList.size());
                } catch (Exception e) {
                    log.error("vccId: {}, 查询真实号码异常: ", vccId, e);
                    continue;
                }

                if (CollUtil.isEmpty(initUserTelPoolList)) {
                    log.info("vccId: {}, 地市: {}, 无AB用户", vccId, areaCode);
                    continue;
                }

                BloomFilter<String> telBloomFilter = BloomFilter.create(Funnels.stringFunnel(Charset.defaultCharset()), SIZE, FPP);
                if (hideProperties.getOffsetDay() < 0) {
                    // 放到布隆过滤器中
                    createBloomFilter(endTime, recentStartTime, vccId, areaCode, telBloomFilter);
                    log.info("area_code: {}, bloom fileter count : {}", areaCode, telBloomFilter.approximateElementCount());
                }

                List<List<PrivateBindAxbInitUserTelPool>> partition = Lists.partition(initUserTelPoolList, hideProperties.getScanLimit());
                CountDownLatch countDownLatch = new CountDownLatch(partition.size());
                for (List<PrivateBindAxbInitUserTelPool> numList : partition) {
                    recycleExecutor.execute(() -> {
                        deal(count, areaCode, allSize, masterSize, numList, vccId, telBloomFilter);
                        countDownLatch.countDown();
                    });
                }
                try {
                    countDownLatch.await();
                } catch (InterruptedException e) {
                    log.error("countDownLatch.await error: ", e);
                }
                log.info("vccId: {}, 地市编码: {}, 回收任务结束: {}, 删除 {} 个, 耗时: {} ms", vccId, areaCode, DateUtil.now(), count.get(), System.currentTimeMillis() - start);
            }
        }
        log.info("回收任务结束: {}, 删除 {} 个, 耗时: {} s", DateUtil.now(), count.get(), System.currentTimeMillis() - start);
    }

    /**
     * 查询所有AB号码
     */
    private List<PrivateBindAxbInitUserTelPool> getInitUserTelPoolList(DateTime startTime, String vccId, String areaCode) {
        LambdaQueryWrapper<PrivateBindAxbInitUserTelPool> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PrivateBindAxbInitUserTelPool::getAreaCode, areaCode);
        queryWrapper.lt(PrivateBindAxbInitUserTelPool::getCreateTime, startTime);
        List<PrivateBindAxbInitUserTelPool> initUserTelPoolList;
        try (HintManager hintManager = HintManager.getInstance()) {
            hintManager.addTableShardingValue(TableNameConstant.PRIVATE_BIND_AXB_INIT_USER_TEL_POOL, vccId);
            initUserTelPoolList = privateBindAxbInitUserTelPoolMapper.selectList(queryWrapper);
        }
        return initUserTelPoolList;
    }

    @SuppressWarnings("all")
    private void createBloomFilter(DateTime endTime, DateTime recentStartTime, String vccId, String areaCode, BloomFilter<String> telBloomFilter) {
        try (HintManager hintManager = HintManager.getInstance()) {
            hintManager.addTableShardingValue(TableNameConstant.PRIVATE_BIND_INFO_AXB_HIS, vccId);
            List<MtBindInfoAxbHis> selectTelList = privateBindInfoAxbHisMapper.selectTelList(areaCode, recentStartTime, endTime);
            selectTelList.forEach(item -> {
                telBloomFilter.put(item.getTelA());
                telBloomFilter.put(item.getTelB());
            });
        }
    }

    private void deal(AtomicLong count, String areaCode, int allSize, int masterSize,
                      List<PrivateBindAxbInitUserTelPool> numList, String vccId, BloomFilter<String> telBloomFilter) {
        for (PrivateBindAxbInitUserTelPool initUserTelPool : numList) {
            String usableKey = PrivateCacheUtil.getUsablePoolKey(initUserTelPool.getVccId(), NumberTypeEnum.AXB.name(), initUserTelPool.getAreaCode(), initUserTelPool.getTel());
            String initKey = PrivateCacheUtil.getInitFlagKey(initUserTelPool.getVccId(), NumberTypeEnum.AXB.name(), initUserTelPool.getAreaCode(), initUserTelPool.getTel());
            int size = masterSize;
            if (InitFlagEnum.SECOND_INIT.getCode().equals(Convert.toStr(initUserTelPool.getInitFlag()))) {
                // 使用全部号码池
                size = allSize;
            }
            // 1. 查询初始化标志 是使用主池还是备池
            int setCount = redissonUtil.getSetCount(usableKey);
            if (setCount == size) {

                // 查询A/B号码在getOffsetDay天内是否有过绑定关系
                if (hideProperties.getOffsetDay() < 0) {
                    // 查询A/B号码在两天内是否有过绑定关系
                    boolean contain = telBloomFilter.mightContain(initUserTelPool.getTel());
                    if (contain) {
                        log.debug("vccId: {}, area_code: {}, tel: {}, {}天内已初始化过, 不回收", vccId, areaCode, initUserTelPool.getTel(), hideProperties.getOffsetDay());
                        continue;
                    }
                }

                boolean delKey = redissonUtil.delKey(initKey);
                if (delKey) {
                    count.incrementAndGet();
                    // 删除init表 private_bind_axb_init_user_tel_pool
                    int delete = 0;
                    try (HintManager hintManager = HintManager.getInstance()) {
                        hintManager.addTableShardingValue(TableNameConstant.PRIVATE_BIND_AXB_INIT_USER_TEL_POOL, vccId);
                        delete = privateBindAxbInitUserTelPoolMapper.deleteById(vccId + ":" + areaCode + ":" + initUserTelPool.getTel());
                    }

                    boolean delSet = redissonUtil.delSet(usableKey);
                    log.info("地市: {}, 成功删除空闲key: {}, init: {}, pool: {}, del init db: {}", areaCode, usableKey, delKey, delSet, delete);
                } else {
                    log.error("删除initKey失败, key: {}", initKey);
                }
            }
        }
    }

}
