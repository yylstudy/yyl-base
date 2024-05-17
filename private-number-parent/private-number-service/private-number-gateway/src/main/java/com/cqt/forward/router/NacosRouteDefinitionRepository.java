package com.cqt.forward.router;

import com.cqt.forward.cache.RouteDefinitionCache;
import com.cqt.forward.nacos.GatewayRouteConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * @author linshiqiang@linkcircle.cn
 * @date 2021/2/2 11:10
 * nacos路由数据源
 */
@Slf4j
@RequiredArgsConstructor
public class NacosRouteDefinitionRepository implements RouteDefinitionRepository {
    private final GatewayRouteConfig gatewayRouteConfig;

    @Override
    public Flux<RouteDefinition> getRouteDefinitions() {
        List<RouteDefinition> definitionList = gatewayRouteConfig.getRouteDefinitions();
        for (RouteDefinition routeDefinition : definitionList) {
            RouteDefinitionCache.put(routeDefinition.getId(), routeDefinition);
        }
        return Flux.fromIterable(definitionList);
    }

    @Override
    public Mono<Void> save(Mono<RouteDefinition> route) {
        return null;
    }

    @Override
    public Mono<Void> delete(Mono<String> routeId) {
        return null;
    }


}
