package com.cqt.hmyc.config.nacos;

import com.alibaba.cloud.nacos.NacosConfigProperties;
import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingService;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

/**
 * @author linshiqiang
 * @date 2022/4/11 15:38
 */
@Configuration
@AllArgsConstructor
public class NacosConfig {

    private NacosConfigProperties nacosConfigProperties;

    @Bean
    public NamingService namingService() throws NacosException {

        Properties properties = nacosConfigProperties.assembleConfigServiceProperties();
        return NacosFactory.createNamingService(properties);
    }
}
