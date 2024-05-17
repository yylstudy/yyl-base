package com.cqt.wechat.config;

import cn.hutool.core.convert.Convert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisNode;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import redis.clients.jedis.JedisPoolConfig;

import java.util.ArrayList;
import java.util.List;

/**
 * @author hlx
 * @date 2021-07-08
 */
@Configuration
public class RedisConfig {

    @Autowired
    private RedisProperties redisProperties;

    /**
     * 简单的string类型序列化规则
     */
    @Bean
    public RedisTemplate<Object, Object> redisTemplate() {
        RedisTemplate<Object, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory(redisProperties.getCluster().getNodes()));
        RedisSerializer<String> stringRedisSerializer = redisTemplate.getStringSerializer();
        redisTemplate.setValueSerializer(stringRedisSerializer);
        redisTemplate.setKeySerializer(stringRedisSerializer);
        redisTemplate.setHashKeySerializer(stringRedisSerializer);
        redisTemplate.setHashValueSerializer(stringRedisSerializer);
        return redisTemplate;
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
        clusterConfiguration.setPassword(RedisPassword.of(redisProperties.getPassword()));

        JedisConnectionFactory jedisConnectionFactory = new JedisConnectionFactory(clusterConfiguration, poolConfig(redisProperties));
        jedisConnectionFactory.afterPropertiesSet();

        return jedisConnectionFactory;
    }

    /**
     * jedis连接池
     */
    public JedisPoolConfig poolConfig(RedisProperties redisProperties) {
        RedisProperties.Pool pool = redisProperties.getJedis().getPool();
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxIdle(pool.getMaxIdle());
        poolConfig.setMaxTotal(pool.getMaxActive());
        poolConfig.setMinIdle(pool.getMinIdle());
        return poolConfig;
    }
}
