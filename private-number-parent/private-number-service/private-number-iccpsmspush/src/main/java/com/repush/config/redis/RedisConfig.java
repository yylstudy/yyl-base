package com.repush.config.redis;

import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisPoolConfig;

import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Set;

/**
 *
 **/
@Configuration
@ConditionalOnClass({JedisCluster.class})
public class RedisConfig {
    @Value("${spring.redis.cluster.nodes}")
    private String clusterNodes;
    @Value("${spring.redis.jedis.timeout}")
    private int timeout;
    @Value("${spring.redis.jedis.pool.max-idle}")
    private int maxIdle;
    @Value("${spring.redis.jedis.pool.max-wait}")
    private long maxWaitMillis;
    @Value("${spring.redis.jedis.timeout}")
    private int commandTimeout;
    @Value("${spring.redis.jedis.password}")
    private String password;

    @Bean
    @RefreshScope
    public JedisCluster getJedisCluster() {
        String[] cNodes = clusterNodes.split(",");
        Set<HostAndPort> nodes = new HashSet<>();
        //分割出集群节点
        for (String node : cNodes) {
            String[] hp = node.split(":");
            nodes.add(new HostAndPort(hp[0], Integer.parseInt(hp[1])));
        }
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        jedisPoolConfig.setMaxIdle(maxIdle);
        jedisPoolConfig.setMaxWaitMillis(maxWaitMillis);
        //创建集群对象（没有设置集群密码）
        //return new JedisCluster(nodes, commandTimeout, jedisPoolConfig);
        if (StrUtil.isEmpty(password)) {
            return new JedisCluster(nodes, commandTimeout, jedisPoolConfig);
        }
        //有设置集群密码
        return new JedisCluster(nodes, commandTimeout, commandTimeout, 5, password, jedisPoolConfig);
    }

    /**
     * 设置数据存入redis 的序列化方式
     * </br>redisTemplate序列化默认使用的jdkSerializeable,存储二进制字节码,导致key会出现乱码，所以自定义
     * 序列化类
     */
    @Bean
    public RedisTemplate<Object, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) throws UnknownHostException {
        RedisTemplate<Object, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        Jackson2JsonRedisSerializer jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer(Object.class);
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        objectMapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
        jackson2JsonRedisSerializer.setObjectMapper(objectMapper);

        redisTemplate.setValueSerializer(jackson2JsonRedisSerializer);
        redisTemplate.setKeySerializer(new StringRedisSerializer());

        redisTemplate.afterPropertiesSet();

        return redisTemplate;
    }

}
