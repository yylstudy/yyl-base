package com.linkcircle.redis.config;

import com.linkcircle.redis.config.MultiRedisProperties;
import com.linkcircle.redis.config.Redis2Executor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * @author yang.yonglian
 * @version 1.0.0
 * @Description TODO
 * @createTime 2023/4/18 14:40
 */
@Component
@Slf4j
public class RedisUtil {
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    /**
     * 指定缓存失效时间
     *
     * @param key  键
     * @param time 时间(秒)
     * @return
     */
    public boolean expire(String key, long time) {
        if (time > 0) {
            redisTemplate.expire(key, time, TimeUnit.SECONDS);
            Redis2Executor.execute(redisTemplate2 -> redisTemplate2.expire(key, time, TimeUnit.SECONDS));
        }
        return true;
    }

    /**
     * 根据key 获取过期时间
     *
     * @param key 键 不能为null
     * @return 时间(秒) 返回0代表为永久有效
     */
    public long getExpire(String key) {
        return redisTemplate.getExpire(key, TimeUnit.SECONDS);
    }

    /**
     * 判断key是否存在
     *
     * @param key 键
     * @return true 存在 false不存在
     */
    public boolean hasKey(String key) {
        try {
            return redisTemplate.hasKey(key);
        } catch (Exception e) {
            log.error("redis error",e);
            return false;
        }
    }

    /**
     * 不存在时设置
     * @param key
     * @param value
     * @param ttl
     * @return
     */
    public Boolean setIfAbsent(String key, Object value, long ttl) {
        try {
            // SET NX
            Boolean absent = redisTemplate.opsForValue().setIfAbsent(key, value, ttl, TimeUnit.SECONDS);
            return absent==null?false:absent;
        } catch (Exception e) {
            log.info("redis setIfAbsent 异常: ", e);
        }
        return false;
    }

    /**
     * 删除缓存
     *
     * @param key 可以传一个值 或多个
     */
    @SuppressWarnings("unchecked")
    public void del(String... key) {
        if (key != null && key.length > 0) {
            if (key.length == 1) {
                redisTemplate.delete(key[0]);
                Redis2Executor.execute(redisTemplate2 -> redisTemplate2.delete(key[0]));
            } else {
                //springboot2.4后用法
                redisTemplate.delete(Arrays.asList(key));
                Redis2Executor.execute(redisTemplate2 -> redisTemplate2.delete(Arrays.asList(key)));
            }
        }
    }

    // ============================String=============================
    /**
     * 普通缓存获取
     *
     * @param key 键
     * @return 值
     */
    public Object get(String key) {
        return key == null ? null : redisTemplate.opsForValue().get(key);
    }

    /**
     * 普通缓存放入
     *
     * @param key   键
     * @param value 值
     * @return true成功 false失败
     */
    public boolean set(String key, Object value) {
        redisTemplate.opsForValue().set(key, value);
        Redis2Executor.execute(redisTemplate2 -> redisTemplate2.opsForValue().set(key, value));
        return true;
    }

    /**
     * 普通缓存放入并设置时间
     *
     * @param key   键
     * @param value 值
     * @param time  时间(秒) time要大于0 如果time小于等于0 将设置无限期
     * @return true成功 false 失败
     */
    public boolean set(String key, Object value, long time) {
        if (time > 0) {
            redisTemplate.opsForValue().set(key, value, time, TimeUnit.SECONDS);
            Redis2Executor.execute(redisTemplate2 -> redisTemplate2.opsForValue().set(key, value, time, TimeUnit.SECONDS));
        } else {
            set(key, value);
        }
        return true;
    }

    // ================================Map=================================
    /**
     * HashGet
     *
     * @param key  键 不能为null
     * @param item 项 不能为null
     * @return 值
     */
    public Object hget(String key, String item) {
        return redisTemplate.opsForHash().get(key, item);
    }

    /**
     * 获取hashKey对应的所有键值
     *
     * @param key 键
     * @return 对应的多个键值
     */
    public Map<Object, Object> hmget(String key) {
        return redisTemplate.opsForHash().entries(key);
    }

    /**
     * HashSet
     *
     * @param key 键
     * @param map 对应多个键值
     * @return true 成功 false 失败
     */
    public boolean hmset(String key, Map<String, Object> map) {
        redisTemplate.opsForHash().putAll(key, map);
        Redis2Executor.execute(redisTemplate2 -> redisTemplate2.opsForHash().putAll(key, map));
        return true;
    }

    /**
     * HashSet 并设置时间
     *
     * @param key  键
     * @param map  对应多个键值
     * @param time 时间(秒)
     * @return true成功 false失败
     */
    public boolean hmset(String key, Map<String, Object> map, long time) {
        redisTemplate.opsForHash().putAll(key, map);
        Redis2Executor.execute(redisTemplate2 -> redisTemplate2.opsForHash().putAll(key, map));
        if (time > 0) {
            expire(key, time);
        }
        return true;
    }

    /**
     * 向一张hash表中放入数据,如果不存在将创建
     *
     * @param key   键
     * @param item  项
     * @param value 值
     * @return true 成功 false失败
     */
    public boolean hset(String key, String item, Object value) {
        redisTemplate.opsForHash().put(key, item, value);
        Redis2Executor.execute(redisTemplate2 -> redisTemplate2.opsForHash().put(key, item, value));
        return true;
    }

    /**
     * 向一张hash表中放入数据,如果不存在将创建
     *
     * @param key   键
     * @param item  项
     * @param value 值
     * @param time  时间(秒) 注意:如果已存在的hash表有时间,这里将会替换原有的时间
     * @return true 成功 false失败
     */
    public boolean hset(String key, String item, Object value, long time) {
        redisTemplate.opsForHash().put(key, item, value);
        Redis2Executor.execute(redisTemplate2 -> redisTemplate2.opsForHash().put(key, item, value));
        if (time > 0) {
            expire(key, time);
        }
        return true;
    }

    /**
     * 删除hash表中的值
     *
     * @param key  键 不能为null
     * @param item 项 可以使多个 不能为null
     */
    public void hdel(String key, Object... item) {
        redisTemplate.opsForHash().delete(key, item);
        Redis2Executor.execute(redisTemplate2 -> redisTemplate2.opsForHash().delete(key, item));
    }

    /**
     * 判断hash表中是否有该项的值
     *
     * @param key  键 不能为null
     * @param item 项 不能为null
     * @return true 存在 false不存在
     */
    public boolean hHasKey(String key, String item) {
        return redisTemplate.opsForHash().hasKey(key, item);
    }


    // ============================set=============================
    /**
     * 根据key获取Set中的所有值
     *
     * @param key 键
     * @return
     */
    public Set<Object> sGet(String key) {
        return redisTemplate.opsForSet().members(key);
    }

    /**
     * 根据value从一个set中查询,是否存在
     *
     * @param key   键
     * @param value 值
     * @return true 存在 false不存在
     */
    public boolean sHasKey(String key, Object value) {
        return redisTemplate.opsForSet().isMember(key, value);
    }

    /**
     * 将数据放入set缓存
     *
     * @param key    键
     * @param values 值 可以是多个
     * @return 成功个数
     */
    public long sSet(String key, Object... values) {
        Redis2Executor.execute(redisTemplate2 -> redisTemplate2.opsForSet().add(key, values));
        return redisTemplate.opsForSet().add(key, values);
    }

    /**
     * 将set数据放入缓存
     *
     * @param key    键
     * @param time   时间(秒)
     * @param values 值 可以是多个
     * @return 成功个数
     */
    public long sSetAndTime(String key, long time, Object... values) {
        Long count = redisTemplate.opsForSet().add(key, values);
        Redis2Executor.execute(redisTemplate2 -> redisTemplate2.opsForSet().add(key, values));
        if (time > 0) {
            expire(key, time);
        }
        return count;
    }

    /**
     * 获取set缓存的长度
     *
     * @param key 键
     * @return
     */
    public long sGetSetSize(String key) {
        try {
            return redisTemplate.opsForSet().size(key);
        } catch (Exception e) {
            log.error("redis error",e);
            return 0;
        }
    }

    /**
     * 移除值为value的
     *
     * @param key    键
     * @param values 值 可以是多个
     * @return 移除的个数
     */
    public long setRemove(String key, Object... values) {
        Long count = redisTemplate.opsForSet().remove(key, values);
        Redis2Executor.execute(redisTemplate2 -> redisTemplate2.opsForSet().remove(key, values));
        return count;
    }
    // ===============================list=================================

    /**
     * 获取list缓存的内容
     *
     * @param key   键
     * @param start 开始
     * @param end   结束 0 到 -1代表所有值
     * @return
     */
    public List<Object> lGet(String key, long start, long end) {
        return redisTemplate.opsForList().range(key, start, end);
    }

    /**
     * 获取list缓存的长度
     *
     * @param key 键
     * @return
     */
    public long lGetListSize(String key) {
        return redisTemplate.opsForList().size(key);
    }

    /**
     * 通过索引 获取list中的值
     *
     * @param key   键
     * @param index 索引 index>=0时， 0 表头，1 第二个元素，依次类推；index<0时，-1，表尾，-2倒数第二个元素，依次类推
     * @return
     */
    public Object lGetIndex(String key, long index) {
        return redisTemplate.opsForList().index(key, index);
    }

    /**
     * 将list放入缓存
     *
     * @param key   键
     * @param value 值
     * @return
     */
    public boolean lSet(String key, Object value) {
        redisTemplate.opsForList().rightPush(key, value);
        Redis2Executor.execute(redisTemplate2 -> redisTemplate2.opsForList().rightPush(key, value));
        return true;
    }

    /**
     * 将list放入缓存
     *
     * @param key   键
     * @param value 值
     * @param time  时间(秒)
     * @return
     */
    public boolean lSet(String key, Object value, long time) {
        redisTemplate.opsForList().rightPush(key, value);
        Redis2Executor.execute(redisTemplate2 -> redisTemplate2.opsForList().rightPush(key, value));
        if (time > 0) {
            expire(key, time);
        }
        return true;
    }

    /**
     * 将list放入缓存
     *
     * @param key   键
     * @param value 值
     * @return
     */
    public boolean lSet(String key, List<Object> value) {
        redisTemplate.opsForList().rightPushAll(key, value);
        Redis2Executor.execute(redisTemplate2 -> redisTemplate2.opsForList().rightPushAll(key, value));
        return true;
    }

    /**
     * 将list放入缓存
     *
     * @param key   键
     * @param value 值
     * @param time  时间(秒)
     * @return
     */
    public boolean lSet(String key, List<Object> value, long time) {
        redisTemplate.opsForList().rightPushAll(key, value);
        Redis2Executor.execute(redisTemplate2 -> redisTemplate2.opsForList().rightPushAll(key, value));
        if (time > 0) {
            expire(key, time);
        }
        return true;
    }

    /**
     * 根据索引修改list中的某条数据
     *
     * @param key   键
     * @param index 索引
     * @param value 值
     * @return
     */
    public boolean lUpdateIndex(String key, long index, Object value) {
        redisTemplate.opsForList().set(key, index, value);
        Redis2Executor.execute(redisTemplate2 -> redisTemplate2.opsForList().set(key, index, value));
        return true;
    }

    /**
     * 移除N个值为value
     *
     * @param key   键
     * @param count 移除多少个
     * @param value 值
     * @return 移除的个数
     */
    public long lRemove(String key, long count, Object value) {
        Long remove = redisTemplate.opsForList().remove(key, count, value);
        Redis2Executor.execute(redisTemplate2 -> redisTemplate2.opsForList().remove(key, count, value));
        return remove;
    }

    /**
     *  获取指定前缀的一系列key
     *  使用scan命令代替keys, Redis是单线程处理，keys命令在KEY数量较多时，
     *  操作效率极低【时间复杂度为O(N)】，该命令一旦执行会严重阻塞线上其它命令的正常请求
     * @param keyPrefix
     * @return
     */
    private Set<String> keys(String keyPrefix) {
        String realKey = keyPrefix + "*";

        try {
            return redisTemplate.execute((RedisCallback<Set<String>>) connection -> {
                Set<String> binaryKeys = new HashSet<>();
                //springboot2.4后用法
                Cursor<byte[]> cursor = connection.scan(ScanOptions.scanOptions().match(realKey).count(Integer.MAX_VALUE).build());
                while (cursor.hasNext()) {
                    binaryKeys.add(new String(cursor.next()));
                }

                return binaryKeys;
            });
        } catch (Throwable e) {
            log.error("redis error",e);
        }

        return null;
    }

    /**
     *  删除指定前缀的一系列key
     * @param keyPrefix
     */
    public void removeAll(String keyPrefix) {
        Set<String> keys = keys(keyPrefix);
        redisTemplate.delete(keys);
        Redis2Executor.execute(redisTemplate2 -> redisTemplate2.delete(keys));
    }

    /**
     * 双机房自定义调用
     * @param redisTemplateObjectFunction
     * @param <T>
     * @return
     */
    public <T> T doExecute(Function<RedisTemplate,T> redisTemplateObjectFunction) {
        T t = redisTemplateObjectFunction.apply(redisTemplate);
        Redis2Executor.execute(redisTemplate2 -> redisTemplateObjectFunction.apply(redisTemplate2));
        return t;
    }

    /**
     * 双机房自定义调用
     * @param redisTemplateConsumer
     */
    public void doExecute(Consumer<RedisTemplate> redisTemplateConsumer) {
        redisTemplateConsumer.accept(redisTemplate);
        Redis2Executor.execute(redisTemplate2 -> redisTemplateConsumer.accept(redisTemplate2));
    }

}
