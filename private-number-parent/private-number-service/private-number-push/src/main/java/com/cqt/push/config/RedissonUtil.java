package com.cqt.push.config;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.cqt.model.common.properties.MultiRedisProperties;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.*;
import org.redisson.client.protocol.ScoredEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @author linshiqiang
 * @date 2021/7/12 14:17
 * 多机房redisson操作 工具类
 */
@Component
@Slf4j
public class RedissonUtil {

    @Qualifier(value = "redissonClient")
    @Autowired
    private RedissonClient redissonClient;

    @Qualifier(value = "redissonClient2")
    @Autowired(required = false)
    private RedissonClient redissonClient2;

    @Resource
    private MultiRedisProperties redisProperties;

    /**
     * 批量设置过期时间
     *
     * @param keys keys
     */
    public void expireBatch(Collection<String> keys, long ttl, TimeUnit unit) {
        if (keys.isEmpty()) {
            log.warn("本次 redis 批量设置过期时间, 数据为空");
            return;
        }

        try {
            RBatch rBatch = redissonClient.createBatch();
            keys.forEach(key -> {
                rBatch.getBucket(key).expireAsync(ttl, unit);
            });
            rBatch.execute();
        } catch (Exception e) {
            log.error("本次 redis 批量设置过期时间, 出现异常:", e);
        }
        if (redisProperties.getCluster2().getActive()) {
            RBatch rBatch = redissonClient2.createBatch();
            keys.forEach(key -> {
                rBatch.getBucket(key).expireAsync(ttl, unit);
            });
            rBatch.execute();
        }
    }

    /**
     * 批量set
     *
     * @param map map集合
     */
    public <V> void setBatch(Map<String, V> map) {
        log.info("redis 批量添加, 数据条数: {}", map.size());
        if (map.isEmpty()) {
            log.warn("本次 redis 批量添加, 数据为空");
            return;
        }

        try {
            try {
                RBatch rBatch = redissonClient.createBatch();
                map.forEach((key, value) -> {
                    rBatch.getBucket(key).setAsync(value);
                });
                rBatch.execute();
            } catch (Exception e) {
                log.error("redisson set操作异常: ", e);
            }
            if (redisProperties.getCluster2().getActive()) {
                RBatch rBatch = redissonClient2.createBatch();
                map.forEach((key, value) -> {
                    rBatch.getBucket(key).setAsync(value);
                });
                rBatch.execute();
            }
        } catch (Exception e) {
            log.error("redisson set操作异常: ", e);
        }
    }

    public void setObject(String key, Object data) {
        try {
            try {
                redissonClient.getBucket(key).setAsync(data);
            } catch (Exception e) {
                log.error("redisson set操作异常: ", e);
            }
            if (redisProperties.getCluster2().getActive()) {
                redissonClient2.getBucket(key).setAsync(data);
            }
        } catch (Exception e) {
            log.error("redisson set操作异常: ", e);
        }
    }

    public void setObject(String key, Object data, long timeToLive, TimeUnit timeUnit) {
        try {
            try {
                redissonClient.getBucket(key).setAsync(data, timeToLive, timeUnit);
            } catch (Exception e) {
                log.error("redisson set and ttl操作异常: ", e);
            }
            if (redisProperties.getCluster2().getActive()) {
                redissonClient2.getBucket(key).setAsync(data, timeToLive, timeUnit);
            }
        } catch (Exception e) {
            log.error("redisson set and ttl操作异常: ", e);
        }
    }

    public Object getObject(String key) {
        return redissonClient.getBucket(key).get();
    }

    public void setString(String key, String data) {
        try {
            try {
                redissonClient.getBucket(key).setAsync(data);
            } catch (Exception e) {
                log.error("redisson setString操作异常: ", e);
            }
            if (redisProperties.getCluster2().getActive()) {
                redissonClient2.getBucket(key).set(data);
            }
        } catch (Exception e) {
            log.error("redisson setString操作异常: ", e);
        }
    }

    public void setString11(String key, String data) {
        try {
            try {
                redissonClient.getBucket(key).setAsync(data);
            } catch (Exception e) {
                log.error("redisson setString操作异常: ", e);
            }

        } catch (Exception e) {
            log.error("redisson setString操作异常: ", e);
        }
    }

    public String getString(String key) {
        RBucket<String> bucket = redissonClient.getBucket(key);
        return bucket.get();
    }

    public Boolean isExistString(String key) {
        return redissonClient.getBucket(key).isExists();
    }

    public void increment(String key) {
        try {
            redissonClient.getLongAdder(key).increment();
            if (redisProperties.getCluster2().getActive()) {
                redissonClient2.getLongAdder(key).increment();
            }
        } catch (Exception e) {
            log.error("redisson LongAdder increment 操作异常: ", e);
        }
    }

    public void decrement(String key) {
        try {
            redissonClient.getLongAdder(key).decrement();
            if (redisProperties.getCluster2().getActive()) {
                redissonClient2.getLongAdder(key).decrement();
            }
        } catch (Exception e) {
            log.error("redisson LongAdder decrement 操作异常: ", e);
        }
    }

    // list

    public List<Object> getList(String key) {
        RList<Object> list = redissonClient.getList(key);
        return list.range(0, -1);
    }

    public List<Object> getListString(String key) {
        RList<Object> list = redissonClient.getList(key);
        return list.readAll();
    }

    public void setList(String key, List<Object> objectList) {
        try {
            redissonClient.getList(key).addAllAsync(objectList);
            if (redisProperties.getCluster2().getActive()) {
                redissonClient2.getList(key).addAllAsync(objectList);
            }
        } catch (Exception e) {
            log.error("redisson setList 操作异常: ", e);
        }
    }

    public void setListString(String key, List<String> stringList) {
        try {
            try {
                redissonClient.getList(key).addAll(stringList);
            } catch (Exception e) {
                log.error("redisson setList 操作异常: ", e);
            }
            if (redisProperties.getCluster2().getActive()) {
                redissonClient2.getList(key).addAllAsync(stringList);
            }
        } catch (Exception e) {
            log.error("redisson setList 操作异常: ", e);
        }
    }

    public Boolean containList(String key, Object object) {
        RList<Object> list = redissonClient.getList(key);
        return list.contains(object);
    }

    // deque

    /**
     * 取出list 头部
     */
    public String getDequeFirst(String key) {

        Object first = null;
        try {
            RDeque<Object> deque = redissonClient.getDeque(key);
            // 取第一个
            first = deque.removeFirst();

            // 添加到尾部
            deque.addLast(first);
            if (redisProperties.getCluster2().getActive()) {
                RDeque<Object> rDeque = redissonClient.getDeque(key);
                Object removeFirst = rDeque.removeFirst();
                rDeque.addLast(removeFirst);
            }
        } catch (Exception e) {
            log.error("redisson getDequeFirst 操作异常: ", e);
        }
        return Convert.toStr(first);
    }

    /**
     * deque 设置值
     */
    public Boolean setDeque(String key, List<String> stringList) {
        boolean addAll;
        try {
            addAll = redissonClient.getDeque(key).addAll(stringList);
            if (redisProperties.getCluster2().getActive()) {
                redissonClient2.getDeque(key).addAllAsync(stringList);
            }
        } catch (Exception e) {
            log.error("redisson deque addAll 操作异常: ", e);
            return false;
        }
        return addAll;
    }


    // set

    public Set<String> getSet(String key) {
        RSet<String> set = redissonClient.getSet(key);
        return set.readAll();
    }

    public Set<String> getSetString(String key) {
        RSet<String> set = redissonClient.getSet(key);
        return set.readAll();
    }

    public Set<String> getSetStringBatch(String... key) {

        RBatch batch = redissonClient.createBatch();
        for (String s : key) {
            batch.getSet(s).readAllAsync();
        }
        BatchResult<?> execute = batch.execute();
        List<?> responses = execute.getResponses();
        Set<String> result = new HashSet<>();
        for (Object res : responses) {
            Set<String> set = (Set<String>) res;
            result.addAll(set);
        }
        return result;
    }

    /**
     * 从set随机取出count个元素
     */
    public Set<Object> getRandomSetByCount(String key, int count) {
        return redissonClient.getSet(key).random(count);
    }

    public Integer getSetCount(String key) {
        RSet<Object> set = redissonClient.getSet(key);
        return set.size();
    }

    public Object getSetRandom(String key) {
        return redissonClient.getSet(key).random();
    }

    /**
     * set 随机移出一个元素
     */
    public String removeSetRandom(String key) {
        String random = "";
        try {
            try {
                random = Convert.toStr(redissonClient.getSet(key).removeRandom(), "");
            } catch (Exception e) {
                log.error("redisson removeSetRandom 操作异常: ", e);
            }
            if (redisProperties.getCluster2().getActive()) {
                if (StrUtil.isNotEmpty(random)) {
                    String finalRandom = random;
                    try {
                        redissonClient2.getSet(key).remove(finalRandom);
                    } catch (Exception e) {
                        log.error("异地redis异常: {}", e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            log.error("redisson removeSetRandom 操作异常: ", e);
        }
        return random;
    }

    public Set<Object> removeSetRandomByCount(String key, Integer count) {
        Set<Object> objects = null;
        try {
            try {
                objects = redissonClient.getSet(key).removeRandom(count);
            } catch (Exception e) {
                log.error("redisson removeSetRandom 操作异常: ", e);
            }
            if (redisProperties.getCluster2().getActive()) {
                if (CollUtil.isNotEmpty(objects)) {
                    Set<Object> finalObjects = objects;
                    try {
                        redissonClient2.getSet(key).removeAll(finalObjects);
                    } catch (Exception e) {
                        log.error("异地redis异常: {}", e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            log.error("redisson removeSetRandom 操作异常: ", e);
        }
        return objects;
    }

    /**
     * set 添加元素
     */
    public Boolean addSet(String key, Object object) {
        boolean add = true;
        try {
            try {
                add = redissonClient.getSet(key).add(object);
            } catch (Exception e) {
                log.error("redisson addSet 操作异常: ", e);
            }
            if (redisProperties.getCluster2().getActive()) {
                try {
                    redissonClient2.getSet(key).add(object);
                } catch (Exception e) {
                    log.error("异地redis异常: {}", e.getMessage());
                }
            }
        } catch (Exception e) {
            log.error("redisson addSet 操作异常: ", e);
        }
        return add;
    }

    public Boolean removeSet(String key, Object object) {
        boolean success = false;
        try {
            if (ObjectUtil.isEmpty(object)) {
                return false;
            }
            try {
                success = redissonClient.getSet(key).remove(object);
            } catch (Exception e) {
                log.error("redisson removeSet 操作异常: ", e);
            }
            if (redisProperties.getCluster2().getActive()) {
                try {
                    redissonClient2.getSet(key).remove(object);
                } catch (Exception e) {
                    log.error("异地redis异常: {}", e.getMessage());
                }
            }
        } catch (Exception e) {
            log.error("redisson removeSet 操作异常: ", e);
        }
        return success;
    }

    public Boolean containsSet(String key, Object object) {
        RSet<Object> set = redissonClient.getSet(key);
        return set.contains(object);
    }

    public void addAllSet(String key, Set<String> objectSet) {
        try {
            redissonClient.getSet(key).addAllAsync(objectSet);
            if (redisProperties.getCluster2().getActive()) {
                redissonClient2.getSet(key).addAllAsync(objectSet);
            }
        } catch (Exception e) {
            log.error("redisson addAllSet 操作异常: ", e);
        }
    }


    public Boolean addAllSet(String key, List<String> valueList) {
        boolean success = false;
        try {
            success = redissonClient.getSet(key).addAll(valueList);
        } catch (Exception e) {
            log.error("redisson addAllSet 操作异常: {}", e.getMessage());
        }

        if (redisProperties.getCluster2().getActive()) {
            try {
                redissonClient2.getSet(key).addAll(valueList);
            } catch (Exception e) {
                log.error("异地redis addAllSet异常: {}", e.getMessage());
            }
        }
        return success;
    }

    public Boolean delSet(String key) {
        boolean success = false;
        try {
            try {
                success = redissonClient.getSet(key).delete();
            } catch (Exception e) {
                log.error("redisson delSet 操作异常: ", e);
            }
            if (redisProperties.getCluster2().getActive()) {
                try {
                    redissonClient2.getSet(key).delete();
                } catch (Exception e) {
                    log.error("异地redis异常: {}", e.getMessage());
                }
            }
        } catch (Exception e) {
            log.error("redisson delSet 操作异常: ", e);
        }
        return success;
    }

    public void setSetString(String key, List<String> stringList) {
        try {
            try {
                redissonClient.getSet(key).addAll(stringList);
            } catch (Exception e) {
                log.error("redisson setSetString List<String> 操作异常: ", e);
            }
            if (redisProperties.getCluster2().getActive()) {
                try {
                    redissonClient2.getSet(key).addAll(stringList);
                } catch (Exception e) {
                    log.error("异地redis异常: {}", e.getMessage());
                }
            }
        } catch (Exception e) {
            log.error("redisson setSetString List<String> 操作异常: ", e);
        }
    }

    public Boolean isExistSet(String key) {
        return redissonClient.getSet(key).isExists();
    }

    public void setSet(String key, Set<Object> objectList) {
        try {
            redissonClient.getSet(key).addAll(objectList);
            if (redisProperties.getCluster2().getActive()) {
                redissonClient2.getSet(key).addAllAsync(objectList);
            }
        } catch (Exception e) {
            log.error("redisson setSet Set<Object> 操作异常: ", e);
        }
    }

    public void setSetString(String key, Set<String> objectList) {
        try {
            redissonClient.getSet(key).addAll(objectList);
            if (redisProperties.getCluster2().getActive()) {
                redissonClient2.getSet(key).addAllAsync(objectList);
            }
        } catch (Exception e) {
            log.error("redisson setSetString Set<String> 操作异常: ", e);
        }
    }

    public void setSet(String key, Object object) {
        try {
            redissonClient.getSet(key).addAsync(object);
            if (redisProperties.getCluster2().getActive()) {
                redissonClient2.getSet(key).addAsync(object);
            }
        } catch (Exception e) {
            log.error("redisson setSet Object 操作异常: ", e);
        }
    }

    // hash

    public String getHashStringByItem(String key, String item) {
        RMap<String, String> map = redissonClient.getMap(key);
        return map.get(item);
    }

    public Object getHashByItem(String key, String item) {
        RMap<Object, Object> map = redissonClient.getMap(key);

        return map.get(item);
    }

    public void delHash(String key) {
        try {
            redissonClient.getMap(key).deleteAsync();
            if (redisProperties.getCluster2().getActive()) {
                redissonClient2.getMap(key).deleteAsync();
            }
        } catch (Exception e) {
            log.error("redisson delHash 操作异常: ", e);
        }
    }

    public void setHash(String key, Map<String, Object> dataMap) {
        try {
            redissonClient.getMap(key).putAllAsync(dataMap, 100);
            if (redisProperties.getCluster2().getActive()) {
                redissonClient2.getMap(key).putAllAsync(dataMap, 100);
            }
        } catch (Exception e) {
            log.error("redisson setHash Map 操作异常: ", e);
        }
    }

    public void setHash(String key, String item, Object data) {
        try {
            redissonClient.getMap(key).putAsync(item, data);
            if (redisProperties.getCluster2().getActive()) {
                redissonClient2.getMap(key).putAsync(item, data);
            }
        } catch (Exception e) {
            log.error("redisson setHash item Object 操作异常: ", e);
        }
    }

    public void lock(String key) {
        RLock lock = redissonClient.getLock(key);
        lock.lock(10, TimeUnit.SECONDS);
    }

    // zset

    /**
     * 批量添加zset元素 分数
     */
    public Integer addAllZset(String key, Map<Object, Double> objects) {
        int i = 0;
        try {
            i = redissonClient.getScoredSortedSet(key).addAll(objects);
            if (redisProperties.getCluster2().getActive()) {
                redissonClient2.getScoredSortedSet(key).addAllAsync(objects);
            }
        } catch (Exception e) {
            log.error("redisson addAllZset 操作异常: ", e);
        }
        return i;
    }

    /**
     * zset key 是否存在
     */
    public Boolean isExistZsetKey(String key) {
        return redissonClient.getScoredSortedSet(key).isExists();
    }

    /**
     * zset 添加一个元素
     */
    public Boolean addZset(String key, double score, String value) {
        boolean add = false;
        try {
            add = redissonClient.getScoredSortedSet(key).add(score, value);
            if (redisProperties.getCluster2().getActive()) {
                redissonClient2.getScoredSortedSet(key).addAsync(score, value);
            }
        } catch (Exception e) {
            log.error("redisson addZset 操作异常: ", e);
        }
        return add;
    }

    /**
     * 移除整个zset
     *
     * @param key
     * @return
     */
    public Boolean deleteZSet(String key) {
        boolean bool = false;
        try {
            bool = redissonClient.getScoredSortedSet(key).delete();
            if (redisProperties.getCluster2().getActive()) {
                redissonClient2.getScoredSortedSet(key).deleteAsync();
            }
        } catch (Exception e) {
            log.error("redisson deleteZSet 操作异常: ", e);
        }
        return bool;
    }

    /**
     * 获取zset score和value
     *
     * @param key        键
     * @param startIndex 开始索引 0 开始
     * @param endIndex   结束索引
     * @return ScoredEntry
     */
    public ScoredEntry<Object> getZsetScoredEntry(String key, int startIndex, int endIndex) {
        try {
            RScoredSortedSet<Object> scoredSortedSet = redissonClient.getScoredSortedSet(key);
            Collection<ScoredEntry<Object>> scoredEntries = scoredSortedSet.entryRange(startIndex, endIndex);
            Optional<ScoredEntry<Object>> first = scoredEntries.stream().findFirst();
            if (first.isPresent()) {
                return scoredEntries.stream().findFirst().get();
            }
        } catch (Exception e) {
            log.error("redisson getZsetScoredEntry 操作异常: ", e);
        }
        return null;
    }

    /**
     * zset 移除头一个元素
     */
    public void removeZsetFirst(String key) {
        try {
            redissonClient.getScoredSortedSet(key).pollFirst();
            if (redisProperties.getCluster2().getActive()) {
                redissonClient2.getScoredSortedSet(key).pollFirst();
            }
        } catch (Exception e) {
            log.error("redisson getZsetScoredEntry 操作异常: ", e);
        }
    }

    /**
     * 移除一个元素
     *
     * @param key    键
     * @param object 元素
     */
    public void removeZsetObject(String key, Object object) {
        redissonClient.getScoredSortedSet(key).remove(object);
        if (redisProperties.getCluster2().getActive()) {
            redissonClient2.getScoredSortedSet(key).removeAsync(object);
        }
    }

    public Boolean removeSetBatch(String key, Collection<String> collection) {
        boolean success = false;
        try {
            success = redissonClient.getSet(key).removeAll(collection);
            if (redisProperties.getCluster2().getActive()) {
                try {
                    redissonClient2.getSet(key).removeAllAsync(collection);
                } catch (Exception e) {
                    log.error("异地redis removeSetBatch异常: {}", e.getMessage());
                }
            }
        } catch (Exception e) {
            log.error("redisson removeSetBatch 操作异常: ", e);
        }
        return success;
    }

    // ttl

    public Boolean isNotExistString(String key) {
        long l = redissonClient.getBucket(key).remainTimeToLive();

        return -2 == l;
    }

    public Boolean existSet(String key) {
        long l = redissonClient.getSet(key).remainTimeToLive();
        return -2 == l;
    }


    public Boolean delKey(String key) {
        boolean success = false;
        try {
            try {
                success = redissonClient.getBucket(key).delete();
            } catch (Exception e) {
                log.error("redisson delSet 操作异常: ", e);
            }
            if (redisProperties.getCluster2().getActive()) {
                try {
                    redissonClient2.getBucket(key).delete();
                } catch (Exception e) {
                    log.error("异地redis异常: {}", e.getMessage());
                }
            }
        } catch (Exception e) {
            log.error("redisson delKey 操作异常: ", e);
        }
        return success;
    }

    /**
     * 设置锁
     * 过期时间单位 分钟
     */
    public boolean setStringNx(String key, long expire) {
        boolean bool = redissonClient.getBucket(key).trySet("", expire, TimeUnit.MINUTES);
        if (redisProperties.getCluster2().getActive()) {
            redissonClient2.getBucket(key).trySet("", expire, TimeUnit.MINUTES);
        }
        return bool;
    }

    public RLock getLock(String key) {
        return redissonClient.getLock(key);
    }

    public void unLock(String key) {
        redissonClient.getLock(key).unlock();
    }

    public Long getTtl(String key) {
        return redissonClient.getBucket(key).remainTimeToLive();
    }
}
