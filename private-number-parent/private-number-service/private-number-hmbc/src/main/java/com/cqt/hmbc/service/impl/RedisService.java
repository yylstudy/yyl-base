package com.cqt.hmbc.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.StrUtil;
import com.cqt.hmbc.config.ThreadPoolConfig;
import com.cqt.model.common.properties.MultiRedisProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.Executor;

/**
 * Redis 双写工具类
 *
 * @author Xienx
 * @date 2023年02月07日 17:10
 */
@Slf4j
@Service
@SuppressWarnings("unused")
public class RedisService {

    /**
     * 批量执行的条数
     */
    private final Integer PER_BATCH_COUNT = 100;
    private final Executor executor;
    private final ThreadLocal<String> threadLocal;
    private final MultiRedisProperties redisProperties;
    private StringRedisTemplate otherStringRedisTemplate;
    private final StringRedisTemplate localStringRedisTemplate;
    private RedisTemplate<String, Object> otherRedisTemplate;
    private final RedisTemplate<String, Object> localRedisTemplate;

    public RedisService(MultiRedisProperties redisProperties,
                        @Qualifier(ThreadPoolConfig.REDIS_EXECUTOR) Executor executor,
                        @Qualifier("localRedisTemplate") RedisTemplate<String, Object> localRedisTemplate,
                        @Qualifier("otherRedisTemplate") @Autowired(required = false) RedisTemplate<String, Object> otherRedisTemplate) {
        this.executor = executor;
        this.threadLocal = new ThreadLocal<>();
        this.redisProperties = redisProperties;
        log.info(">>>>>> 本地机房redis初始化 ......");
        localStringRedisTemplate = new StringRedisTemplate(localRedisTemplate.getRequiredConnectionFactory());
        this.localRedisTemplate = localRedisTemplate;
        if (this.redisProperties.getCluster2().getActive()) {
            this.otherRedisTemplate = otherRedisTemplate;
            log.info(">>>>>> 异地机房redis初始化 ...... ");
            otherStringRedisTemplate = new StringRedisTemplate(otherRedisTemplate.getRequiredConnectionFactory());
        }
    }

    /**
     * Set the {@code value} and expiration {@code timeout} for {@code key}.
     *
     * @param key     must not be {@literal null}.
     * @param value   must not be {@literal null}.
     * @param timeout must not be {@literal null}.
     * @throws IllegalArgumentException if either {@code key}, {@code value} or {@code timeout} is not present.
     * @see <a href="https://redis.io/commands/setex">Redis Documentation: SETEX</a>
     * @since 2.1
     */
    public <K extends String, V> void setObj(K key, V value, Duration timeout) {
        setObj(key, value, timeout, localRedisTemplate);
        if (redisProperties.getCluster2().getActive()) {
            setObj(key, value, timeout, otherRedisTemplate);
        }
    }


    private <K extends String, V> void setObj(K key, V value, Duration timeout, RedisTemplate<String, Object> redisTemplate) {
        try {
            redisTemplate.opsForValue().set(key, value, timeout);
        } catch (Exception e) {
            log.error("key:{}, redis[set]操作异常: ", key, e);
        }
    }

    /**
     * Increment an integer value stored as string value under {@code key} by one.
     *
     * @param key must not be {@literal null}.
     * @return {@literal null} when used in pipeline / transaction.
     * @see <a href="https://redis.io/commands/incr">Redis Documentation: INCR</a>
     * @since 2.1
     */
    public <K extends String> Long increment(K key) {
        return increment(key, null);
    }

    /**
     * Increment an integer value stored as string value under {@code key} by one.
     *
     * @param key     must not be {@literal null}.
     * @param timeout if not null, then call #expire() method
     * @return {@literal null} when used in pipeline / transaction.
     * @see <a href="https://redis.io/commands/incr">Redis Documentation: INCR</a>
     * @since 2.1
     */
    public <K extends String> Long increment(K key, Duration timeout) {
        return increment(key, 1L, timeout);
    }

    /**
     * Increment an integer value stored as string value under {@code key} by {@code delta}.
     *
     * @param key     must not be {@literal null}.
     * @param delta   delta
     * @param timeout if not null, then call #expire() method
     * @return {@literal null} when used in pipeline / transaction.
     * @see <a href="https://redis.io/commands/incrby">Redis Documentation: INCRBY</a>
     */
    public <K extends String> Long increment(K key, long delta, Duration timeout) {
        Long localRes = increment(key, delta, localRedisTemplate);
        if (redisProperties.getCluster2().getActive()) {
            increment(key, delta, otherRedisTemplate);
        }
        // 设置超时时间
        expire(key, timeout);
        return localRes;
    }


    private <K extends String> Long increment(K key, Long delta, RedisTemplate<String, Object> redisTemplate) {
        Long res = 0L;
        try {
            res = redisTemplate.opsForValue().increment(key, delta);
        } catch (Exception e) {
            log.error("key:{}, redis[increment]操作异常: ", key, e);
        }
        return res;
    }

    /**
     * Decrement an integer value stored as string value under {@code key} by one.
     *
     * @param key must not be {@literal null}.
     * @return {@literal null} when used in pipeline / transaction.
     * @see <a href="https://redis.io/commands/decr">Redis Documentation: DECR</a>
     * @since 2.1
     */
    public <K extends String> Long decrement(K key) {
        return decrement(key, null);
    }

    /**
     * Decrement an integer value stored as string value under {@code key} by one.
     *
     * @param key     must not be {@literal null}.
     * @param timeout if not null, then call #expire() method
     * @return {@literal null} when used in pipeline / transaction.
     * @see <a href="https://redis.io/commands/decr">Redis Documentation: DECR</a>
     * @since 2.1
     */
    public <K extends String> Long decrement(K key, Duration timeout) {
        return decrement(key, 1L, timeout);
    }

    /**
     * Decrement an integer value stored as string value under {@code key} by {@code delta}.
     *
     * @param key     must not be {@literal null}.
     * @param delta   delta
     * @param timeout if not null, then call #expire() method
     * @return {@literal null} when used in pipeline / transaction.
     * @see <a href="https://redis.io/commands/decrby">Redis Documentation: DECRBY</a>
     * @since 2.1
     */
    public <K extends String> Long decrement(K key, Long delta, Duration timeout) {
        Long localRes = decrement(key, delta, localRedisTemplate);
        if (redisProperties.getCluster2().getActive()) {
            decrement(key, delta, otherRedisTemplate);
        }
        // 设置超时时间
        expire(key, timeout);
        return localRes;
    }

    private <K extends String> Long decrement(K key, Long delta, RedisTemplate<String, Object> redisTemplate) {
        Long res = 0L;
        try {
            res = redisTemplate.opsForValue().decrement(key, delta);
        } catch (Exception e) {
            log.error("key:{}, redis[decrement]操作异常: ", key, e);
        }
        return res;
    }

    /**
     * Set time to live for given {@code key}.
     *
     * @param key     must not be {@literal null}.
     * @param timeout when null then will be not call #expire() method
     * @throws IllegalArgumentException if the timeout is {@literal null}.
     * @since 2.3
     */
    public <K extends String> void expire(K key, Duration timeout) {
        expire(key, timeout, localRedisTemplate);
        if (redisProperties.getCluster2().getActive()) {
            expire(key, timeout, otherRedisTemplate);
        }
    }

    private <K extends String> void expire(K key, Duration timeout, RedisTemplate<String, Object> redisTemplate) {
        if (timeout == null) {
            return;
        }
        try {
            redisTemplate.expire(key, timeout);
        } catch (Exception e) {
            log.error("key:{}, redis[expire]操作异常: ", key, e);
        }
    }

    /**
     * Get the value of {@code key}.
     *
     * @param key must not be {@literal null}.
     * @return {@literal null} when used in pipeline / transaction.
     * @see <a href="https://redis.io/commands/get">Redis Documentation: GET</a>
     */
    public <K extends String> Object get(K key) {
        try {
            return localRedisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            log.error("key:{}, redis[get]操作异常: ", key, e);
        }
        return null;
    }


    /**
     * 获取缓存的Integer值
     *
     * @param key          key
     * @param defaultValue 默认值
     */
    public Integer getInt(String key, Integer defaultValue) {
        return Convert.toInt(get(key), defaultValue);
    }


    /**
     * Delete given {@code key}.
     *
     * @param key must not be {@literal null}.
     * @see <a href="https://redis.io/commands/del">Redis Documentation: DEL</a>
     */
    public <K extends String> void delKey(String key) {
        delKeys(Collections.singleton(key));
    }

    /**
     * Delete given {@code keys}.
     *
     * @param keys must not be {@literal null}.
     * @see <a href="https://redis.io/commands/del">Redis Documentation: DEL</a>
     */
    public void delKeys(Collection<String> keys) {
        delKeys(keys, localRedisTemplate);
        if (redisProperties.getCluster2().getActive()) {
            delKeys(keys, otherRedisTemplate);
        }
    }

    private void delKeys(Collection<String> keys, RedisTemplate<String, Object> redisTemplate) {
        try {
            if (keys.size() <= PER_BATCH_COUNT) {
                redisTemplate.delete(keys);
                return;
            }
            // 如果大于100个key, 则使用异步线程池进行处理
            List<List<String>> batchList = CollUtil.split(keys, PER_BATCH_COUNT);
            for (List<String> list : batchList) {
                executor.execute(() -> delKeys(list, redisTemplate));
            }
        } catch (Exception e) {
            log.error("keys: {}, redis[del]操作异常: ", keys, e);
        }
    }

    /**
     * Set {@code key} to hold the string "" and expiration {@code timeout} if {@code key} is absent.
     *
     * @param key     must not be {@literal null}.
     * @param timeout must not be {@literal null}.
     * @return {@literal null} when used in pipeline / transaction.
     * @throws IllegalArgumentException if either {@code key}, {@code value} or {@code timeout} is not present.
     * @see <a href="https://redis.io/commands/set">Redis Documentation: SET</a>
     * @since 2.1
     */
    public Boolean setNx(String key, Duration timeout) {
        return setNx(key, "", timeout);
    }

    /**
     * Set {@code key} to hold the string {@code value} and expiration {@code timeout} if {@code key} is absent.
     *
     * @param key     must not be {@literal null}.
     * @param value   must not be {@literal null}.
     * @param timeout must not be {@literal null}.
     * @return {@literal null} when used in pipeline / transaction.
     * @throws IllegalArgumentException if either {@code key}, {@code value} or {@code timeout} is not present.
     * @see <a href="https://redis.io/commands/set">Redis Documentation: SET</a>
     * @since 2.1
     */
    public Boolean setNx(String key, Object value, Duration timeout) {
        try {
            return localRedisTemplate.opsForValue().setIfAbsent(key, value, timeout);
        } catch (Exception e) {
            log.error("keys: {}, redis[setNx]操作异常: ", key, e);
        }
        return true;
    }

    /**
     * 简易锁
     *
     * @param key         key
     * @param releaseTime 锁自然过期时间/防止出现死锁
     * @return boolean
     */
    public boolean tryLock(String key, Duration releaseTime) {
        // lock acquired
        if (setNx(key, releaseTime)) {
            log.debug("lock acquired success, threadId:[{}], key:[{}]", Thread.currentThread().getId(), key);
            threadLocal.set(key);
            return true;
        }
        log.debug("lock acquired failed, threadId:[{}], key:[{}]", Thread.currentThread().getId(), key);
        return false;
    }

    /**
     * 简易锁
     *
     * @param key         key
     * @param waitTime    最大等待时间
     * @param releaseTime 锁自然过期时间/防止出现死锁
     * @return boolean
     */
    public boolean tryLock(String key, Duration waitTime, Duration releaseTime) {
        long waitMs = waitTime.toMillis();
        long current = System.currentTimeMillis();
        boolean lockRes = false;
        do {
            waitMs -= System.currentTimeMillis() - current;
            if (tryLock(key, releaseTime)) {
                lockRes = true;
                break;
            }
        } while (waitMs > 0);

        return lockRes;
    }

    /**
     * 释放锁
     *
     * @param key key
     */
    public void releaseLock(String key) {
        if (StrUtil.isEmpty(threadLocal.get())) {
            log.warn("key:[{}], lock release failed, current thread does not hold the lock", key);
            return;
        }
        delKeys(Collections.singleton(key), localRedisTemplate);
        // 及时清理, 防止 OOM
        threadLocal.remove();
    }

    /**
     * 批量设置redis超时时间
     *
     * @param keys    keys
     * @param timeout 超时时间
     */
    public void expireBatch(Collection<String> keys, Duration timeout) {
        if (CollUtil.isEmpty(keys) || timeout == null) {
            return;
        }
        // 如果超过上限, 则使用异步方式进行执行
        int limit = 10;
        if (keys.size() > limit) {
            keys.forEach(key -> executor.execute(() -> expire(key, timeout)));
            return;
        }
        keys.forEach(key -> expire(key, timeout));
    }


    /**
     * Set multiple keys to multiple values using key-value pairs provided in {@code tuple}.
     *
     * @param map     must not be {@literal null}.
     * @param timeout when null then will be not call #expire() method
     * @see <a href="https://redis.io/commands/mset">Redis Documentation: MSET</a>
     */
    public <V> void setBatch(Map<String, V> map, Duration timeout) {
        if (CollUtil.isEmpty(map)) {
            return;
        }
        setBatch(map, localRedisTemplate);
        if (redisProperties.getCluster2().getActive()) {
            setBatch(map, otherRedisTemplate);
        }

        expireBatch(map.keySet(), timeout);
    }

    private <V> void setBatch(Map<String, V> map, RedisTemplate<String, Object> redisTemplate) {
        if (map.size() > PER_BATCH_COUNT) {
            List<Map<String, V>> batchList = split(map, PER_BATCH_COUNT);
            batchList.forEach(subMap -> executor.execute(() -> redisTemplate.opsForValue().multiSet(subMap)));
            return;
        }
        // 批量set
        redisTemplate.opsForValue().multiSet(map);
    }

    private <V> List<Map<String, V>> split(Map<String, V> map, int size) {
        if (CollUtil.isEmpty(map)) {
            return Collections.emptyList();
        }

        List<Map<String, V>> res = new ArrayList<>();

        if (map.size() <= size) {
            res.add(map);
            return res;
        }

        List<List<String>> batchKeys = CollUtil.split(map.keySet(), size);
        Map<String, V> vMap;
        for (List<String> keys : batchKeys) {
            vMap = new HashMap<>(16);
            for (String key : keys) {
                vMap.put(key, map.get(key));
            }
            res.add(vMap);
        }

        return res;
    }

    /**
     * Set the {@code value} and expiration {@code timeout} for {@code key}.
     *
     * @param key     must not be {@literal null}.
     * @param value   must not be {@literal null}.
     * @param timeout must not be {@literal null}.
     * @throws IllegalArgumentException if either {@code key}, {@code value} or {@code timeout} is not present.
     * @see <a href="https://redis.io/commands/setex">Redis Documentation: SETEX</a>
     * @since 2.1
     */
    public <K extends String, V extends String> void setStr(K key, V value, Duration timeout) {
        setStr(key, value, timeout, localStringRedisTemplate);
        if (redisProperties.getCluster2().getActive()) {
            setStr(key, value, timeout, otherStringRedisTemplate);
        }
    }

    private <K extends String, V extends String> void setStr(K key, V value, Duration timeout, StringRedisTemplate redisTemplate) {
        try {
            redisTemplate.opsForValue().set(key, value, timeout);
        } catch (Exception e) {
            log.error("key:{}, redis[set]操作异常: ", key, e);
        }
    }

    /**
     * Get the value of {@code key}.
     *
     * @param key must not be {@literal null}.
     * @return {@literal null} when used in pipeline / transaction.
     * @see <a href="https://redis.io/commands/get">Redis Documentation: GET</a>
     */
    public <K extends String> String getStr(K key) {
        return localStringRedisTemplate.opsForValue().get(key);
    }

    /**
     * Set multiple keys to multiple values using key-value pairs provided in {@code tuple}.
     *
     * @param map     must not be {@literal null}.
     * @param timeout when null then will be not call #expire() method.
     * @see <a href="https://redis.io/commands/mset">Redis Documentation: MSET</a>
     */
    public void setBatchStr(Map<String, String> map, Duration timeout) {
        if (CollUtil.isEmpty(map)) {
            return;
        }
        setBatchStr(map, localStringRedisTemplate);
        if (redisProperties.getCluster2().getActive()) {
            setBatchStr(map, otherStringRedisTemplate);
        }
        expireBatch(map.keySet(), timeout);
    }

    private void setBatchStr(Map<String, String> map, StringRedisTemplate redisTemplate) {
        if (map.size() > PER_BATCH_COUNT) {
            List<Map<String, String>> batchList = split(map, PER_BATCH_COUNT);
            batchList.forEach(subMap -> executor.execute(() -> redisTemplate.opsForValue().multiSet(subMap)));
            return;
        }
        // 批量set
        redisTemplate.opsForValue().multiSet(map);
    }

    /**
     * Get the time to live for {@code key} in seconds.
     *
     * @param key must not be {@literal null}.
     * @return {@literal null} when used in pipeline / transaction.
     * @see <a href="https://redis.io/commands/ttl">Redis Documentation: TTL</a>
     */
    public Long getExpire(String key) {
        return localRedisTemplate.getExpire(key);
    }

    /**
     * Determine if given {@code key} exists.
     *
     * @param key must not be {@literal null}.
     * @return Boolean
     * @see <a href="https://redis.io/commands/exists">Redis Documentation: EXISTS</a>
     */
    public Boolean hasKey(String key) {
        return localRedisTemplate.hasKey(key);
    }
}
