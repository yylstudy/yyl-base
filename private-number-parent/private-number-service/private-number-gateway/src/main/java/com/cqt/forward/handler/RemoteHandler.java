package com.cqt.forward.handler;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.StrUtil;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.cqt.common.constants.GatewayConstant;
import com.cqt.forward.balancer.RoundRobinLoadBalancer;
import com.cqt.forward.cache.LocalCache;
import com.cqt.forward.config.RequestConfigHolder;
import com.cqt.forward.util.GatewayUtil;
import com.cqt.model.common.Result;
import com.cqt.model.common.properties.ForwardProperties;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.config.RequestConfig;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.support.DelegatingServiceInstance;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ServerWebExchange;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author linshiqiang
 * @date 2022/1/18 12:51
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RemoteHandler {

    private final RestTemplate restTemplate;

    private final ForwardProperties forwardProperties;

    private final RoundRobinLoadBalancer roundRobinLoadBalancer;

    private final GatewayUtil gatewayUtil;

    private final LoadBalancerClient loadBalancer;

    @CircuitBreaker(name = "otherRoom", fallbackMethod = "otherRoomFallBack")
    public Result remoteRequestBreak(ServerWebExchange exchange, Map<String, String> paramMap, String requestJson, String url, HttpMethod method) {
        return remoteRequest(exchange, paramMap, requestJson, url, method);
    }

    /**
     * 异地熔断器回调
     *
     * @param exchange    exchange
     * @param paramMap    get参数
     * @param requestJson 请求体
     * @param url         源url
     * @param method      请求类型
     * @param e           异常信息
     * @return Result
     */
    public Result otherRoomFallBack(ServerWebExchange exchange, Map<String, String> paramMap, String requestJson, String url, HttpMethod method, Throwable e) {
        URI uri = exchange.getRequest().getURI();
        Route route = gatewayUtil.getGatewayRoute(exchange);
        ServiceInstance instance = loadBalancer.choose(route.getId());
        String overrideScheme = instance.isSecure() ? "https" : "http";
        URI requestUrl = loadBalancer.reconstructURI(new DelegatingServiceInstance(instance, overrideScheme), uri);
        log.error("异地熔断器开启, 异地接口: {}, 请求体: {}, 调本地接口: {}", url, gatewayUtil.getRequestBody(exchange), requestUrl.toString(), e);
        gatewayUtil.setAttribute(exchange, GatewayConstant.CACHED_FORWARD_URL_KEY, requestUrl.toString());
        return remoteRequest(exchange, paramMap, requestJson, requestUrl.toString(), method);
    }

    /**
     * 发起远程http调用
     *
     * @param paramMap    get请求参数 form
     * @param requestJson post put请求体
     * @param url         异地url
     * @param method      请求类型 get post
     * @return Result
     */
    public Result remoteRequest(ServerWebExchange exchange, Map<String, String> paramMap, String requestJson, String url, HttpMethod method) {
        try {
            RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(forwardProperties.getHttpTimeout()).build();
            RequestConfigHolder.bind(requestConfig);
            if (HttpMethod.GET.equals(method)) {
                return restTemplate.getForObject(url, Result.class, paramMap);
            }
            HttpHeaders headers = new HttpHeaders();
            MediaType type = MediaType.parseMediaType("application/json; charset=UTF-8");
            headers.setContentType(type);
            headers.add("Accept", MediaType.APPLICATION_JSON_VALUE);
            Optional<String> supplierIdOptional = gatewayUtil.getSupplierId(exchange);
            supplierIdOptional.ifPresent(s -> headers.add(GatewayConstant.SUPPLIER_ID, s));

            HttpEntity<String> formEntity = new HttpEntity<>(requestJson, headers);
            return restTemplate.postForObject(url, formEntity, Result.class);
        } finally {
            RequestConfigHolder.clear();
        }
    }

    /**
     * 判断请求是否走本机房
     *
     * @param exchange 请求上下文
     * @param areaCode 地市编码
     * @return 是否
     */
    public Boolean isCurrentRoom(ServerWebExchange exchange, String areaCode) {

        String curLocation = forwardProperties.getCurLocation();
        gatewayUtil.setAttribute(exchange, GatewayConstant.CACHED_AREA_CODE_KEY, Convert.toStr(areaCode, ""));

        String location = LocalCache.getLocation(areaCode);
        if (StrUtil.isBlank(location)) {
            // 未配置机房, 走本机房
            return true;
        }
        // 是本机房 走本机房
        return location.equals(curLocation);
    }

    /**
     * 异地服务列表 轮训 取一个
     */
    public Optional<String> getServer(String path, String serviceName) {
        List<Instance> backServerList = LocalCache.getServiceList(serviceName);
        if (CollUtil.isEmpty(backServerList)) {
            return Optional.empty();
        }
        Instance instance = roundRobinLoadBalancer.getInstance(backServerList);
        if (instance == null) {
            return Optional.empty();
        }
        String ip = instance.getIp();
        int port = instance.getPort();

        return Optional.of("http://" + ip + ":" + port + path);
    }

}
