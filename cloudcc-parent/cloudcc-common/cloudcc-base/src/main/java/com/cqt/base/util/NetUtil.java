package com.cqt.base.util;

import org.apache.dubbo.common.utils.NetUtils;
import org.springframework.util.StringUtils;

/**
 * @author linshiqiang
 * date:  2023-08-07 17:28
 */
public class NetUtil {

    private static final String DUBBO_IP_TO_BIND = "DUBBO_IP_TO_BIND";

    private static final String DUBBO_IP_TO_REGISTRY = "DUBBO_IP_TO_REGISTRY";

    public static String getLocalIp() {
        String registerIp = getSystemProperty(DUBBO_IP_TO_REGISTRY);
        if (StringUtils.hasLength(registerIp)) {
            return registerIp;
        }
        String bindIp = getSystemProperty(DUBBO_IP_TO_BIND);
        if (StringUtils.hasLength(bindIp)) {
            return bindIp;
        }
        return NetUtils.getLocalHost();
    }

    /**
     * System environment -> System properties
     *
     * @param key key
     * @return value
     */
    public static String getSystemProperty(String key) {
        String value = System.getenv(key);
        if (StringUtils.hasLength(value)) {
            return value;
        }
        return System.getProperty(key);
    }
}
