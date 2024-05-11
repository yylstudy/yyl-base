package com.linkcircle.gateway.loader;

import com.alibaba.cloud.commons.lang.StringUtils;
import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.listener.Listener;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.ArrayType;
import com.linkcircle.gateway.config.GatewayRoutersConfig;
import com.linkcircle.gateway.loader.repository.DynamicRouteService;
import com.linkcircle.gateway.loader.repository.MyInMemoryRouteDefinitionRepository;
import com.linkcircle.gateway.loader.vo.MyRouteDefinition;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.gateway.event.RefreshRoutesEvent;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Executor;
/**
 * @author yang.yonglian
 * @version 1.0.0
 * @Description TODO
 * @createTime 2023/4/26 11:21
 */
@Slf4j
@Component
@RefreshScope
@DependsOn({"gatewayRoutersConfig"})
public class DynamicRouteLoader implements ApplicationEventPublisherAware {
    private static ObjectMapper mapper = new ObjectMapper();
    public static final long DEFAULT_TIMEOUT = 30000;
    @Autowired
    private GatewayRoutersConfig gatewayRoutersConfig;
    private MyInMemoryRouteDefinitionRepository repository;
    private ApplicationEventPublisher publisher;
    private DynamicRouteService dynamicRouteService;
    private ConfigService configService;

    public DynamicRouteLoader(MyInMemoryRouteDefinitionRepository repository, DynamicRouteService dynamicRouteService) {
        this.repository = repository;
        this.dynamicRouteService = dynamicRouteService;
    }

    @PostConstruct
    public Mono<Void> refresh() {
        log.info("初始化路由模式，dataType："+ gatewayRoutersConfig.getDataType());
        this.init();
        return Mono.empty();
    }

    public void init() {
        log.info("初始化路由模式，dataType："+ gatewayRoutersConfig.getDataType());
        loadRoutesByNacos();
    }

    /**
     * 从nacos中读取路由配置
     *
     * @return
     */
    private void loadRoutesByNacos() {
        List<RouteDefinition> routes = new ArrayList<>();
        configService = createConfigService();
        if (configService == null) {
            log.warn("initConfigService fail");
            return;
        }
        try {
            String configInfo = configService.getConfig(gatewayRoutersConfig.getDataId(), gatewayRoutersConfig.getRouteGroup(), DEFAULT_TIMEOUT);
            if (StringUtils.isNotBlank(configInfo)) {
                log.info("获取网关当前配置:\r\n{}", configInfo);
                JavaType javaType = mapper.getTypeFactory().constructParametricType(List.class,RouteDefinition.class);
                routes = mapper.readValue(configInfo,javaType);
            }else{
                log.warn("ERROR: 从Nacos获取网关配置为空，请确认Nacos配置是否正确！");
            }
        } catch (Exception e) {
            log.error("初始化网关路由时发生错误", e);
        }
        for (RouteDefinition definition : routes) {
            log.info("update route : {}", definition.toString());
            dynamicRouteService.add(definition);
        }
        this.publisher.publishEvent(new RefreshRoutesEvent(this));
        dynamicRouteByNacosListener(gatewayRoutersConfig.getDataId(), gatewayRoutersConfig.getRouteGroup());
    }




    /**
     * 监听Nacos下发的动态路由配置
     *
     * @param dataId
     * @param group
     */
    public void dynamicRouteByNacosListener(String dataId, String group) {
        try {
            configService.addListener(dataId, group, new Listener() {
                @Override
                public void receiveConfigInfo(String configInfo) {
                    log.info("进行网关更新:\n\r{}", configInfo);
                    JavaType javaType = mapper.getTypeFactory().constructParametricType(List.class,RouteDefinition.class);
                    List<MyRouteDefinition> definitionList = null;
                    try {
                        definitionList = mapper.readValue(configInfo,javaType);
                    } catch (Exception e) {
                        log.error("网关更新路由异常",e);
                    }
                    for (MyRouteDefinition definition : definitionList) {
                        log.info("update route : {}", definition.toString());
                        dynamicRouteService.update(definition);
                    }
                }
                @Override
                public Executor getExecutor() {
                    log.info("getExecutor\n\r");
                    return null;
                }
            });
        } catch (Exception e) {
            log.error("从nacos接收动态路由配置出错!!!", e);
        }
    }

    /**
     * 创建ConfigService
     *
     * @return
     */
    private ConfigService createConfigService() {
        try {
            Properties properties = new Properties();
            properties.setProperty("serverAddr", gatewayRoutersConfig.getServerAddr());
            if(StringUtils.isNotBlank(gatewayRoutersConfig.getNamespace())){
                properties.setProperty("namespace", gatewayRoutersConfig.getNamespace());
            }
            if(StringUtils.isNotBlank( gatewayRoutersConfig.getUsername())){
                properties.setProperty("username", gatewayRoutersConfig.getUsername());
            }
            if(StringUtils.isNotBlank(gatewayRoutersConfig.getPassword())){
                properties.setProperty("password", gatewayRoutersConfig.getPassword());
            }
            return configService = NacosFactory.createConfigService(properties);
        } catch (Exception e) {
            log.error("创建ConfigService异常", e);
            return null;
        }
    }

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.publisher = applicationEventPublisher;
    }
}
