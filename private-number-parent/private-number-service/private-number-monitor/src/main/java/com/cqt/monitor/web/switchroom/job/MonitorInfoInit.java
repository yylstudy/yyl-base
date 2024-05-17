package com.cqt.monitor.web.switchroom.job;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.cloud.nacos.NacosConfigProperties;
import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.exception.NacosException;
import com.cqt.common.enums.ServerEnum;
import com.cqt.model.monitor.entity.PrivateMonitorInfo;
import com.cqt.model.monitor.properties.MonitorProperties;
import com.cqt.monitor.cache.LocalCache;
import com.cqt.monitor.web.switchroom.mapper.PrivateMonitorInfoMapper;
import com.cqt.monitor.web.switchroom.mysql.MysqlFactory;
import com.cqt.monitor.web.switchroom.rabbitmq.RabbitmqNode;
import com.cqt.monitor.web.switchroom.rabbitmq.RabbitmqUri;
import com.cqt.monitor.web.switchroom.redis.RedisUri;
import com.linkcircle.ss.LHikariDataSource;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import redis.clients.jedis.HostAndPort;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author linshiqiang
 * @date 2022/1/21 16:22
 * 小号平台 监控信息初始化
 */
@Component
@Slf4j
public class MonitorInfoInit {

    public static final String NAMESPACE = "namespace";

    public static final String USERNAME = "username";

    public static final String PASSWORD = "password";

    public static final String SERVER_ADDR = "serverAddr";

    private final PrivateMonitorInfoMapper monitorInfoMapper;

    private final NacosConfigProperties nacosConfigProperties;

    private final MonitorProperties monitorProperties;

    public MonitorInfoInit(PrivateMonitorInfoMapper monitorInfoMapper, NacosConfigProperties nacosConfigProperties, MonitorProperties monitorProperties) {
        this.monitorInfoMapper = monitorInfoMapper;
        this.nacosConfigProperties = nacosConfigProperties;
        this.monitorProperties = monitorProperties;
    }

    @PostConstruct
    public void init() {

        LocalCache.RABBIT_MQ_INFO_CACHE.clear();
        LocalCache.REDIS_INFO_CACHE.clear();
        LocalCache.MYSQL_DATA_SOURCE_MAP.clear();
        List<PrivateMonitorInfo> monitorInfoList = monitorInfoMapper.selectList(null);

        Map<String, List<PrivateMonitorInfo>> listMap = monitorInfoList.stream().collect(Collectors.groupingBy(PrivateMonitorInfo::getType));

        for (Map.Entry<String, List<PrivateMonitorInfo>> entry : listMap.entrySet()) {
            // 类型 redis mq
            String type = entry.getKey();
            List<PrivateMonitorInfo> infoList = entry.getValue();
            Map<String, List<PrivateMonitorInfo>> collect = infoList.stream().collect(Collectors.groupingBy(PrivateMonitorInfo::getClusterName));
            for (Map.Entry<String, List<PrivateMonitorInfo>> listEntry : collect.entrySet()) {
                // 集群名称
                String clusterName = listEntry.getKey();
                List<PrivateMonitorInfo> list = listEntry.getValue();
                if (CollUtil.isEmpty(list)) {
                    continue;
                }
                Set<RabbitmqNode> rabbitmqNodes = new HashSet<>();
                Set<HostAndPort> hostAndPortSet = new HashSet<>();
                Set<PrivateMonitorInfo> nacosConfigUriSet = new HashSet<>();
                for (PrivateMonitorInfo monitorInfo : list) {
                    if (ServerEnum.rabbitmq.name().equals(type)) {
                        RabbitmqNode rabbitmqNode = RabbitmqNode.builder()
                                .uri(monitorInfo.getUrl())
                                .host(monitorInfo.getIp())
                                .port(monitorInfo.getPort())
                                .username(monitorInfo.getUsername())
                                .password(monitorInfo.getPassword())
                                .timeout(Convert.toInt(monitorProperties.getTimeout(), 5000))
                                .mqCheckType(monitorProperties.getMqCheckType())
                                .build();
                        if (ObjectUtil.isNull(LocalCache.getRabbitmqConnectionCache(monitorInfo.getIp()))) {
                            ConnectionFactory connectionFactory = new ConnectionFactory();
                            connectionFactory.setHost(monitorInfo.getIp());
                            connectionFactory.setPort(monitorInfo.getPort());
                            connectionFactory.setVirtualHost("/");
                            connectionFactory.setUsername(monitorInfo.getUsername());
                            connectionFactory.setPassword(monitorInfo.getPassword());
                            connectionFactory.setRequestedHeartbeat(10);
                            try {
                                Connection connection = connectionFactory.newConnection();
                                LocalCache.putRabbitmqConnectionCache(monitorInfo.getIp(), connection);
                            } catch (Exception e) {
                                log.error("{}, rabbitmq connect error: ", monitorInfo, e);

                            }
                        }
                        rabbitmqNodes.add(rabbitmqNode);
                    }
                    if (ServerEnum.redis.name().equals(type)) {
                        HostAndPort hostAndPort = new HostAndPort(monitorInfo.getIp(), monitorInfo.getPort());
                        hostAndPortSet.add(hostAndPort);
                    }

                    if (ServerEnum.nacos.name().equals(type)) {
                        nacosConfigUriSet.add(monitorInfo);
                    }

                    if (ServerEnum.mysql.name().equals(type)) {
                        LHikariDataSource hikariDataSource = new LHikariDataSource();
                        hikariDataSource.setJdbcUrl(MysqlFactory.buildUrl(monitorInfo.getIp(), monitorInfo.getPort()));
                        hikariDataSource.setUsername(monitorInfo.getUsername());
                        hikariDataSource.setPassword(monitorInfo.getPassword());
                        LocalCache.MYSQL_DATA_SOURCE_MAP.put(monitorInfo.getIp(), hikariDataSource);
                    }
                }
                if (ServerEnum.rabbitmq.name().equals(type)) {
                    RabbitmqUri rabbitmqUri = RabbitmqUri.builder().hostAndPortSet(rabbitmqNodes).build();
                    LocalCache.putRabbitmqInfoCache(clusterName, rabbitmqUri);
                }
                if (ServerEnum.redis.name().equals(type)) {
                    RedisUri redisUri = RedisUri.builder()
                            .requirePass(list.get(0).getPassword())
                            .clusterName(clusterName)
                            .hostAndPortSet(hostAndPortSet)
                            .database(0)
                            .build();

                    LocalCache.putRedisInfoCache(clusterName, redisUri);
                }
                if (ServerEnum.nacos.name().equals(type)) {
                    ConfigService service = LocalCache.getConfigService(clusterName);
                    if (service == null) {
                        Optional<ConfigService> configServiceOptional = buildConfigService(nacosConfigUriSet);
                        configServiceOptional.ifPresent(configService -> LocalCache.putConfigServiceCache(clusterName, configService));
                    }
                }

            }
        }
        log.info("redis init finish: {}", LocalCache.REDIS_INFO_CACHE.size());
        log.info("nacos init finish: {}", LocalCache.NACOS_CONFIG_SERVICE_CACHE.size());
        log.info("mysql init finish: {}", LocalCache.MYSQL_DATA_SOURCE_MAP.size());
        log.info("rabbitmq init finish: {}", LocalCache.RABBIT_MQ_INFO_CACHE.size());

    }

    /**
     * nacos 配置连接
     */
    private Optional<ConfigService> buildConfigService(Set<PrivateMonitorInfo> nacosConfigUriSet) {

        Optional<PrivateMonitorInfo> privateMonitorInfo = nacosConfigUriSet.stream().findFirst();
        if (!privateMonitorInfo.isPresent()) {
            return Optional.empty();
        }

        String serverAddr = nacosConfigUriSet.stream()
                .map(item -> item.getIp() + ":" + item.getPort())
                .collect(Collectors.joining(","));

        PrivateMonitorInfo monitorInfo = privateMonitorInfo.get();

        Properties properties = nacosConfigProperties.assembleConfigServiceProperties();
        properties.put(SERVER_ADDR, serverAddr);
        properties.put(NAMESPACE, monitorInfo.getNamespace());
        properties.put(USERNAME, monitorInfo.getUsername());
        properties.put(PASSWORD, monitorInfo.getPassword());
        ConfigService configService;
        try {
            configService = NacosFactory.createConfigService(properties);
            return Optional.of(configService);
        } catch (NacosException e) {
            log.error("NacosException: ", e);
        }
        return Optional.empty();
    }
}
