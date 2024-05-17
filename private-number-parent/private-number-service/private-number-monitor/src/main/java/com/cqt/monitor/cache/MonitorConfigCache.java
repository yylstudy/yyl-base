package com.cqt.monitor.cache;

import com.cqt.model.monitor.entity.MonitorConfigInfo;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * 业务侧监控配置
 *
 * @author hlx
 * @date 2022-02-01
 */
public class MonitorConfigCache {

    /**
     * redis配置 key platform  value configs
     */
    public static Map<String, List<MonitorConfigInfo>> REDIS_CONFIG = new ConcurrentHashMap<>();

    /**
     * mq配置 key platform value  config
     */
    public static Map<String, MonitorConfigInfo> RABBITMQ_CONFIG = new ConcurrentHashMap<>();

    /**
     * 漫游号配置 key platform value  config
     */
    public static Map<String, List<MonitorConfigInfo>> MSRN_CONFIG = new ConcurrentHashMap<>();

    /**
     * ng配置 key platform value  config
     */
    public static Map<String, List<MonitorConfigInfo>> NG_CONFIG = new ConcurrentHashMap<>();
}
