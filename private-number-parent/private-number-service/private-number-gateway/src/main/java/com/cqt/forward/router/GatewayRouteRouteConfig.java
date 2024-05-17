package com.cqt.forward.router;

import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.web.reactive.HttpHandlerAutoConfiguration;
import org.springframework.boot.autoconfigure.web.reactive.WebFluxAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.gateway.config.GatewayClassPathWarningAutoConfiguration;
import org.springframework.cloud.gateway.config.GatewayLoadBalancerClientAutoConfiguration;
import org.springframework.cloud.gateway.config.GatewayProperties;
import org.springframework.cloud.gateway.filter.factory.GatewayFilterFactory;
import org.springframework.cloud.gateway.handler.predicate.RoutePredicateFactory;
import org.springframework.cloud.gateway.route.RouteDefinitionLocator;
import org.springframework.cloud.gateway.support.ConfigurationService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.DispatcherHandler;

import java.util.List;

/**
 * @author linshiqiang
 * @date 2022/7/26 15:21
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(name = "spring.cloud.gateway.enabled", matchIfMissing = true)
@EnableConfigurationProperties
@AutoConfigureBefore({HttpHandlerAutoConfiguration.class,
        WebFluxAutoConfiguration.class})
@AutoConfigureAfter({GatewayLoadBalancerClientAutoConfiguration.class,
        GatewayClassPathWarningAutoConfiguration.class})
@ConditionalOnClass(DispatcherHandler.class)
public class GatewayRouteRouteConfig {


    @Bean("myRouteDefinitionRouteLocator")
    public MyRouteDefinitionRouteLocator myRouteDefinitionRouteLocator(GatewayProperties properties,
                                                                       List<GatewayFilterFactory> gatewayFilters,
                                                                       List<RoutePredicateFactory> predicates,
                                                                       RouteDefinitionLocator routeDefinitionLocator,
                                                                       ConfigurationService configurationService) {
        return new MyRouteDefinitionRouteLocator(routeDefinitionLocator, predicates,
                gatewayFilters, properties, configurationService);
    }
}
