package com.cqt.model.common.properties;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.List;

/**
 * @author linshiqiang
 * @date 2021/8/26 10:15
 */
@Data
@Component
@JsonIgnoreProperties(ignoreUnknown = true)
@ConfigurationProperties(prefix = "spring.redis")
public class MultiRedisProperties implements Serializable {

    /**
     * Login password of the redis server.
     */
    private String password;

    /**
     * Connection timeout.
     */
    private Integer timeout;

    /**
     * 本地集群
     */
    private Cluster cluster;

    /**
     * 异地集群
     */
    private Cluster2 cluster2;

    private Integer masterConnectionPoolSize;

    /**
     * jedis 连接池
     */
    private Jedis jedis;

    @Data
    public static class Cluster implements Serializable {

        /**
         * Comma-separated list of "host:port" pairs to bootstrap from. This represents an
         * "initial" list of cluster nodes and is required to have at least one entry.
         */
        private List<String> nodes;

        /**
         * Maximum number of redirects to follow when executing commands across the
         * cluster.
         */
        private Integer maxRedirects;

        /**
         * 是否激活
         */
        private Boolean active;

        /**
         * 机房位置 A/B
         */
        private String location;

    }

    @Data
    public static class Cluster2 implements Serializable {

        /**
         * Comma-separated list of "host:port" pairs to bootstrap from. This represents an
         * "initial" list of cluster nodes and is required to have at least one entry.
         */
        private List<String> nodes;

        /**
         * Maximum number of redirects to follow when executing commands across the
         * cluster.
         */
        private Integer maxRedirects;

        /**
         * 是否激活
         */
        private Boolean active;

        /**
         * 机房位置 A/B
         */
        private String location;

    }


    @Data
    public static class Jedis {

        /**
         * Jedis pool configuration.
         */
        private Pool pool;

    }

    @Data
    public static class Pool {

        private int maxIdle = 50;

        private int minIdle = 10;

        private int maxActive = 200;

        private long maxWait = -1;

        private long timeBetweenEvictionRuns = -1;
    }

}
