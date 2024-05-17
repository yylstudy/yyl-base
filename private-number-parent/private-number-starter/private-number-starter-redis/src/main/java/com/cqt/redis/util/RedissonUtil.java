package com.cqt.redis.util;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.cqt.redis.config.DoubleRedisProperties;
import org.redisson.api.*;
import org.redisson.client.protocol.ScoredEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * @author linshiqiang
 * @date 2021/7/12 14:17
 * 多机房redisson操作 工具类
 */
@Component
public class RedissonUtil {

    private static final Logger log = LoggerFactory.getLogger(RedissonUtil.class);

    @Qualifier(value = "redissonClient")
    @Autowired
    private RedissonClient redissonClient;

    @Qualifier(value = "redissonClient2")
    @Autowired(required = false)
    private RedissonClient redissonClient2;

    @Qualifier(value = "redissonClient3")
    @Autowired(required = false)
    private RedissonClient redissonClient3;

    @Resource
    private DoubleRedisProperties redisProperties;

    @Resource(name = "otherExecutor")
    private ThreadPoolTaskExecutor otherExecutor;

    private final static Map<String, RedissonClient> REDISSON_CLIENT_MAP = new ConcurrentHashMap<>(16);

    @PostConstruct
    public void initClient() {
        String location = redisProperties.getCluster().getLocation();
        REDISSON_CLIENT_MAP.put(location, redissonClient);
        if (Optional.ofNullable(redissonClient2).isPresent()) {
            String location2 = redisProperties.getCluster2().getLocation();
            REDISSON_CLIENT_MAP.put(location2, redissonClient2);
        }
        if (Optional.ofNullable(redissonClient3).isPresent()) {
            String location3 = redisProperties.getCluster3().getLocation();
            REDISSON_CLIENT_MAP.put(location3, redissonClient3);
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

    public Boolean setObject(String key, Object data, long timeToLive, TimeUnit timeUnit) {
        try {

            redissonClient.getBucket(key).set(data, timeToLive, timeUnit);

            if (redisProperties.getCluster2().getActive()) {
                try {
                    otherExecutor.execute(() -> {
                        redissonClient2.getBucket(key).set(data, timeToLive, timeUnit);
                    });
                } catch (Exception e) {
                    log.error("redisson set and ttl操作异常: ", e);
                }
            }
        } catch (Exception e) {
            log.error("redisson set and ttl操作异常: ", e);
            return false;
        }
        return true;
    }


    public Boolean setObject(String location, String key, Object data, long timeToLive, TimeUnit timeUnit) {
        try {
            RedissonClient redissonClient = REDISSON_CLIENT_MAP.get(location);
            redissonClient.getBucket(key).set(data, timeToLive, timeUnit);

        } catch (Exception e) {
            log.error("redisson set and ttl操作异常: ", e);
            return false;
        }
        return true;
    }

    public Object getObject(String key) {
        return redissonClient.getBucket(key).get();
    }

    public Object getObject2(String key) {
        return redissonClient2.getBucket(key).get();
    }

    public void setString(String location, String key, String data) {
        RedissonClient redissonClient = REDISSON_CLIENT_MAP.get(location);
        redissonClient.getBucket(key).set(data);
    }

    public Boolean setString(String key, String data) {
        try {
            try {
                redissonClient.getBucket(key).set(data);
            } catch (Exception e) {
                log.error("redisson setString操作异常: ", e);
            }
            if (redisProperties.getCluster2().getActive()) {
                otherExecutor.execute(() -> {
                    redissonClient2.getBucket(key).set(data);
                });
            }
        } catch (Exception e) {
            log.error("redisson setString操作异常: ", e);
            return false;
        }
        return true;
    }

    public Boolean setStringX(String key, String data) {
        try {
            redissonClient3.getBucket(key).set(data);
        } catch (Exception e) {
            log.error("redisson setString操作异常: ", e);
            return false;
        }
        return true;
    }

    public Boolean setString(String key, String data, long timeToLive, TimeUnit timeUnit) {
        try {
            try {
                redissonClient.getBucket(key).set(data, timeToLive, timeUnit);
            } catch (Exception e) {
                log.error("redisson setString操作异常: ", e);
            }
            if (redisProperties.getCluster2().getActive()) {
                otherExecutor.execute(() -> {
                    redissonClient2.getBucket(key).set(data, timeToLive, timeUnit);
                });
            }
        } catch (Exception e) {
            log.error("redisson setString操作异常: ", e);
            return false;
        }
        return true;
    }

    public String getString(String key) {
        RBucket<String> bucket = redissonClient.getBucket(key);
        return bucket.get();
    }


    public String getStringX(String key) {
        RBucket<String> bucket = redissonClient3.getBucket(key);
        return bucket.get();
    }

    public String getStringOfBack(String key) {
        if (redissonClient2 == null) {
            return "";
        }
        RBucket<String> bucket = redissonClient2.getBucket(key);
        return bucket.get();
    }

    public String getString(String location, String key) {
        RedissonClient redissonClient = REDISSON_CLIENT_MAP.get(location);
        RBucket<String> bucket = redissonClient.getBucket(key);
        return bucket.get();
    }

    public Boolean isExistString(String key) {
        return redissonClient.getBucket(key).isExists();
    }

    public Boolean isExistString(String location, String key) {
        RedissonClient redissonClient = REDISSON_CLIENT_MAP.get(location);
        return redissonClient.getBucket(key).isExists();
    }

    public void increment(String key) {
        try {
            redissonClient.getAtomicLong(key).addAndGet(1);
            if (redisProperties.getCluster2().getActive()) {
                otherExecutor.execute(() -> {
                    try {
                        redissonClient2.getAtomicLong(key).addAndGet(1);
                    } catch (Exception e) {
                        log.error("redisson AtomicLong increment 异地操作异常: ", e);
                    }
                });
            }
        } catch (Exception e) {
            log.error("redisson AtomicLong increment 操作异常: ", e);
        }
    }

    public Long increment(String key, Duration duration) {
        RAtomicLong rAtomicLong = redissonClient.getAtomicLong(key);
        long get = rAtomicLong.incrementAndGet();
        rAtomicLong.expire(duration.getSeconds(), TimeUnit.SECONDS);
        return get;
    }

    public void increment(String location, String key) {
        try {
            RedissonClient redissonClient = REDISSON_CLIENT_MAP.get(location);
            redissonClient.getAtomicLong(key).addAndGet(1);
        } catch (Exception e) {
            log.error("redisson LongAdder increment 操作异常: ", e);
        }
    }


    public void decrement(String key) {
        try {
            redissonClient.getAtomicLong(key).decrementAndGet();
            if (redisProperties.getCluster2().getActive()) {
                otherExecutor.execute(() -> {
                    try {
                        redissonClient2.getAtomicLong(key).decrementAndGet();
                    } catch (Exception e) {
                        log.error("redisson AtomicLong decrement 异地操作异常: ", e);
                    }
                });
            }
        } catch (Exception e) {
            log.error("redisson AtomicLong decrement 操作异常: ", e);
        }
    }

    // list

    public List<Object> getList(String key) {
        RList<Object> list = redissonClient.getList(key);
        return list.range(0, -1);
    }

    public List<String> readAllList(String key) {
        RList<String> list = redissonClient.getList(key);
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

    public void removeList(String key, String value) {
        redissonClient.getList(key).remove(value);
        if (redisProperties.getCluster2().getActive()) {
            redissonClient2.getList(key).remove(value);
        }
    }

    public Boolean delDeque(String key) {
        boolean del;
        try {
            del = redissonClient.getDeque(key).delete();
            if (redisProperties.getCluster2().getActive()) {
                redissonClient2.getDeque(key).delete();
            }
        } catch (Exception e) {
            log.error("redisson deque del 操作异常: ", e);
            return false;
        }
        return del;
    }

    public boolean setDequeString(String key, List<String> stringList) {
        boolean success = false;
        try {
            try {
                success = redissonClient.getDeque(key).addAll(stringList);
            } catch (Exception e) {
                log.error("redisson setList 操作异常: ", e);
            }
            if (redisProperties.getCluster2().getActive()) {
                redissonClient2.getDeque(key).addAll(stringList);
            }
        } catch (Exception e) {
            log.error("redisson setList 操作异常: ", e);
        }
        return success;
    }

    /**
     * 取出双向list 第一个元素
     */
    public String getDequeFirst(String key) {

        RDeque<String> deque = redissonClient.getDeque(key);
        // 取第一个
        String first = deque.pollFirst();

        if (redisProperties.getCluster2().getActive()) {
            if (ObjectUtil.isNotEmpty(first)) {
                otherExecutor.execute(() -> {
                    try {
                        RDeque<Object> rDeque = redissonClient2.getDeque(key);
                        rDeque.remove(first);
                    } catch (Exception e) {
                        log.error("other redisson getDequeFirst remove操作异常: ", e);
                    }
                });
            }
        }

        return first;
    }

    /**
     * deque移除元素
     */
    public Boolean removeDequeValue(String key, String value) {
        RDeque<String> deque = redissonClient.getDeque(key);
        boolean remove = deque.remove(value);
        if (redisProperties.getCluster2().getActive()) {
            otherExecutor.execute(() -> {
                try {
                    RDeque<Object> rDeque = redissonClient2.getDeque(key);
                    rDeque.remove(value);
                } catch (Exception e) {
                    log.error("other redisson removeDequeValue remove操作异常: ", e);
                }
            });
        }
        return remove;
    }

    public Boolean isExistDeque(String key) {
        return redissonClient.getDeque(key).isExists();
    }

    /**
     * 双向list 添加元素到最后一个
     */
    public boolean offerLastDeque(String key, String value) {
        RDeque<Object> deque = redissonClient.getDeque(key);
        boolean offerLast = deque.offerLast(value);
        if (redisProperties.getCluster2().getActive()) {
            otherExecutor.execute(() -> {
                try {
                    RDeque<Object> rDeque = redissonClient2.getDeque(key);
                    rDeque.offerLast(value);
                } catch (Exception e) {
                    log.error("other redisson offerLastDeque offerLast 操作异常: ", e);
                }
            });
        }
        return offerLast;
    }

    public Boolean containDeque(String key, String value) {
        RDeque<String> deque = redissonClient.getDeque(key);
        return deque.contains(value);
    }

    /**
     * deque 设置值
     */
    public Boolean setDeque(String key, List<String> stringList) {
        boolean addAll;
        try {
            addAll = redissonClient.getDeque(key).addAll(stringList);
            if (redisProperties.getCluster2().getActive()) {
                redissonClient2.getDeque(key).addAll(stringList);
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

    public Set<String> getSet(String location, String key) {
        RedissonClient redissonClient = REDISSON_CLIENT_MAP.get(location);
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

    public <V> V getSetRandom(String key) {
        RSet<V> set = redissonClient.getSet(key);
        return set.random();
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
                    otherExecutor.execute(() -> {
                        try {
                            redissonClient2.getSet(key).remove(finalRandom);
                        } catch (Exception e) {
                            log.error("异地redis异常: {}", e.getMessage());
                        }
                    });
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
                    otherExecutor.execute(() -> {
                        try {
                            redissonClient2.getSet(key).removeAll(finalObjects);
                        } catch (Exception e) {
                            log.error("异地redis异常: {}", e.getMessage());
                        }
                    });
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
                otherExecutor.execute(() -> {
                    try {
                        redissonClient2.getSet(key).add(object);
                    } catch (Exception e) {
                        log.error("异地redis异常: {}", e.getMessage());
                    }
                });
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
                otherExecutor.execute(() -> {
                    try {
                        redissonClient2.getSet(key).remove(object);
                    } catch (Exception e) {
                        log.error("异地redis异常: {}", e.getMessage());
                    }
                });
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
            redissonClient.getSet(key).addAll(objectSet);
            if (redisProperties.getCluster2().getActive()) {
                redissonClient2.getSet(key).addAll(objectSet);
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
            otherExecutor.execute(() -> {
                try {
                    redissonClient2.getSet(key).addAll(valueList);
                } catch (Exception e) {
                    log.error("异地redis addAllSet异常: {}", e.getMessage());
                }
            });
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
                otherExecutor.execute(() -> {
                    try {
                        redissonClient2.getSet(key).delete();
                    } catch (Exception e) {
                        log.error("异地redis异常: {}", e.getMessage());
                    }
                });
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
                otherExecutor.execute(() -> {
                    try {
                        redissonClient2.getSet(key).addAll(stringList);
                    } catch (Exception e) {
                        log.error("异地redis异常: {}", e.getMessage());
                    }
                });
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
                redissonClient2.getSet(key).addAll(objectList);
            }
        } catch (Exception e) {
            log.error("redisson setSet Set<Object> 操作异常: ", e);
        }
    }

    public void setSetString(String key, Set<String> objectList) {
        try {
            redissonClient.getSet(key).addAll(objectList);
            if (redisProperties.getCluster2().getActive()) {
                redissonClient2.getSet(key).addAll(objectList);
            }
        } catch (Exception e) {
            log.error("redisson setSetString Set<String> 操作异常: ", e);
        }
    }

    public void setSet(String key, Object object) {
        try {
            redissonClient.getSet(key).add(object);
            if (redisProperties.getCluster2().getActive()) {
                redissonClient2.getSet(key).add(object);
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

    public Object getHashByItem(String location, String key, String item) {
        RedissonClient redissonClient = REDISSON_CLIENT_MAP.get(location);
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
            redissonClient.getMap(key).putAll(dataMap, 100);
            if (redisProperties.getCluster2().getActive()) {
                redissonClient2.getMap(key).putAll(dataMap, 100);
            }
        } catch (Exception e) {
            log.error("redisson setHash Map 操作异常: ", e);
        }
    }

    public void setHash(String key, String item, Object data) {
        try {
            redissonClient.getMap(key).put(item, data);
            if (redisProperties.getCluster2().getActive()) {
                redissonClient2.getMap(key).put(item, data);
            }
        } catch (Exception e) {
            log.error("redisson setHash item Object 操作异常: ", e);
        }
    }

    public void lock(String key) {
        RLock lock = redissonClient.getLock(key);
        lock.lock(10, TimeUnit.SECONDS);
    }

    public void lock(String key, long leaseTime) {
        RLock lock = redissonClient.getLock(key);
        lock.lock(leaseTime, TimeUnit.SECONDS);
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
                redissonClient2.getScoredSortedSet(key).addAll(objects);
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
                redissonClient2.getScoredSortedSet(key).add(score, value);
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
                redissonClient2.getScoredSortedSet(key).delete();
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
            redissonClient2.getScoredSortedSet(key).remove(object);
        }
    }

    public Boolean removeSetBatch(String key, Collection<String> collection) {
        boolean success = false;
        try {
            success = redissonClient.getSet(key).removeAll(collection);
            if (redisProperties.getCluster2().getActive()) {
                try {
                    redissonClient2.getSet(key).removeAll(collection);
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

    public Boolean isNotExistString2(String key) {
        long l = redissonClient2.getBucket(key).remainTimeToLive();

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
                log.error("redisson delKey 操作异常: ", e);
            }
            if (redisProperties.getCluster2().getActive()) {
                otherExecutor.execute(() -> {
                    try {
                        redissonClient2.getBucket(key).delete();
                    } catch (Exception e) {
                        log.error("异地redis delKey异常: {}", e.getMessage());
                    }
                });
            }
        } catch (Exception e) {
            log.error("redisson delKey 操作异常: ", e);
        }
        return success;
    }

    public Boolean delKey(String location, String key) {
        boolean success = false;
        try {
            RedissonClient redissonClient = REDISSON_CLIENT_MAP.get(location);
            success = redissonClient.getBucket(key).delete();
        } catch (Exception e) {
            log.error("redisson delSet 操作异常: ", e);
        }

        return success;
    }

    public Boolean delKey2(String key) {
        boolean success = false;
        try {
            success = redissonClient2.getBucket(key).delete();
        } catch (Exception e) {
            log.error("redisson delSet 操作异常: ", e);
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

    /**
     * 设置key 过期时间
     * 过期时间单位 s
     */
    public boolean setTTL(String key, long expire) {
        boolean bool = redissonClient.getBucket(key).expire(expire, TimeUnit.SECONDS);
        if (redisProperties.getCluster2().getActive()) {
            redissonClient2.getBucket(key).expire(expire, TimeUnit.SECONDS);
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

    public void setSetsBatch(List<String> keys, List<String> list) {
        RBatch batch = redissonClient.createBatch();
        for (String key : keys) {
            batch.getSet(key).addAllAsync(list);
        }
        batch.execute();
        if (redisProperties.getCluster2().getActive()) {
            RBatch batch2 = redissonClient2.createBatch();
            for (String key : keys) {
                batch2.getSet(key).addAllAsync(list);
            }
            batch2.execute();
        }
    }

    public void removeSetsBatch(List<String> keys, List<String> list) {
        RBatch batch = redissonClient.createBatch();
        for (String key : keys) {
            batch.getSet(key).removeAllAsync(list);
        }
        batch.execute();
        if (redisProperties.getCluster2().getActive()) {
            RBatch batch2 = redissonClient2.createBatch();
            for (String key : keys) {
                batch2.getSet(key).removeAllAsync(list);
            }
            batch2.execute();
        }
    }

    /**
     * hash 元素递增
     *
     * @param key        hash 键
     * @param item       项
     * @param delta      递增量
     * @param timeToLive 过期时间
     * @param timeUnit   单位
     * @return 结果
     */
    public Long addHashItem(String key, String item, Integer delta, long timeToLive, TimeUnit timeUnit) {
        RMap<String, Long> map = redissonClient.getMap(key);
        map.expire(timeToLive, timeUnit);
        return map.addAndGet(item, delta);
    }

    /**
     * hash 元素递增
     *
     * @param location   所属机房编码
     * @param key        hash 键
     * @param item       项
     * @param delta      递增量
     * @param timeToLive 过期时间
     * @param timeUnit   单位
     * @return 结果
     */
    public Long addHashItem(String location, String key, String item, long delta, long timeToLive, TimeUnit timeUnit) {
        RedissonClient redissonClient = REDISSON_CLIENT_MAP.get(location);
        RMap<String, Long> map = redissonClient.getMap(key);
        Long aLong = map.addAndGet(item, delta);
        map.expire(timeToLive, timeUnit);
        return aLong;
    }


}
