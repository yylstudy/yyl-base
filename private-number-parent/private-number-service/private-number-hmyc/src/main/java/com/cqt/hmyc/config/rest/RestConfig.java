package com.cqt.hmyc.config.rest;

import okhttp3.ConnectionPool;
import okhttp3.Dispatcher;
import okhttp3.OkHttpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.OkHttp3ClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.TimeUnit;

/**
 * @author linshiqiang
 * @date 2021/9/17 15:12
 */
@Configuration
public class RestConfig {
    @Bean
    public RestTemplate restTemplate() {
        OkHttpClient httpClient = okHttpClient();
        ClientHttpRequestFactory factory = new OkHttp3ClientHttpRequestFactory(httpClient);
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setRequestFactory(factory);
        return restTemplate;
    }

    @Bean
    public ConnectionPool pool() {
        return new ConnectionPool(500, 5, TimeUnit.MINUTES);
    }

    @Bean
    public OkHttpClient okHttpClient() {
        Dispatcher dispatcher = new Dispatcher();
        dispatcher.setMaxRequests(2000);
        dispatcher.setMaxRequestsPerHost(500);

        return new OkHttpClient.Builder()
                .retryOnConnectionFailure(false)
                .connectionPool(pool())
                .connectTimeout(10000, TimeUnit.MILLISECONDS)
                .readTimeout(10000, TimeUnit.MILLISECONDS)
                .writeTimeout(10000, TimeUnit.MILLISECONDS)
                .dispatcher(dispatcher)
                .build();
    }


}
