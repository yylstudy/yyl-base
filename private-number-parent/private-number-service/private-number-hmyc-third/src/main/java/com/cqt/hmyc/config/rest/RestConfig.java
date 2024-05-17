package com.cqt.hmyc.config.rest;

import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.OkHttp3ClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * @author linshiqiang
 * @date 2021/9/17 15:12
 */
@Configuration
public class RestConfig {

    @Bean
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setRequestFactory(clientHttpRequestFactory());
        return restTemplate;
    }

    @Bean
    public HttpClientConnectionManager poolingConnectionManager() {
        PoolingHttpClientConnectionManager poolingConnectionManager = new PoolingHttpClientConnectionManager();
        // 连接池最大连接数
        poolingConnectionManager.setMaxTotal(Runtime.getRuntime().availableProcessors() * 2);
        // 每个路由的最大连接数
        poolingConnectionManager.setDefaultMaxPerRoute(Runtime.getRuntime().availableProcessors() * 2);
        return poolingConnectionManager;
    }

    @Bean
    public HttpClientBuilder httpClientBuilder() {
        HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
        //设置HTTP连接管理器
        httpClientBuilder.setConnectionManager(poolingConnectionManager());
        return httpClientBuilder;
    }

    @Bean
    public ClientHttpRequestFactory clientHttpRequestFactory() {
        //比 HttpComponentsClientHttpRequestFactory 性能好上传一个10MB文件从10s-->400ms
        OkHttp3ClientHttpRequestFactory clientHttpRequestFactory = new OkHttp3ClientHttpRequestFactory();
        // 链接超时，毫秒
        clientHttpRequestFactory.setConnectTimeout(5000);
        // 读写超时，毫秒
        clientHttpRequestFactory.setWriteTimeout(10000);
        clientHttpRequestFactory.setReadTimeout(10000);
        return clientHttpRequestFactory;
    }


}
