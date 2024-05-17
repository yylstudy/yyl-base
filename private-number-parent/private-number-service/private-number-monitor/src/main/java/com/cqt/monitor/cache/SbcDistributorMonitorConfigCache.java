package com.cqt.monitor.cache;

import com.cqt.common.util.CopyOnWriteMap;
import com.cqt.monitor.web.distributor.SbcDistributorMonitorConfig;
import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;

/**
 * @author linshiqiang
 * @since 2022-12-02 9:43
 * SBC dis组监控信息缓存
 */
@Slf4j
public class SbcDistributorMonitorConfigCache {

    private static final Map<String, SbcDistributorMonitorConfig> CACHE = new CopyOnWriteMap<>();

    private static String SERVER_IP = "";

    static {
        try {
            SERVER_IP = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            log.error("获取本机ip失败: ", e);
        }
    }

    private static final String CACHE_NAME = "monitor";

    public static String getServerIp() {

        return SERVER_IP;
    }

    public static SbcDistributorMonitorConfig get() {

        return CACHE.get(CACHE_NAME);
    }

    public static void put(SbcDistributorMonitorConfig config) {

        CACHE.put(CACHE_NAME, config);
    }

    public static String getGroup() {

        return CACHE.get(CACHE_NAME).getDingtalkGroup();
    }

    public static Integer getTimeout() {

        return CACHE.get(CACHE_NAME).getTimeout();
    }

    public static Integer getMaxRetry() {

        return CACHE.get(CACHE_NAME).getMaxRetry();
    }

    public static Integer getMergeCount() {

        return CACHE.get(CACHE_NAME).getMergeCount();
    }
}
