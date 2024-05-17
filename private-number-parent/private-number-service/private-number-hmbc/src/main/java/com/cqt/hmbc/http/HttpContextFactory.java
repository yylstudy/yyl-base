package com.cqt.hmbc.http;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.protocol.HttpContext;
import org.springframework.http.HttpMethod;

import java.net.URI;
import java.util.Objects;
import java.util.function.BiFunction;

/**
 * HttpContextFactory
 *
 * @author Xienx
 * @date 2023年02月23日 10:46
 */
public class HttpContextFactory implements BiFunction<HttpMethod, URI, HttpContext> {

    @Override
    public HttpContext apply(HttpMethod httpMethod, URI uri) {
        RequestConfig requestConfig = HttpContextHolder.peek();
        if (Objects.nonNull(requestConfig)) {
            HttpContext context = HttpClientContext.create();
            context.setAttribute(HttpClientContext.REQUEST_CONFIG, requestConfig);
            return context;
        }
        return null;
    }
}
