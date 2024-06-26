package com.cqt.forward.config;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.protocol.HttpContext;
import org.springframework.http.HttpMethod;

import java.net.URI;
import java.util.function.BiFunction;

/**
 * Created with IntelliJ IDEA.
 * Description:
 *
 * @author linshiqiang
 * @date 2022-03-23 23:10
 */
public class CustomHttpContextFactory implements BiFunction<HttpMethod, URI, HttpContext> {
    @Override
    public HttpContext apply(HttpMethod httpMethod, URI uri) {
        RequestConfig requestConfig = RequestConfigHolder.get();
        if (requestConfig != null) {
            HttpContext context = HttpClientContext.create();
            context.setAttribute(HttpClientContext.REQUEST_CONFIG, requestConfig);
            return context;
        }
        return null;
    }
}