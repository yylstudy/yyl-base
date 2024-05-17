package com.cqt.forward.router;

import com.cqt.forward.nacos.GatewayRouteConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created with IntelliJ IDEA.
 * ClassName: DynamicRouteConfig
 *
 * @author linshiqiang@linkcircle.cn
 * Date: 2021-10-26 22:27
 * Description:
 */
@Slf4j
@RequiredArgsConstructor
@Configuration(proxyBeanMethods = false)
public class DynamicRouteConfig {

    private final GatewayRouteConfig gatewayRouteConfig;

    @ConditionalOnProperty(prefix = "spring.cloud.gateway.dynamicRoute", name = "enabled", havingValue = "true")
    public class NaocsDynamicRoute {

        @Bean
        public NacosRouteDefinitionRepository nacosRouteDefinitionRepository() {

            return new NacosRouteDefinitionRepository(gatewayRouteConfig);
        }
    }


}
