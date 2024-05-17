package com.cqt.monitor.web.switchroom.job;

import com.cqt.model.monitor.entity.MonitorConfigInfo;
import com.cqt.monitor.cache.MonitorConfigCache;
import com.cqt.monitor.web.switchroom.mapper.MonitorConfigInfoMapper;
import com.cqt.monitor.web.switchroom.service.MonitorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @author hlx
 * @date 2021-11-28
 */
@Component
@Order(value = 1)
@Slf4j
public class StartRunner implements ApplicationRunner {

    @Autowired
    private MonitorService monitorService;

    @Autowired
    private MonitorConfigInfoMapper configInfoMapper;


    /**
     * 赋予初始值  数据库读取redis和mq的监控信息
     */
    @Override
    public void run(ApplicationArguments applicationArguments) {
        // 读取redis信息
        List<MonitorConfigInfo> configList = configInfoMapper.selectList(null);
        log.info("读取平台配置信息成功！");
        Map<String, List<MonitorConfigInfo>> redisConfigMap = configList.stream()
                .filter(config -> "redis".equals(config.getType()))
                .collect(Collectors.groupingBy(MonitorConfigInfo::getPlatform));
        Map<String, List<MonitorConfigInfo>> concurrentRedisConfigMap = new ConcurrentHashMap<>(16);
        concurrentRedisConfigMap.putAll(redisConfigMap);
        MonitorConfigCache.REDIS_CONFIG = concurrentRedisConfigMap;
        // 读取mq信息
        Map<String, List<MonitorConfigInfo>> rabbitmqConfigMap = configList.stream()
                .filter(config -> "rabbitmq".equals(config.getType()))
                .collect(Collectors.groupingBy(MonitorConfigInfo::getPlatform));
        Map<String, MonitorConfigInfo> concurrentRabbitmqConfigMap = new ConcurrentHashMap<>(16);
        rabbitmqConfigMap.forEach((platform, rabbitmqConfigs) -> {
            for (MonitorConfigInfo rabbitmqConfig:rabbitmqConfigs) {
                concurrentRabbitmqConfigMap.put(platform, rabbitmqConfig);
            }
        });
        MonitorConfigCache.RABBITMQ_CONFIG = concurrentRabbitmqConfigMap;

        MonitorConfigCache.NG_CONFIG = configList.stream()
                .filter(config1 -> "nginx".equals(config1.getType()))
                .collect(Collectors.groupingBy(MonitorConfigInfo::getPlatform));
        // 读取漫游号信息
        MonitorConfigCache.MSRN_CONFIG = configList.stream()
                .filter(config -> "msrn".equals(config.getType()))
                .collect(Collectors.groupingBy(MonitorConfigInfo::getPlatform));
//        Map<String, MonitorConfigInfo> concurrentMsrnConfigMap = new ConcurrentHashMap<>(16);
//        msrnConfigMap.forEach((platform, msrnConfigs) -> {
//            for (MonitorConfigInfo msrnConfig:msrnConfigs) {
//                concurrentMsrnConfigMap.put(platform, msrnConfig);
//            }
//        });


        monitorService.clearRabbitCacheMap();
        log.info("平台配置写入缓存成功！");
    }
}
