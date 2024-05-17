package com.cqt.feign;

import com.alibaba.cloud.nacos.NacosConfigProperties;
import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.exception.NacosException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

/**
 * @author linshiqiang
 * date:  2023-07-03 15:39
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
@EnableFeignClients(basePackages = "com.cqt.feign")
public class OpenFeignConfig {

    private final NacosConfigProperties nacosConfigProperties;

    @Bean
    public ConfigService configService() throws NacosException {
        log.info("init nacos config service");
        Properties properties = nacosConfigProperties.assembleConfigServiceProperties();
        return NacosFactory.createConfigService(properties);
    }
}
