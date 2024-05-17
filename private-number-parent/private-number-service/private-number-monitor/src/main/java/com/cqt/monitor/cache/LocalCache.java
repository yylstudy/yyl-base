package com.cqt.monitor.cache;

import com.alibaba.nacos.api.config.ConfigService;
import com.cqt.monitor.web.switchroom.rabbitmq.RabbitmqUri;
import com.cqt.monitor.web.switchroom.redis.RedisUri;
import com.linkcircle.ss.LHikariDataSource;
import com.rabbitmq.client.Connection;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author linshiqiang
 * @date 2022/1/21 16:23
 * 本地缓存
 */
public class LocalCache {

    /**
     * redis集群 连接信息
     */
    public final static Map<String, RedisUri> REDIS_INFO_CACHE = new ConcurrentHashMap<>(512);

    /**
     * rabbitmq 连接信息
     */
    public final static Map<String, RabbitmqUri> RABBIT_MQ_INFO_CACHE = new ConcurrentHashMap<>(128);

    /**
     * rabbitmq 连接
     */
    public final static Map<String, Connection> RABBIT_MQ_CONNECT_CACHE = new ConcurrentHashMap<>(128);

    /**
     * nacos 配置连接
     */
    public final static Map<String, ConfigService> NACOS_CONFIG_SERVICE_CACHE = new ConcurrentHashMap<>(128);

    /**
     * mysql 数据源
     */
    public final static Map<String, LHikariDataSource> MYSQL_DATA_SOURCE_MAP = new ConcurrentHashMap<>();

    public static RedisUri getRedisInfoCache(String clusterName) {

        return REDIS_INFO_CACHE.get(clusterName);
    }

    public static synchronized void putRedisInfoCache(String clusterName, RedisUri redisUri) {

        REDIS_INFO_CACHE.put(clusterName, redisUri);
    }

    public static RabbitmqUri getRabbitmqInfoCache(String clusterName) {

        return RABBIT_MQ_INFO_CACHE.get(clusterName);
    }

    public static void putRabbitmqInfoCache(String clusterName, RabbitmqUri rabbitmqUri) {

        RABBIT_MQ_INFO_CACHE.put(clusterName, rabbitmqUri);
    }

    public static Connection getRabbitmqConnectionCache(String host) {

        return RABBIT_MQ_CONNECT_CACHE.get(host);
    }

    public static void putRabbitmqConnectionCache(String host, Connection connection) {

        RABBIT_MQ_CONNECT_CACHE.put(host, connection);
    }

    public static ConfigService getConfigService(String clusterName) {

        return NACOS_CONFIG_SERVICE_CACHE.get(clusterName);
    }

    public static void putConfigServiceCache(String clusterName, ConfigService configService) {

        NACOS_CONFIG_SERVICE_CACHE.put(clusterName, configService);
    }

}
