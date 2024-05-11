//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.linkcircle.gateway.loader.repository;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionRepository;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author yang.yonglian
 * @version 1.0.0
 * @Description TODO
 * @createTime 2023/4/26 11:21
 */
@Slf4j
@Component
public class MyInMemoryRouteDefinitionRepository implements RouteDefinitionRepository {
    private final Map<String, RouteDefinition> routes = Collections.synchronizedMap(new LinkedHashMap());

    public MyInMemoryRouteDefinitionRepository() {
    }

    @Override
    public Mono<Void> save(Mono<RouteDefinition> route) {
        return route.flatMap((r) -> {
            if (ObjectUtils.isEmpty(r.getId())) {
                return Mono.error(new IllegalArgumentException("id may not be empty"));
            } else {
                this.routes.put(r.getId(), r);
                return Mono.empty();
            }
        });
    }

    @Override
    public Mono<Void> delete(Mono<String> routeId) {
        return routeId.flatMap((id) -> {
            if (this.routes.containsKey(id)) {
                this.routes.remove(id);
                return Mono.empty();
            } else {
                log.warn("RouteDefinition not found: " + routeId);
                return Mono.empty();
//                return Mono.defer(() -> {
//                    return Mono.error(new NotFoundException("RouteDefinition not found: " + routeId));
//                });
            }
        });
    }

    @Override
    public Flux<RouteDefinition> getRouteDefinitions() {
        Map<String, RouteDefinition> routesSafeCopy = new LinkedHashMap(this.routes);
        return Flux.fromIterable(routesSafeCopy.values());
    }
}
