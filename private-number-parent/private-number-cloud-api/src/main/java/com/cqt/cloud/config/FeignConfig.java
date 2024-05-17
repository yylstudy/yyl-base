package com.cqt.cloud.config;

import com.alibaba.cloud.nacos.NacosConfigProperties;
import com.alibaba.cloud.nacos.NacosDiscoveryProperties;
import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingService;
import com.dtflys.forest.springboot.annotation.ForestScan;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

/**
 * @author linshiqiang
 * @date 2022/8/26 13:58
 */
@Slf4j
@Configuration
@EnableFeignClients("com.cqt.cloud.api")
@RequiredArgsConstructor
@ForestScan("com.cqt.cloud.api")
public class FeignConfig {

    private final NacosDiscoveryProperties nacosDiscoveryProperties;

    private final NacosConfigProperties nacosConfigProperties;

    @Bean
    public NamingService namingService() throws NacosException {
        log.info("init nacos naming service");
        Properties properties = nacosDiscoveryProperties.getNacosProperties();
        return NacosFactory.createNamingService(properties);
    }

    @Bean
    public ConfigService configService() throws NacosException {
        log.info("init nacos config service");
        Properties properties = nacosConfigProperties.assembleConfigServiceProperties();
        return NacosFactory.createConfigService(properties);
    }
}
