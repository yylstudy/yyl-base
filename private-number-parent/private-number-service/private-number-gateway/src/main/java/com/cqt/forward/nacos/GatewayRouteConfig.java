package com.cqt.forward.nacos;

import com.alibaba.cloud.nacos.NacosConfigProperties;
import com.alibaba.fastjson.JSON;
import com.alibaba.nacos.api.config.ConfigService;
import com.cqt.common.config.nacos.AbstractNacosConfig;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.event.RefreshRoutesEvent;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author linshiqiang
 * @date 2022/8/25 09:15
 * 动态路由配置
 */
@Slf4j
@Component(GatewayRouteConfig.DATA_ID)
@RequiredArgsConstructor
public class GatewayRouteConfig extends AbstractNacosConfig {

    public static final String DATA_ID = "private-gateway-router.json";

    private final NacosConfigProperties nacosConfigProperties;

    private final ConfigService configService;

    private final ApplicationEventPublisher publisher;

    @Getter
    private List<RouteDefinition> routeDefinitions;

    @Override
    public void onReceived(String content) {
        // 将 json 串转成 RouteDefinition 对象集合
        this.routeDefinitions = JSON.parseArray(content, RouteDefinition.class);
        publisher.publishEvent(new RefreshRoutesEvent(this));
    }

    @Override
    public String getDataId() {
        return DATA_ID;
    }

    @Override
    public String getGroup() {
        return nacosConfigProperties.getGroup();
    }

    @Override
    public ConfigService configService() {
        return configService;
    }
}
