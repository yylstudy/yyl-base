package com.linkcircle.gateway.config;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.reactive.result.view.ViewResolver;

import java.util.stream.Collectors;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;
import static org.springframework.web.reactive.function.server.ServerResponse.ok;

/**
 * @author yang.yonglian
 * @version 1.0.0
 * @Description TODO
 * @createTime 2023/4/20 15:42
 */
@Configuration
public class GatewayConfig {
    /**
     * 用于在gateway上远程调用，在Spring Cloud 2020版本后，spring cloud gateway集成openfeign会卡住无法启动
     * 详细查看
     * https://github.com/spring-cloud/spring-cloud-gateway/issues/2126
     * https://github.com/spring-cloud/spring-cloud-gateway/issues/1090
     * 解决方法官方给出的通过WebClient去调用
     * @return
     */
    @Bean
    @LoadBalanced
    public WebClient.Builder webBuilder(){
        return WebClient.builder();
    }

    @Bean
    @Order(-1)
    public ErrorWebExceptionHandler errorWebExceptionHandler(ServerProperties serverProperties, ErrorAttributes errorAttributes,
                                                             WebProperties webProperties, ObjectProvider<ViewResolver> viewResolvers,
                                                             ServerCodecConfigurer serverCodecConfigurer, ApplicationContext applicationContext) {
        GlobalErrorWebExceptionHandler exceptionHandler = new GlobalErrorWebExceptionHandler(errorAttributes,
                webProperties.getResources(), serverProperties.getError(), applicationContext);
        exceptionHandler.setViewResolvers(viewResolvers.orderedStream().collect(Collectors.toList()));
        exceptionHandler.setMessageWriters(serverCodecConfigurer.getWriters());
        exceptionHandler.setMessageReaders(serverCodecConfigurer.getReaders());
        return exceptionHandler;
    }
    @Bean
    public GlobalErrorAttributes globalErrorAttributes(){
        return new GlobalErrorAttributes();
    }

    /**
     * 接口地址（通过9999端口直接访问）
     *
     * @param indexHtml
     * @return
     */
    @Bean
    public RouterFunction<ServerResponse> indexRouter(@Value("classpath:/META-INF/resources/doc.html") Resource indexHtml) {
        return route(GET("/"), request -> ok().contentType(MediaType.TEXT_HTML).bodyValue(indexHtml));
    }
}
