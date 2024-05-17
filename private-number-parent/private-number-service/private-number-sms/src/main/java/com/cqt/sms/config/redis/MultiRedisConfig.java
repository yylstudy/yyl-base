package com.cqt.sms.config.redis;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.StrUtil;
import com.cqt.model.common.properties.MultiRedisProperties;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisNode;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisPoolConfig;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author linshiqiang
 * @date 2021/8/26 10:20
 * redisson 多机房配置
 */
@Configuration
public class MultiRedisConfig {

    private final MultiRedisProperties multiRedisProperties;

    public MultiRedisConfig(MultiRedisProperties multiRedisProperties) {
        this.multiRedisProperties = multiRedisProperties;
    }

    @Bean
    @RefreshScope
    public JedisCluster getJedisCluster() {
        List<String> clusterNodes = multiRedisProperties.getCluster().getNodes();
        Set<HostAndPort> nodes = new HashSet<>();
        for (String node : clusterNodes) {
            String[] hp = node.split(StrUtil.COLON);
            nodes.add(new HostAndPort(hp[0], Integer.parseInt(hp[1])));
        }

        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        jedisPoolConfig.setMaxIdle(multiRedisProperties.getJedis().getPool().getMaxIdle());
        jedisPoolConfig.setMaxWaitMillis(multiRedisProperties.getJedis().getPool().getMaxWait());
        //创建集群对象
        return new JedisCluster(nodes, multiRedisProperties.getTimeout(), multiRedisProperties.getTimeout(), 5, multiRedisProperties.getPassword(), jedisPoolConfig);
    }

    /**
     * 本地集群
     */
    @RefreshScope
    @Bean
    @SuppressWarnings("all")
    public RedisTemplate<String, Object> redisTemplate(MultiRedisProperties multiRedisProperties) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        // 设置redis连接工厂
        List<String> nodes = multiRedisProperties.getCluster().getNodes();
        template.setConnectionFactory(redisConnectionFactory(nodes));
        Jackson2JsonRedisSerializer jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer(Object.class);
        ObjectMapper om = new ObjectMapper();
        om.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        jackson2JsonRedisSerializer.setObjectMapper(om);
        StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();
        // key采用String的序列化方式
        template.setKeySerializer(stringRedisSerializer);
        // hash的key也采用String的序列化方式
        template.setHashKeySerializer(stringRedisSerializer);
        // value序列化方式采用jackson
        template.setValueSerializer(jackson2JsonRedisSerializer);
        // hash的value序列化方式采用jackson
        template.setHashValueSerializer(jackson2JsonRedisSerializer);
        template.afterPropertiesSet();
        return template;
    }

    /**
     * 异地集群
     */
    @RefreshScope
    @Bean("offsiteRedisTemplate")
    @SuppressWarnings("all")
    @ConditionalOnProperty(prefix = "spring.redis.cluster2", name = "active", havingValue = "true")
    public RedisTemplate<String, Object> offsiteRedisTemplate(MultiRedisProperties multiRedisProperties) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        // 设置redis连接工厂
        List<String> nodes = multiRedisProperties.getCluster2().getNodes();
        template.setConnectionFactory(redisConnectionFactory(nodes));
        Jackson2JsonRedisSerializer jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer(Object.class);
        ObjectMapper om = new ObjectMapper();
        om.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        jackson2JsonRedisSerializer.setObjectMapper(om);
        StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();
        // key采用String的序列化方式
        template.setKeySerializer(stringRedisSerializer);
        // hash的key也采用String的序列化方式
        template.setHashKeySerializer(stringRedisSerializer);
        // value序列化方式采用jackson
        template.setValueSerializer(jackson2JsonRedisSerializer);
        // hash的value序列化方式采用jackson
        template.setHashValueSerializer(jackson2JsonRedisSerializer);
        template.afterPropertiesSet();
        return template;
    }

    public RedisConnectionFactory redisConnectionFactory(List<String> clusterNodes) {
        List<RedisNode> redisNodeList = new ArrayList<>();
        for (String nodes : clusterNodes) {
            String[] node = nodes.split(":");
            RedisNode redisNode = new RedisNode(node[0], Convert.toInt(node[1]));
            redisNodeList.add(redisNode);
        }
        RedisClusterConfiguration clusterConfiguration = new RedisClusterConfiguration();
        clusterConfiguration.setClusterNodes(redisNodeList);
        clusterConfiguration.setPassword(RedisPassword.of(multiRedisProperties.getPassword()));


        JedisConnectionFactory jedisConnectionFactory = new JedisConnectionFactory(clusterConfiguration, poolConfig(multiRedisProperties));
        jedisConnectionFactory.afterPropertiesSet();
        return jedisConnectionFactory;
    }

    /**
     * jedis连接池
     */
    public JedisPoolConfig poolConfig(MultiRedisProperties multiRedisProperties) {
        MultiRedisProperties.Pool pool = multiRedisProperties.getJedis().getPool();
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxIdle(pool.getMaxIdle());
        poolConfig.setMaxTotal(pool.getMaxActive());
        poolConfig.setMaxWaitMillis(pool.getMaxWait());
        poolConfig.setMinIdle(pool.getMinIdle());
        poolConfig.setTimeBetweenEvictionRunsMillis(pool.getTimeBetweenEvictionRuns());
        return poolConfig;
    }

    protected final RedisClusterConfiguration getClusterConfiguration() {
        MultiRedisProperties.Cluster cluster = multiRedisProperties.getCluster();
        RedisClusterConfiguration config = new RedisClusterConfiguration(cluster.getNodes());
        if (cluster.getMaxRedirects() != null) {
            config.setMaxRedirects(cluster.getMaxRedirects());
        }
        if (this.multiRedisProperties.getPassword() != null) {
            config.setPassword(RedisPassword.of(this.multiRedisProperties.getPassword()));
        }
        return config;
    }

}
