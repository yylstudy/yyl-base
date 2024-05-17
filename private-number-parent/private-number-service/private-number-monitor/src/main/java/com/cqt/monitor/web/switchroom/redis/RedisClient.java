package com.cqt.monitor.web.switchroom.redis;

import com.cqt.monitor.common.util.SignUtil;
import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisConnectionException;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * Description:
 *
 * @author linshiqiang
 * @since 2022-01-20 20:35
 */
@Slf4j
public class RedisClient {

    public static final int TIMEOUT = 5000;

    private static final String OK = "OK";

    private static final String PONG = "PONG";

    private Jedis jedis;

    public RedisClient(RedisUri uri) {
        String redisPassword = uri.getRequirePass();
        Set<HostAndPort> hostAndPortSet = uri.getHostAndPortSet();
        for (HostAndPort hostAndPort : hostAndPortSet) {
            try {
                jedis = new Jedis(hostAndPort.getHost(), hostAndPort.getPort(), TIMEOUT, TIMEOUT);
                if (!Strings.isNullOrEmpty(redisPassword)) {
                    jedis.auth(redisPassword);
                }
                if (ping()) {
                    break;
                }
            } catch (JedisConnectionException e) {
                // try next nodes
                close();
            }
        }
    }

    public Jedis getJedisClient() {
        return jedis;
    }

    public List<RedisNode> clusterNodes() throws Exception {
        String nodes = jedis.clusterNodes();
        if (nodes.contains(RedisNode.DISCONNECTED) || nodes.contains("fail")) {
            log.error("redis集群节点disconnected: {}", nodes);
        }
        close();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(nodes.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8));
        String line;
        List<RedisNode> redisNodeList = new ArrayList<>();
        while ((line = bufferedReader.readLine()) != null) {
            String[] item = SignUtil.splitBySpace(line);
            String nodeId = item[0].trim();
            String ipPort = item[1];
            // noaddr 可知有此标记的节点属于无用节点
            if (line.contains("noaddr")) {
                log.warn("find a useless node: {}", line);
                continue;
            }
            Set<HostAndPort> hostAndPortSet = RedisUtil.nodesToHostAndPortSet(SignUtil.splitByAite(ipPort)[0]);
            HostAndPort hostAndPort = hostAndPortSet.iterator().next();
            String flags = item[2];
            String masterId = item[3];
            String linkState = item[7];
            RedisNode redisNode = new RedisNode(nodeId, hostAndPort.getHost(), hostAndPort.getPort(), null);
            if (flags.contains("myself")) {
                flags = flags.substring(7);
            }
            redisNode.setFlags(flags);
            redisNode.setMasterId(masterId);
            redisNode.setLinkState(linkState);
            redisNode.setRunStatus(RedisNode.CONNECTED.equals(linkState));
            int length = item.length;
            if (length > 8) {
                int slotNumber = 0;
                StringBuilder slotRang = new StringBuilder();
                for (int i = 8; i < length; i++) {
                    String slotRangeItem = item[i];
                    String[] startAndEnd = SignUtil.splitByMinus(slotRangeItem);
                    if (startAndEnd.length == 1) {
                        slotNumber += 1;
                    } else {
                        slotNumber += Integer.parseInt(startAndEnd[1]) - Integer.parseInt(startAndEnd[0]) + 1;
                    }
                    slotRang.append(slotRangeItem);
                    if (i < length - 1) {
                        slotRang.append(SignUtil.COMMAS);
                    }
                }
                redisNode.setSlotRange(slotRang.toString());
                redisNode.setSlotNumber(slotNumber);
            }
            if (flags.contains(NodeRole.MASTER.getValue())) {
                redisNode.setNodeRole(NodeRole.MASTER);
            } else if (flags.contains(NodeRole.SLAVE.getValue())) {
                redisNode.setNodeRole(NodeRole.SLAVE);
            } else {
                redisNode.setNodeRole(NodeRole.UNKNOWN);
            }
            redisNodeList.add(redisNode);
        }
        return redisNodeList;
    }

    public boolean ping() {
        String ping = jedis.ping();
        return !Strings.isNullOrEmpty(ping) && Objects.equals(ping.toUpperCase(), PONG);
    }

    public void close() {
        try {
            if (jedis != null) {
                jedis.close();
            }
        } catch (Exception e) {
            log.error("jedis.close error: ", e);
        }
    }


}
