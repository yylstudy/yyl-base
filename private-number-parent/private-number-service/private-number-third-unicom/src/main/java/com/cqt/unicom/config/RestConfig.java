package com.cqt.unicom.config;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * @author zhengsuhao
 * @date 2022/12/5
 */
@Api(tags="初始化配置")
@Configuration
public class RestConfig {

    @ApiOperation("初始化url连接")
    @Bean
    @LoadBalanced
    public RestTemplate restTemplate() {
        //设置连接超时时长5秒
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(5000);
        requestFactory.setReadTimeout(5000);
        return new RestTemplate(requestFactory);
    }


}
