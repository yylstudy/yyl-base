package com.linkcircle.redis.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;

/**
 * @author yang.yonglian
 * @version 1.0.0
 * @Description TODO
 * @createTime 2023/4/18 11:32
 */
@Data
@ConfigurationProperties(prefix = "spring.redis")
@Component
public class MultiRedisProperties {
    /**
     * Login password of the redis server.
     */
    private String password;
    /**
     * Read timeout.
     */
    private Duration timeout;
    /**
     * Connection timeout.
     */
    private Duration connectTimeout;

    private Cluster cluster;

    private Cluster2 cluster2;

    private final Jedis jedis = new Jedis();

    /**
     * Cluster properties.
     */
    @Data
    public static class Cluster {
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
    }
    @Data
    public static class Cluster2 extends Cluster{
        /**
         * 是否启用
         */
        private boolean active;
    }
    /**
     * Jedis client properties.
     */
    public static class Jedis {

        /**
         * Jedis pool configuration.
         */
        private final Pool pool = new Pool();

        public Pool getPool() {
            return this.pool;
        }

    }
    /**
     * Pool properties.
     */
    public static class Pool {

        /**
         * Whether to enable the pool. Enabled automatically if "commons-pool2" is
         * available. With Jedis, pooling is implicitly enabled in sentinel mode and this
         * setting only applies to single node setup.
         */
        private Boolean enabled;

        /**
         * Maximum number of "idle" connections in the pool. Use a negative value to
         * indicate an unlimited number of idle connections.
         */
        private int maxIdle = 8;

        /**
         * Target for the minimum number of idle connections to maintain in the pool. This
         * setting only has an effect if both it and time between eviction runs are
         * positive.
         */
        private int minIdle = 0;

        /**
         * Maximum number of connections that can be allocated by the pool at a given
         * time. Use a negative value for no limit.
         */
        private int maxActive = 8;

        /**
         * Maximum amount of time a connection allocation should block before throwing an
         * exception when the pool is exhausted. Use a negative value to block
         * indefinitely.
         */
        private Duration maxWait = Duration.ofMillis(-1);

        /**
         * Time between runs of the idle object evictor thread. When positive, the idle
         * object evictor thread starts, otherwise no idle object eviction is performed.
         */
        private Duration timeBetweenEvictionRuns;

        public Boolean getEnabled() {
            return this.enabled;
        }

        public void setEnabled(Boolean enabled) {
            this.enabled = enabled;
        }

        public int getMaxIdle() {
            return this.maxIdle;
        }

        public void setMaxIdle(int maxIdle) {
            this.maxIdle = maxIdle;
        }

        public int getMinIdle() {
            return this.minIdle;
        }

        public void setMinIdle(int minIdle) {
            this.minIdle = minIdle;
        }

        public int getMaxActive() {
            return this.maxActive;
        }

        public void setMaxActive(int maxActive) {
            this.maxActive = maxActive;
        }

        public Duration getMaxWait() {
            return this.maxWait;
        }

        public void setMaxWait(Duration maxWait) {
            this.maxWait = maxWait;
        }

        public Duration getTimeBetweenEvictionRuns() {
            return this.timeBetweenEvictionRuns;
        }

        public void setTimeBetweenEvictionRuns(Duration timeBetweenEvictionRuns) {
            this.timeBetweenEvictionRuns = timeBetweenEvictionRuns;
        }

    }
}
