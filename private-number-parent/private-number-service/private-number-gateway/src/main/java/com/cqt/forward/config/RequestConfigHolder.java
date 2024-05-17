package com.cqt.forward.config;

import org.apache.http.client.config.RequestConfig;

/**
 * Created with IntelliJ IDEA.
 * Description:
 *
 * @author linshiqiang
 * @date 2022-03-23 23:11
 */
public class RequestConfigHolder {

    private static final ThreadLocal<RequestConfig> THREAD_LOCAL = new ThreadLocal<>();

    public static void bind(RequestConfig requestConfig) {
        THREAD_LOCAL.set(requestConfig);
    }

    public static RequestConfig get() {
        return THREAD_LOCAL.get();
    }

    public static void clear() {
        THREAD_LOCAL.remove();
    }
}