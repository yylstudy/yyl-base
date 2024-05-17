package com.cqt.hmyc.config.nacos;

import com.alibaba.cloud.nacos.NacosConfigProperties;
import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

/**
 * @author linshiqiang
 * @date 2022/4/11 15:38
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class NacosConfig {

    private final NacosConfigProperties nacosConfigProperties;

    @Bean
    public NamingService namingService() throws NacosException {
        log.info("init nacos naming service");
        Properties properties = nacosConfigProperties.assembleConfigServiceProperties();
        return NacosFactory.createNamingService(properties);
    }

    @Bean
    public ConfigService configService() throws NacosException {
        log.info("init nacos config service");
        Properties properties = nacosConfigProperties.assembleConfigServiceProperties();
        return NacosFactory.createConfigService(properties);
    }
}
