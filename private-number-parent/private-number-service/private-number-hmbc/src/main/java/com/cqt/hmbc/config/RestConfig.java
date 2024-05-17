package com.cqt.hmbc.config;

import lombok.RequiredArgsConstructor;
import okhttp3.ConnectionPool;
import okhttp3.Dispatcher;
import okhttp3.OkHttpClient;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.openfeign.support.FeignHttpClientProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.OkHttp3ClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.TimeUnit;

/**
 * ribbon负载
 *
 * @author Xienx
 */
@RequiredArgsConstructor
@Configuration(proxyBeanMethods = false)
public class RestConfig {
    private final FeignHttpClientProperties feignHttpClientProperties;

    @Bean
    @RefreshScope
    public RestTemplate restTemplate() {
        OkHttpClient httpClient = okHttpClient();
        ClientHttpRequestFactory factory = new OkHttp3ClientHttpRequestFactory(httpClient);
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setRequestFactory(factory);
        return new RestTemplate();
    }

    /**
     * 声明restTemplate拥有 ribbon负载
     */
    @Bean
    @LoadBalanced
    @RefreshScope
    public RestTemplate lbRestTemplate() {
        OkHttpClient httpClient = okHttpClient();
        ClientHttpRequestFactory factory = new OkHttp3ClientHttpRequestFactory(httpClient);
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setRequestFactory(factory);
        return new RestTemplate();
    }


    private OkHttpClient okHttpClient() {
        Dispatcher dispatcher = new Dispatcher();
        dispatcher.setMaxRequests(feignHttpClientProperties.getMaxConnections());
        dispatcher.setMaxRequestsPerHost(feignHttpClientProperties.getMaxConnectionsPerRoute());

        return new OkHttpClient.Builder()
                .retryOnConnectionFailure(false)
                .connectionPool(pool())
                .connectTimeout(feignHttpClientProperties.getConnectionTimeout(), TimeUnit.MILLISECONDS)
                .readTimeout(feignHttpClientProperties.getConnectionTimeout(), TimeUnit.MILLISECONDS)
                .writeTimeout(feignHttpClientProperties.getConnectionTimeout(), TimeUnit.MILLISECONDS)
                .dispatcher(dispatcher)
                .build();
    }

    private ConnectionPool pool() {
        return new ConnectionPool(feignHttpClientProperties.getMaxConnections(),
                feignHttpClientProperties.getTimeToLive(),
                feignHttpClientProperties.getTimeToLiveUnit());
    }
}
