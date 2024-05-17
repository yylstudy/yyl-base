package com.cqt.recycle.config;

import com.alibaba.cloud.nacos.NacosConfigProperties;
import com.alibaba.cloud.nacos.NacosDiscoveryProperties;
import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingService;
import com.cqt.model.common.properties.HideProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

/**
 * @author linshiqiang
 * @date 2022/7/27 14:57
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class NacosConfig {

    public static final String SERVER_ADDR = "serverAddr";

    private final NacosConfigProperties nacosConfigProperties;

    private final NacosDiscoveryProperties nacosDiscoveryProperties;

    private final HideProperties hideProperties;

    @Bean
    public NamingService namingService() throws NacosException {
        log.info("init nacos naming service");
        Properties properties = nacosConfigProperties.assembleConfigServiceProperties();
        return NacosFactory.createNamingService(properties);
    }

    @ConditionalOnProperty(prefix = "hide", name = "backNacos")
    @Bean("backNamingService")
    public NamingService backNamingService() throws NacosException {
        log.info("init backNamingService");
        Properties nacosProperties = nacosDiscoveryProperties.getNacosProperties();
        nacosProperties.put(SERVER_ADDR, hideProperties.getBackNacos());
        return NacosFactory.createNamingService(nacosProperties);
    }

    @Bean("configService")
    public ConfigService configService() throws NacosException {
        log.info("init nacos config service");
        Properties properties = nacosConfigProperties.assembleConfigServiceProperties();
        return NacosFactory.createConfigService(properties);
    }

    @ConditionalOnProperty(prefix = "hide", name = "backNacos")
    @Bean("backConfigService")
    public ConfigService backConfigService() throws NacosException {
        log.info("init back nacos config service");
        Properties properties = nacosConfigProperties.assembleConfigServiceProperties();
        properties.put(SERVER_ADDR, hideProperties.getBackNacos());
        return NacosFactory.createConfigService(properties);
    }

}
