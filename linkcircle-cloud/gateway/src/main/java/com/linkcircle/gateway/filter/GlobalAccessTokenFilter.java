package com.linkcircle.gateway.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.linkcircle.gateway.common.GlobalConstants;
import com.linkcircle.gateway.common.Result;
import com.linkcircle.gateway.config.ExcludePath;
import io.netty.channel.ChannelOption;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;

/**
 * @author yang.yonglian
 * @version 1.0.0
 * @Description token调研器
 * @createTime 2023/4/20 15:32
 */
@Component
@Slf4j
public class GlobalAccessTokenFilter implements GlobalFilter, Ordered {
    private PathMatcher pathMatcher = new AntPathMatcher();
    private static ObjectMapper mapper = new ObjectMapper();
    @Autowired
    private ExcludePath excludePath;
    /**
     * webclient的底层httpClient，主要设置超时时间
     */
    private HttpClient httpClient = HttpClient.create().option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 3000)
            .responseTimeout(Duration.ofSeconds(5));
    @Autowired
    private WebClient.Builder webClientBuilder;
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String url = exchange.getRequest().getURI().getPath();
        ServerHttpResponse resp = exchange.getResponse();
        for(String excludeUrl: excludePath.getAllPathList()){
            if(pathMatcher.match(excludeUrl,url)){
                return chain.filter(exchange);
            }
        }
        String token = exchange.getRequest().getHeaders().getFirst(GlobalConstants.X_ACCESS_TOKEN);
        if(!StringUtils.hasText(token)){
            return getVoidMono(resp,Result.errorAuth(GlobalConstants.TOKEN_IS_EMPTY));
        }
        Mono<Void> mono = webClientBuilder.baseUrl(GlobalConstants.TOKEN_SERVER_NAME)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build().get().uri(uriBuilder ->
                        uriBuilder.path(GlobalConstants.TOKEN_VERIFY_URL)
                        .queryParam(GlobalConstants.TOKEN_PARAM, token).build())
                .retrieve().bodyToMono(Result.class)
//                .doOnSuccess(match->log.info("token校验结果:{}",match))
                .doOnError(error->{
                    throw new RuntimeException("token校验失败");
                }).flatMap(result->{
                    if(!result.isSuccess()){
                        return getVoidMono(exchange.getResponse(),result);
                    }
                    return chain.filter(exchange);
                });
        return mono;
    }

    private Mono<Void> getVoidMono(ServerHttpResponse serverHttpResponse, Result result) {
        try{
            serverHttpResponse.getHeaders().add("Character-Encoding", "UTF-8");
            serverHttpResponse.getHeaders().add("Content-Type", "application/json;charset=UTF-8");
            DataBuffer dataBuffer = serverHttpResponse.bufferFactory().wrap(mapper.writeValueAsString(result).getBytes());
            return serverHttpResponse.writeWith(Flux.just(dataBuffer));
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }
    @Override
    public int getOrder() {
        return 0;
    }

}
