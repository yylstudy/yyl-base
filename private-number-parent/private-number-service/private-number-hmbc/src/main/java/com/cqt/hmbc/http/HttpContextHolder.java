package com.cqt.hmbc.http;

import org.apache.http.client.config.RequestConfig;

/**
 * HTTP进程执行状态上下文
 *
 * @author Xienx
 * @date 2023年02月23日 10:47
 */
public class HttpContextHolder {

    private static final ThreadLocal<RequestConfig> THREAD_LOCAL = new ThreadLocal<>();

    public static void bind(RequestConfig requestConfig) {
        THREAD_LOCAL.set(requestConfig);
    }

    public static RequestConfig peek() {
        return THREAD_LOCAL.get();
    }

    public static void clear() {
        THREAD_LOCAL.remove();
    }
}
