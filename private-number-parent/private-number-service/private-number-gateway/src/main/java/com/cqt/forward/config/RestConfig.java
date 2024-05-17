package com.cqt.forward.config;

import com.cqt.model.common.properties.ForwardProperties;
import okhttp3.ConnectionPool;
import okhttp3.Dispatcher;
import okhttp3.OkHttpClient;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
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
@EnableConfigurationProperties(ForwardProperties.class)
@Configuration
public class RestConfig {

    @Bean
    public RestTemplate restTemplate(ForwardProperties forwardProperties) {
        OkHttpClient httpClient = okHttpClient(forwardProperties);
        ClientHttpRequestFactory factory = new OkHttp3ClientHttpRequestFactory(httpClient);
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setRequestFactory(factory);
        return restTemplate;
    }

    @Bean
    public ConnectionPool pool(ForwardProperties forwardProperties) {
        ForwardProperties.ConnectionPoolConfig connectionPoolConfig = forwardProperties.getConnectionPoolConfig();

        return new ConnectionPool(connectionPoolConfig.getMaxIdleConnections(), connectionPoolConfig.getKeepAliveDuration(), TimeUnit.MINUTES);
    }

    @Bean
    public OkHttpClient okHttpClient(ForwardProperties forwardProperties) {
        ForwardProperties.ConnectionPoolConfig connectionPoolConfig = forwardProperties.getConnectionPoolConfig();
        Dispatcher dispatcher = new Dispatcher();
        dispatcher.setMaxRequests(connectionPoolConfig.getMaxRequests());
        dispatcher.setMaxRequestsPerHost(connectionPoolConfig.getMaxRequestsPerHost());

        return new OkHttpClient.Builder()
                .retryOnConnectionFailure(false)
                .connectionPool(pool(forwardProperties))
                .connectTimeout(connectionPoolConfig.getConnectTimeout(), TimeUnit.MILLISECONDS)
                .readTimeout(connectionPoolConfig.getReadTimeout(), TimeUnit.MILLISECONDS)
                .writeTimeout(connectionPoolConfig.getWriteTimeout(), TimeUnit.MILLISECONDS)
                .dispatcher(dispatcher)
                .build();
    }


}
