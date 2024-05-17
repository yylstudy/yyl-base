package com.cqt.monitor.web.switchroom.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.ConfigType;
import com.alibaba.nacos.api.exception.NacosException;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cqt.common.constants.SystemConstant;
import com.cqt.common.enums.ServerEnum;
import com.cqt.model.bind.entity.PrivateAreaLocation;
import com.cqt.model.monitor.dto.HealthCheckResultDTO;
import com.cqt.model.monitor.entity.PrivateMonitorErrorLog;
import com.cqt.model.monitor.entity.PrivateMonitorInfo;
import com.cqt.monitor.cache.LocalCache;
import com.cqt.monitor.common.util.DingUtil;
import com.cqt.monitor.web.switchroom.mapper.PrivateAreaLocationMapper;
import com.cqt.monitor.web.switchroom.mapper.PrivateMonitorErrorLogMapper;
import com.cqt.monitor.web.switchroom.mapper.PrivateMonitorInfoMapper;
import com.cqt.monitor.web.switchroom.rabbitmq.RabbitmqFactory;
import com.cqt.monitor.web.switchroom.rabbitmq.RabbitmqUri;
import com.cqt.monitor.web.switchroom.redis.RedisClientFactory;
import com.cqt.monitor.web.switchroom.redis.RedisNode;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author linshiqiang
 * @since 2022/1/24 15:50
 * 绑定关系, redis rabbitmq 通用健康检测
 */
@Slf4j
public class CommonBindHealthCheck {

    private final static AtomicLong ERROR_TIME = new AtomicLong(0);

    private final String business;

    private final String businessBind;

    private final String bindRabbitmqA;

    private final String bindRabbitmqB;

    private final String bindRedisA;

    private final String bindRedisB;

    private final String nacosA;

    private final String nacosB;

    private final PrivateMonitorErrorLogMapper privateMonitorErrorLogMapper;

    private final PrivateAreaLocationMapper areaLocationMapper;

    private final PrivateMonitorInfoMapper privateMonitorInfoMapper;

    private final DingUtil dingUtil;

    public CommonBindHealthCheck(String business, PrivateMonitorErrorLogMapper privateMonitorErrorLogMapper,
                                 PrivateAreaLocationMapper areaLocationMapper, PrivateMonitorInfoMapper privateMonitorInfoMapper, DingUtil dingUtil) {
        this.business = business;
        businessBind = business + "-BIND";
        bindRabbitmqA = business + "-BIND-RABBITMQ-A";
        bindRabbitmqB = business + "-BIND-RABBITMQ-B";
        bindRedisA = business + "-BIND-REDIS-A";
        bindRedisB = business + "-BIND-REDIS-B";
        nacosA = business + "-NACOS-A";
        nacosB = business + "-NACOS-B";
        this.privateMonitorErrorLogMapper = privateMonitorErrorLogMapper;
        this.areaLocationMapper = areaLocationMapper;
        this.privateMonitorInfoMapper = privateMonitorInfoMapper;
        this.dingUtil = dingUtil;
    }

    /**
     * 健康检测入口
     */
    public void healthCheck() {
        log.info("start check bind mq redis health, business: {}, now: {}", businessBind, DateUtil.now());
        // redis 检测结果
        HealthCheckResultDTO redisCheck = redisCheck();
        boolean roomRedisA = redisCheck.getRoomRedisA();
        boolean roomRedisB = redisCheck.getRoomRedisB();

        // rabbitmq 检测结果
        HealthCheckResultDTO rabbitmqCheck = rabbitmqCheck();
        boolean roomRabbitmqA = rabbitmqCheck.getRoomRabbitmqA();
        boolean roomRabbitmqB = rabbitmqCheck.getRoomRabbitmqB();

        // redis 均异常 不切
        if (roomRedisA && roomRedisB) {
            dingUtil.sendMessage(businessBind + " 已持续检测到异常" + ERROR_TIME.incrementAndGet() + "次, 此时AB机房 redis均异常, 绑定关系不切换!!!");
            log.info("business: {}, 两侧redis均存在异常", businessBind);
            return;
        }

        // A机房redis异常, B机房正常, meituan-area-location.json 都改成B
        if (roomRedisA) {
            log.info("business: {}, redis room a exist fail", businessBind);
            if (!isToggle()) {
                toggle(SystemConstant.B, businessBind + " 已持续检测到异常" + ERROR_TIME.incrementAndGet() + "次, 此时A机房 redis异常, 绑定关系全部切换到B机房!!!");
                updateToggle(1);
            }
            return;
        }

        // B机房redis异常, A机房正常, meituan-area-location.json 都改成A
        if (roomRedisB) {
            log.info("business: {}, redis room b exist fail", businessBind);
            if (!isToggle()) {
                toggle(SystemConstant.A, businessBind + " 已持续检测到异常" + ERROR_TIME.incrementAndGet() + "次, 此时B机房 redis异常, 绑定关系全部切换到A机房!!!");
                updateToggle(1);
            }
            return;
        }

        if (roomRabbitmqA && roomRabbitmqB) {
            dingUtil.sendMessage(businessBind + " 已持续检测到异常" + ERROR_TIME.incrementAndGet() + "次, 此时AB机房 rabbitmq均异常, 绑定关系不切换!!!");
            log.info("business: {}, 两侧rabbitmq均存在异常", businessBind);
            return;
        }

        if (roomRabbitmqA) {
            log.info("business: {}, rabbitmq room a exist fail", businessBind);
            // 查询 toggle = 1 and type = nacos 有记录, 还未进行灾备切换
            if (!isToggle()) {
                toggle(SystemConstant.B,businessBind + " 已持续检测到异常" + ERROR_TIME.incrementAndGet() + "次, 此时A机房 rabbitmq异常, 绑定关系全部切换到B机房!!!");
                // 灾备切换完成, 修改 toggle = 1 and type = nacos and business = MT
                updateToggle(1);
            }
            return;
        }
        if (roomRabbitmqB) {
            log.info("business: {}, rabbitmq room b exist fail", businessBind);
            if (!isToggle()) {
                toggle(SystemConstant.A, businessBind + " 已持续检测到异常" + ERROR_TIME.incrementAndGet() + "次, 此时B机房 rabbitmq异常, 绑定关系全部切换到A机房!!!");
                updateToggle(1);
            }
            return;
        }

        // 恢复, 控制只恢复一次, 查询到toggle = 1 and type = nacos and business = MT, 有记录, 则之前发生过灾备切换, 进行恢复
        if (isToggle()) {
            toggle("", StrUtil.format("{}, redis或mq已恢复正常, 此时AB机房nacos配置已恢复至初始状态", businessBind));
            // 恢复到灾备之前的状态, 修改 toggle = 0
            updateToggle(0);
            ERROR_TIME.set(0);
            privateMonitorInfoMapper.updateStatusByBusiness(1, businessBind);
        }
        log.info("finish check bind mq redis health, business: {}, now: {}", businessBind, DateUtil.now());
    }

    /**
     * rabbitmq 健康检测
     */
    private HealthCheckResultDTO rabbitmqCheck() {
        boolean roomRabbitmqA = false;
        boolean roomRabbitmqB = false;
        boolean healthCheckA = RabbitmqFactory.healthCheck(LocalCache.getRabbitmqInfoCache(bindRabbitmqA));
        boolean healthCheckB = RabbitmqFactory.healthCheck(LocalCache.getRabbitmqInfoCache(bindRabbitmqB));

        PrivateMonitorErrorLog errorLog = PrivateMonitorErrorLog.builder()
                .createTime(DateUtil.date())
                .place(SystemConstant.A)
                .type(ServerEnum.rabbitmq.name())
                .business(businessBind)
                .build();
        if (!healthCheckA) {
            roomRabbitmqA = true;
            RabbitmqUri rabbitmqInfoCache = LocalCache.getRabbitmqInfoCache(bindRabbitmqA);
            errorLog.setId(IdUtil.fastSimpleUUID());
            errorLog.setClusterName(bindRabbitmqA);
            errorLog.setLog(JSON.toJSONString(rabbitmqInfoCache.getHostAndPortSet()));
            int insert = privateMonitorErrorLogMapper.insert(errorLog);
            log.info("business: {}, rabbitmq a room test error: {}, insert: {}", businessBind, errorLog, insert);
            updateStatus("", null, bindRabbitmqA);
            dingUtil.sendMessage(businessBind + " 已持续检测到异常" + ERROR_TIME.incrementAndGet() + "次, 此时A机房 rabbitmq异常: " + JSON.toJSONString(rabbitmqInfoCache.getHostAndPortSet()));
        }
        if (!healthCheckB) {
            roomRabbitmqB = true;
            RabbitmqUri rabbitmqInfoCache = LocalCache.getRabbitmqInfoCache(bindRabbitmqB);
            errorLog.setId(IdUtil.fastSimpleUUID());
            errorLog.setPlace(SystemConstant.B);
            errorLog.setClusterName(bindRabbitmqB);
            errorLog.setLog(JSON.toJSONString(rabbitmqInfoCache.getHostAndPortSet()));
            int insert = privateMonitorErrorLogMapper.insert(errorLog);
            log.info("business: {}, rabbitmq b room test error: {}, insert: {}", businessBind, errorLog, insert);
            updateStatus("", null, bindRabbitmqB);
            dingUtil.sendMessage(businessBind + " 已持续检测到异常" + ERROR_TIME.incrementAndGet() + "次, 此时B机房 rabbitmq异常: " + JSON.toJSONString(rabbitmqInfoCache.getHostAndPortSet()));
        }
        return HealthCheckResultDTO.builder()
                .roomRabbitmqA(roomRabbitmqA)
                .roomRabbitmqB(roomRabbitmqB)
                .build();
    }

    /**
     * redis 健康检测
     */
    private HealthCheckResultDTO redisCheck() {
        boolean roomRedisA = false;
        boolean roomRedisB = false;
        PrivateMonitorErrorLog errorLog = PrivateMonitorErrorLog.builder()
                .createTime(DateUtil.date())
                .place(SystemConstant.A)
                .type(ServerEnum.redis.name())
                .business(businessBind)
                .build();
        try {
            List<RedisNode> nodeListA = RedisClientFactory.getFailNode(LocalCache.getRedisInfoCache(bindRedisA));
            if (CollUtil.isNotEmpty(nodeListA)) {
                roomRedisA = true;
                errorLog.setId(IdUtil.fastSimpleUUID());
                errorLog.setPlace(SystemConstant.A);
                errorLog.setClusterName(bindRedisA);
                errorLog.setLog(JSON.toJSONString(nodeListA));
                int insert = privateMonitorErrorLogMapper.insert(errorLog);
                log.info("business: {}, redis a room test error: {}, insert: {}", businessBind, errorLog, insert);
                for (RedisNode redisNode : nodeListA) {
                    updateStatus(redisNode.getHost(), redisNode.getPort(), bindRedisA);
                }
                dingUtil.sendMessage(businessBind + " 已持续检测到异常" + ERROR_TIME.incrementAndGet() + "次, 此时A机房 redis异常: " + JSON.toJSONString(nodeListA));
            }
        } catch (Exception e) {
            roomRedisA = true;
            log.error("business: {}, redis a room connect fail: {}", businessBind, e.getMessage());
            errorLog.setId(IdUtil.fastSimpleUUID());
            errorLog.setPlace(SystemConstant.A);
            errorLog.setClusterName(bindRedisA);
            errorLog.setLog(e.getMessage());
            int insert = privateMonitorErrorLogMapper.insert(errorLog);
            log.error("business: {}, redis a room test error: {}, insert: {}", businessBind, errorLog, insert);
            updateStatus("", null, bindRedisA);
            dingUtil.sendMessage(businessBind + " 已持续检测到异常" + ERROR_TIME.incrementAndGet() + "次, 此时A机房 redis均ping异常");
        }
        try {
            List<RedisNode> nodeListB = RedisClientFactory.getFailNode(LocalCache.getRedisInfoCache(bindRedisB));
            if (CollUtil.isNotEmpty(nodeListB)) {
                roomRedisB = true;
                errorLog.setId(IdUtil.fastSimpleUUID());
                errorLog.setPlace(SystemConstant.B);
                errorLog.setClusterName(bindRedisB);
                errorLog.setLog(JSON.toJSONString(nodeListB));
                int insert = privateMonitorErrorLogMapper.insert(errorLog);
                log.info("business: {}, redis B room test error: {}, insert: {}", businessBind, errorLog, insert);
                for (RedisNode redisNode : nodeListB) {
                    updateStatus(redisNode.getHost(), redisNode.getPort(), bindRedisB);
                }
                dingUtil.sendMessage(businessBind + " 已持续检测到异常" + ERROR_TIME.incrementAndGet() + "次, 此时B机房 redis异常: " + JSON.toJSONString(nodeListB));
            }
        } catch (Exception e) {
            roomRedisB = true;
            log.error("business: {}, redis b room connect fail: {}", businessBind, e.getMessage());
            errorLog.setId(IdUtil.fastSimpleUUID());
            errorLog.setPlace(SystemConstant.B);
            errorLog.setClusterName(bindRedisB);
            errorLog.setLog(e.getMessage());
            int insert = privateMonitorErrorLogMapper.insert(errorLog);
            log.error("business: {}, redis b room test error: {}, insert: {}", businessBind, errorLog, insert);
            updateStatus("", null, bindRedisB);
            dingUtil.sendMessage(businessBind + " 已持续检测到异常" + ERROR_TIME.incrementAndGet() + "次, 此时B机房 redis均ping异常");
        }
        return HealthCheckResultDTO.builder()
                .roomRedisA(roomRedisA)
                .roomRedisB(roomRedisB)
                .build();
    }

    /**
     * 切换 恢复
     *
     * @param place   机房 AB  为空表示恢复, 切换至place
     * @param message 告警信息
     */
    private synchronized void toggle(String place, String message) {
        List<PrivateAreaLocation> locationList = areaLocationMapper.selectList(null);

        TreeMap<String, String> treeMap = new TreeMap<>();
        for (PrivateAreaLocation location : locationList) {
            if (StrUtil.isEmpty(place)) {
                treeMap.put(location.getAreaCode(), location.getInitLocation());
            } else {
                treeMap.put(location.getAreaCode(), place);
            }
        }
        String locationJson = JSON.toJSONString(treeMap, true);
        log.info("business: {}, area location config: {}", businessBind, locationJson);
        LambdaQueryWrapper<PrivateMonitorInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PrivateMonitorInfo::getClusterName, nacosA);
        wrapper.last("limit 1");
        PrivateMonitorInfo monitorInfoA = privateMonitorInfoMapper.selectOne(wrapper);
        wrapper.clear();
        wrapper.eq(PrivateMonitorInfo::getClusterName, nacosB);
        wrapper.last("limit 1");
        PrivateMonitorInfo monitorInfoB = privateMonitorInfoMapper.selectOne(wrapper);

        // 两侧nacos修改配置
        if (monitorInfoA != null) {
            ConfigService configServiceA = LocalCache.getConfigService(nacosA);
            try {
                boolean b = configServiceA.publishConfig(monitorInfoA.getDataId(), monitorInfoA.getGroupId(), locationJson, ConfigType.JSON.getType());
                log.info("business: {}, publish config to room a: {}", businessBind, b);
            } catch (NacosException e) {
                dingUtil.sendMessage(String.format("%s, A机房 nacos发布配置异常: %s", businessBind, e.getMessage()));
                log.error("business: {}, room a nacos, publish config error: ", businessBind, e);
            }
        }

        if (monitorInfoB != null) {
            ConfigService configServiceB = LocalCache.getConfigService(nacosB);
            try {
                boolean b = configServiceB.publishConfig(monitorInfoB.getDataId(), monitorInfoB.getGroupId(), locationJson, ConfigType.JSON.getType());
                log.info("business: {}, publish config to room b: {}", businessBind, b);
            } catch (NacosException e) {
                dingUtil.sendMessage(String.format("%s, B机房 nacos发布配置异常: %s", businessBind, e.getMessage()));
                log.error("business: {}, room b nacos, publish config error: ", businessBind, e);
            }
        }
        // 修改private_area_location
        List<PrivateAreaLocation> areaLocationList = new ArrayList<>();
        for (Map.Entry<String, String> entry : treeMap.entrySet()) {
            areaLocationList.add(new PrivateAreaLocation(entry.getKey(), entry.getValue(), DateUtil.date()));
        }
        // 批量更新
        int updateBatch = areaLocationMapper.updateBatch(areaLocationList);

        dingUtil.sendMessage(message);
        log.info("business: {}, 开启nacos配置切换: {}, 全部切换到 {} 机房, 更新完成: {}", businessBind, DateUtil.now(), place, updateBatch);
    }

    /**
     * 是否已灾备切换
     */
    private Boolean isToggle() {
        LambdaQueryWrapper<PrivateMonitorInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PrivateMonitorInfo::getType, ServerEnum.nacos.name());
        queryWrapper.eq(PrivateMonitorInfo::getBusiness, business);
        queryWrapper.eq(PrivateMonitorInfo::getToggle, 1);
        Integer count = privateMonitorInfoMapper.selectCount(queryWrapper);
        return count > 0;
    }

    /**
     * 更新灾备切换状态 nacos
     */
    private synchronized void updateToggle(Integer toggle) {
        PrivateMonitorInfo privateMonitorInfo = new PrivateMonitorInfo();
        privateMonitorInfo.setBusiness(business);
        privateMonitorInfo.setToggle(toggle);
        privateMonitorInfo.setType(ServerEnum.nacos.name());
        LambdaQueryWrapper<PrivateMonitorInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PrivateMonitorInfo::getType, privateMonitorInfo.getType());
        queryWrapper.eq(PrivateMonitorInfo::getBusiness, privateMonitorInfo.getBusiness());
        int update = privateMonitorInfoMapper.update(privateMonitorInfo, queryWrapper);
        log.info("business: {}, update nacos toggle status finish: {}", businessBind, update);
    }

    /**
     * 更新 mq redis 状态
     *
     * @param ip          ip
     * @param port        端口
     * @param clusterName 集群名称
     */
    private synchronized void updateStatus(String ip, Integer port, String clusterName) {
        PrivateMonitorInfo privateMonitorInfo = new PrivateMonitorInfo();
        privateMonitorInfo.setStatus(0);
        if (StrUtil.isNotEmpty(ip)) {
            privateMonitorInfo.setIp(ip);
        }
        if (ObjectUtil.isNotEmpty(port)) {
            privateMonitorInfo.setPort(port);
        }
        privateMonitorInfo.setClusterName(clusterName);


        LambdaQueryWrapper<PrivateMonitorInfo> queryWrapper = new LambdaQueryWrapper<>();
        if (StrUtil.isNotEmpty(ip)) {
            queryWrapper.eq(PrivateMonitorInfo::getIp, privateMonitorInfo.getIp());
        }
        if (ObjectUtil.isNotEmpty(port)) {
            queryWrapper.eq(PrivateMonitorInfo::getPort, privateMonitorInfo.getPort());
        }
        queryWrapper.eq(PrivateMonitorInfo::getClusterName, privateMonitorInfo.getClusterName());

         privateMonitorInfoMapper.update(privateMonitorInfo, queryWrapper);
    }
}
