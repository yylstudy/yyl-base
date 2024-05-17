package com.cqt.forward.circuit;

import com.cqt.common.constants.GatewayConstant;
import com.cqt.forward.handler.RemoteHandler;
import com.cqt.forward.util.GatewayUtil;
import com.cqt.model.common.Result;
import com.google.common.collect.Maps;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 响应超时熔断处理器
 *
 * @author zyf
 */
@RestController
@Slf4j
@RequiredArgsConstructor
public class FallbackController {

    private final MyReactiveResilience4JCircuitBreakerFactory circuitBreakerFactory;

    private final CircuitBreakerRegistry circuitBreakerRegistry;

    private final RemoteHandler remoteHandler;

    private final GatewayUtil gatewayUtil;

    /**
     * 全局熔断处理
     */
    @RequestMapping("/fallback")
    public Mono<Void> fallback(ServerWebExchange exchange) {
        String path = gatewayUtil.getStringAttribute(exchange, GatewayConstant.CACHED_REQUEST_PATH_KEY);
        Optional<Exception> exceptionOptional = gatewayUtil.getCircuitBreakerExecutionException(exchange);
        if (exceptionOptional.isPresent()) {
            Exception exception = exceptionOptional.get();
            log.error("本地熔断器开启, 进入fallback熔断, 请求地址: {}, 请求参数: {}, 异常信息: ", path, gatewayUtil.getRequestBody(exchange), exception);
        }
        ServerHttpRequest request = exchange.getRequest();
        HttpMethod method = request.getMethod();
        Route route = gatewayUtil.getGatewayRoute(exchange);
        Optional<String> optional = remoteHandler.getServer(path, route.getId());
        if (!optional.isPresent()) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "异地未找到可用的服务");
        }
        String requestPath = optional.get();

        String requestJson = gatewayUtil.getRequestBody(exchange);
        // 发起请求
        // 如果异地请求还异常, 直接报错!
        gatewayUtil.setAttribute(exchange, GatewayConstant.CACHED_FORWARD_URL_KEY, requestPath);
        Result result = remoteHandler.remoteRequest(exchange, Maps.newHashMap(), requestJson, requestPath, method);
        log.info("本机房进入熔断, 调用异地机房接口: {}, 完成", requestPath);
        gatewayUtil.setAttribute(exchange, GatewayConstant.CACHED_FORWARD_URL_KEY, requestPath);
        exchange.getAttribute(ServerWebExchangeUtils.CLIENT_RESPONSE_ATTR);
        return gatewayUtil.responseData(exchange, result);
    }

    @RequestMapping("/getCircuitBreaker")
    private Map<String, Object> getCircuitBreaker() {
        CircuitBreaker circuitBreaker = circuitBreakerFactory.getCircuitBreakerRegistry().getAllCircuitBreakers().filter(breaker ->
                        "myCircuitBreaker".equals(breaker.getName()))
                .getOrNull();
        Map<String, Object> data = new HashMap<>(16);
        if (circuitBreaker != null) {
            data.put("State", circuitBreaker.getState());
            data.put("Metrics", circuitBreaker.getMetrics());
        }
        return data;
    }

    @RequestMapping("/getOtherCircuitBreaker")
    private Map<String, Object> getOtherCircuitBreaker() {

        Map<String, Object> data = new HashMap<>(16);
        CircuitBreaker circuitBreaker = circuitBreakerRegistry.getAllCircuitBreakers().filter(breaker ->
                        "otherRoom".equals(breaker.getName()))
                .getOrNull();
        if (circuitBreaker != null) {
            data.put("State", circuitBreaker.getState());
            data.put("Metrics", circuitBreaker.getMetrics());
        }
        return data;
    }

}
