package com.cqt.forward.cache;

import com.cqt.common.util.CopyOnWriteMap;
import org.springframework.cloud.gateway.route.RouteDefinition;

import java.util.Map;

/**
 * @author linshiqiang
 * @date 2022/7/26 15:27
 * 路由定义缓存
 */
public class RouteDefinitionCache {

    /**
     * 路由id: 录音定义
     */
    public static final Map<String, RouteDefinition> ROUTE_DEFINITION_CACHE = new CopyOnWriteMap<>();

    public static RouteDefinition get(String id) {
        return ROUTE_DEFINITION_CACHE.get(id);
    }

    public static void put(String id, RouteDefinition routeDefinition) {
        ROUTE_DEFINITION_CACHE.put(id, routeDefinition);
    }

}
