package com.cqt.monitor.web.switchroom.redis;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.exceptions.JedisConnectionException;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created with IntelliJ IDEA.
 * Description:
 *
 * @author linshiqiang
 * @date 2022-01-20 20:40
 */
@Slf4j
public class RedisClientFactory {

    public static RedisClient buildRedisClient(RedisUri uri) {
        RedisClient redisClient = new RedisClient(uri);
        if (redisClient.getJedisClient() == null) {
            throw new JedisConnectionException("All seed node can't connect.");
        }
        return redisClient;
    }

    public static synchronized List<RedisNode> getFailNode(RedisUri uri) throws Exception {

        if (uri == null) {
            return ListUtil.empty();
        }

        RedisClient redisClient = new RedisClient(uri);
        if (redisClient.getJedisClient() == null) {
            throw new JedisConnectionException("All seed node can't connect.");
        }

        List<RedisNode> redisNodes = redisClient.clusterNodes();
        List<RedisNode> nodeList = redisNodes.stream()
                .filter(item -> NodeRole.MASTER.equals(item.getNodeRole()))
                .filter(item -> !item.getRunStatus()).collect(Collectors.toList());
        if (CollUtil.isNotEmpty(nodeList)) {
            return nodeList;
        }
        return ListUtil.empty();
    }

}
