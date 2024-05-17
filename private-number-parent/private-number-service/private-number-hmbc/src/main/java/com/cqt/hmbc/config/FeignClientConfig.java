package com.cqt.hmbc.config;

import cn.hutool.core.util.StrUtil;
import com.cqt.common.constants.SystemConstant;
import feign.Client;
import feign.Request;
import feign.Response;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.cloud.netflix.ribbon.SpringClientFactory;
import org.springframework.cloud.openfeign.ribbon.CachingSpringLoadBalancerFactory;
import org.springframework.cloud.openfeign.ribbon.LoadBalancerFeignClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;

/**
 * FeignClientConfig
 *
 * @author Xienx
 * @date 2023年02月14日 16:44
 */
@Slf4j
@Configuration(proxyBeanMethods = false)
public class FeignClientConfig implements SystemConstant {

    static class FeignIpClient extends Client.Default {

        public FeignIpClient(SSLSocketFactory sslContextFactory, HostnameVerifier hostnameVerifier) {
            super(sslContextFactory, hostnameVerifier);
        }

        @Override
        public Response execute(Request request, Request.Options options) throws IOException {
            MDC.put(REMOTE_IP, determineClientIp(request.url()));
            //打印目标节点IP
            log.info("[{}] -> request to remote instance [{}]", MDC.get(REQUEST_ID), MDC.get(REMOTE_IP));
            return super.execute(request, options);
        }


        private String determineClientIp(String url) {
            return StrUtil.isEmpty(url) ? url : url.substring(url.indexOf("/") + 2, url.lastIndexOf(":"));
        }
    }


    /**
     * 自定义feign.Client
     */
    @Bean
    public Client client(CachingSpringLoadBalancerFactory cachingFactory,
                         SpringClientFactory clientFactory) {
        return new LoadBalancerFeignClient(new FeignIpClient(null, null), cachingFactory, clientFactory);
    }

}
