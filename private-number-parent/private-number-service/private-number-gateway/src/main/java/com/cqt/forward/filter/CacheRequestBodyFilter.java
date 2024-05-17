package com.cqt.forward.filter;

import com.cqt.common.constants.GatewayConstant;
import com.cqt.forward.util.GatewayUtil;
import com.cqt.model.common.properties.ForwardProperties;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.core.Ordered;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * @author linshiqiang
 * date 2021/9/16 16:58
 */
@Slf4j
@Component
@AllArgsConstructor
public class CacheRequestBodyFilter implements GlobalFilter, Ordered {

    private final GatewayUtil gatewayUtil;

    private final ServerCodecConfigurer codecConfigurer;

    private final ForwardProperties forwardProperties;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        exchange.getAttributes().put(GatewayConstant.CACHED_REQUEST_PATH_KEY, request.getPath().value());
        boolean passFilter = gatewayUtil.passFilter(request);
        if (passFilter) {
            return chain.filter(exchange);
        }
        return ServerWebExchangeUtils.cacheRequestBodyAndRequest(exchange, (serverHttpRequest) ->
                ServerRequest.create(exchange.mutate().request(serverHttpRequest).build(), codecConfigurer.getReaders())
                        .bodyToMono(String.class)
                        .defaultIfEmpty("")
                        .doOnNext(objectValue -> {
                            // objectValue 即为请求Body内容
                            exchange.getAttributes().put(GatewayConstant.CACHED_REQUEST_BODY_OBJECT_KEY, objectValue);
                            if (forwardProperties.getEnableLog()) {
                                log.info("来源IP: {}, url: {}, 参数: {}", gatewayUtil.getGatewayIpAddress(request),
                                        gatewayUtil.getRequestPath(exchange),
                                        objectValue);
                            }
                        })
                        .flatMap(bodyString -> {
                            if (serverHttpRequest == exchange.getRequest()) {
                                return chain.filter(exchange);
                            }
                            // ServerWebExchangeUtils.cacheRequestBodyAndRequest中已经缓存了可重复读的request
                            // 并且request也已经转换成了可重复读Body的request
                            ServerHttpRequest cacheRequest =
                                    (ServerHttpRequest) exchange.getAttributes()
                                            .get(ServerWebExchangeUtils.CACHED_SERVER_HTTP_REQUEST_DECORATOR_ATTR);
                            return chain.filter(exchange.mutate().request(cacheRequest).build());
                        })
        );
    }

    @Override
    public int getOrder() {
        return -100;
    }
}
