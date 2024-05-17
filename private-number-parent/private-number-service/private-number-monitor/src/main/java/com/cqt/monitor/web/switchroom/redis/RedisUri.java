package com.cqt.monitor.web.switchroom.redis;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import redis.clients.jedis.HostAndPort;

import java.util.Set;

/**
 * Redis connection param
 *
 * @author Jay.H.Zou
 * @date 2019/7/18
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RedisUri {

    public static final int TIMEOUT = 5000;

    public static final int MAX_ATTEMPTS = 3;

    private Set<HostAndPort> hostAndPortSet;

    private String requirePass;

    private int database;

    private String clusterName;

}
