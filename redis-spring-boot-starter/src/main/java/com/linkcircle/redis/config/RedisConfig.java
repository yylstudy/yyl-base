package com.linkcircle.redis.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.PropertyMapper;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.jedis.JedisClientConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import redis.clients.jedis.JedisPoolConfig;

/**
 * @author yang.yonglian
 * @version 1.0.0
 * @Description TODO
 * @createTime 2024/3/25 13:46
 */
@Slf4j
@EnableCaching
@Configuration
@DependsOn("applicationContextHolder")
public class RedisConfig {
	@Autowired
	private MultiRedisProperties redisProperties;

	private static volatile RedisConnectionFactory redisConnectionFactory2;
	/**
	 * 本地机房redisTemplate
	 * @param redisConnectionFactory
	 * @return
	 */
	@Bean
	@Primary
	public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
		RedisTemplate<String, Object> template = new RedisTemplate<>();
		template.setConnectionFactory(redisConnectionFactory);
		RedisSerializer redisSerializer = jacksonSerializer();
		StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();
		template.setKeySerializer(stringRedisSerializer);
		template.setValueSerializer(redisSerializer);

		/*hash字符串序列化方法*/
		template.setHashKeySerializer(stringRedisSerializer);
		template.setHashValueSerializer(stringRedisSerializer);

		return template;
	}

	/**
	 * 异地机房RedisTemplate
	 * @return
	 */
	@Bean
	@ConditionalOnProperty(prefix = "spring.redis.cluster2", name = "active", havingValue = "true")
	public RedisTemplate<String, Object> redisTemplate2() {
		RedisTemplate<String, Object> template = new RedisTemplate<>();
		RedisSerializer redisSerializer = jacksonSerializer();
		template.setValueSerializer(redisSerializer);
		StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();
		template.setKeySerializer(stringRedisSerializer);
		RedisConnectionFactory redisConnectionFactory = redisConnectionFactory2();
		template.setConnectionFactory(redisConnectionFactory);
		return template;
	}

	@Bean
	public JedisConnectionFactory redissonConnectionFactory() {
		return createJedisConnectionFactory(redisProperties.getCluster());
	}
	public RedisConnectionFactory redisConnectionFactory2() {
		try{
			if(redisConnectionFactory2==null){
				synchronized (RedisConfig.class){
					if(redisConnectionFactory2==null){
						JedisConnectionFactory jedisConnectionFactory =
								createJedisConnectionFactory(redisProperties.getCluster2());
						jedisConnectionFactory.afterPropertiesSet();
						redisConnectionFactory2 = jedisConnectionFactory;
					}
				}
			}
		}catch (Exception e){
			throw new RuntimeException(e);
		}
		return redisConnectionFactory2;
	}

	private JedisConnectionFactory createJedisConnectionFactory(MultiRedisProperties.Cluster cluster) {
		JedisClientConfiguration clientConfiguration = getJedisClientConfiguration();
		return new JedisConnectionFactory(getClusterConfiguration(cluster), clientConfiguration);
	}

	protected final RedisClusterConfiguration getClusterConfiguration(MultiRedisProperties.Cluster clusterProperties) {
		RedisClusterConfiguration config = new RedisClusterConfiguration(clusterProperties.getNodes());
		if (clusterProperties.getMaxRedirects() != null) {
			config.setMaxRedirects(clusterProperties.getMaxRedirects());
		}
		if (this.redisProperties.getPassword() != null) {
			config.setPassword(RedisPassword.of(this.redisProperties.getPassword()));
		}
		return config;
	}

	private JedisClientConfiguration getJedisClientConfiguration(
			) {
		JedisClientConfiguration.JedisClientConfigurationBuilder builder = applyProperties(JedisClientConfiguration.builder());
		MultiRedisProperties.Pool pool = redisProperties.getJedis().getPool();
		applyPooling(pool, builder);
		return builder.build();
	}

	private JedisClientConfiguration.JedisClientConfigurationBuilder applyProperties(JedisClientConfiguration.JedisClientConfigurationBuilder builder) {
		PropertyMapper map = PropertyMapper.get().alwaysApplyingWhenNonNull();
		map.from(redisProperties.getTimeout()).to(builder::readTimeout);
		map.from(redisProperties.getConnectTimeout()).to(builder::connectTimeout);
		return builder;
	}

	private void applyPooling(MultiRedisProperties.Pool pool,
							  JedisClientConfiguration.JedisClientConfigurationBuilder builder) {
		builder.usePooling().poolConfig(jedisPoolConfig(pool));
	}

	private JedisPoolConfig jedisPoolConfig(MultiRedisProperties.Pool pool) {
		JedisPoolConfig config = new JedisPoolConfig();
		config.setMaxTotal(pool.getMaxActive());
		config.setMaxIdle(pool.getMaxIdle());
		config.setMinIdle(pool.getMinIdle());
		if (pool.getTimeBetweenEvictionRuns() != null) {
			config.setTimeBetweenEvictionRuns(pool.getTimeBetweenEvictionRuns());
		}
		if (pool.getMaxWait() != null) {
			config.setMaxWait(pool.getMaxWait());
		}
		return config;
	}

	private Jackson2JsonRedisSerializer jacksonSerializer() {
		Jackson2JsonRedisSerializer jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer(Object.class);
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
		objectMapper.activateDefaultTyping(
				LaissezFaireSubTypeValidator.instance ,
				ObjectMapper.DefaultTyping.NON_FINAL,
				JsonTypeInfo.As.WRAPPER_ARRAY);
		jackson2JsonRedisSerializer.setObjectMapper(objectMapper);
		return jackson2JsonRedisSerializer;
	}


}
