package com.cqt.wechat.config;

import cn.hutool.system.OsInfo;
import cn.hutool.system.SystemUtil;
import com.cqt.model.common.properties.MultiRedisProperties;
import lombok.extern.slf4j.Slf4j;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;
import org.redisson.config.Config;
import org.redisson.config.ReadMode;
import org.redisson.config.TransportMode;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.ArrayList;
import java.util.List;

/**
 * @author linshiqiang
 * @date 2021/8/26 10:20
 * redisson 多机房配置
 */
@Slf4j
@Configuration
public class MultiRedissonConfig {

    private static final String REDIS_PROTOCOL_PREFIX = "redis://";
    private static final String REDISS_PROTOCOL_PREFIX = "rediss://";

    private final MultiRedisProperties redisProperties;

    public MultiRedissonConfig(MultiRedisProperties redisProperties) {
        this.redisProperties = redisProperties;
    }

    @Bean(name = "redissonClient")
    @Primary
    public RedissonClient redissonClient(MultiRedisProperties redisProperties) {
        Config config = getConfig(redisProperties.getCluster().getNodes());
        config.setCodec(StringCodec.INSTANCE);
//        config.setCodec(new LZ4Codec());
        OsInfo osInfo = SystemUtil.getOsInfo();
        if (osInfo.getName().contains("Linux")) {
            log.info("redisson use epoll...");
            config.setTransportMode(TransportMode.EPOLL);
        }
        return Redisson.create(config);
    }

//    @Bean(name = "redissonClient2")
//    @ConditionalOnProperty(prefix = "spring.redis.cluster2", name = "active", havingValue = "true")
//    public RedissonClient redissonClient2(MultiRedisProperties redisProperties) {
//        Config config = getConfig(redisProperties.getCluster2().getNodes());
//        config.setCodec(StringCodec.INSTANCE);
////        config.setCodec(new LZ4Codec());
//        OsInfo osInfo = SystemUtil.getOsInfo();
//        if (osInfo.getName().contains("Linux")) {
//            config.setTransportMode(TransportMode.EPOLL);
//        }
//        return Redisson.create(config);
//    }

    @SuppressWarnings("all")
    private Config getConfig(List<String> nodesObject) {
        int timeout = redisProperties.getTimeout();

        String[] nodes = convert(nodesObject);

        Config config = new Config();
        config.useClusterServers()
                .addNodeAddress(nodes)
                .setConnectTimeout(timeout)
                .setTimeout(500)
                .setIdleConnectionTimeout(500)
                .setRetryAttempts(3)
                .setMasterConnectionPoolSize(redisProperties.getMasterConnectionPoolSize())
                .setRetryInterval(500)
                .setCheckSlotsCoverage(false)
                .setScanInterval(10000)
                .setReadMode(ReadMode.MASTER)
                .setPassword(redisProperties.getPassword());
        return config;
    }

    @SuppressWarnings("all")
    private String[] convert(List<String> nodesObject) {
        List<String> nodes = new ArrayList<>(nodesObject.size());
        for (String node : nodesObject) {
            if (!node.startsWith(REDIS_PROTOCOL_PREFIX) && !node.startsWith(REDISS_PROTOCOL_PREFIX)) {
                nodes.add(REDIS_PROTOCOL_PREFIX + node);
            } else {
                nodes.add(node);
            }
        }
        return nodes.toArray(new String[nodes.size()]);
    }

}
