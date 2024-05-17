package com.cqt.forward.nacos;

import cn.hutool.core.util.StrUtil;
import com.alibaba.cloud.nacos.NacosConfigProperties;
import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingService;
import com.cqt.model.common.properties.ForwardProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

/**
 * @author linshiqiang
 * date 2022/8/25 09:15
 * nacos bean的定义
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class NacosConfiguration {

    public static final String NAMESPACE = "namespace";

    public static final String USERNAME = "username";

    public static final String PASSWORD = "password";

    public static final String SERVER_ADDR = "serverAddr";

    private final ForwardProperties forwardProperties;

    private final NacosConfigProperties nacosConfigProperties;

    @Bean
    public ConfigService configService() throws NacosException {
        log.info("init nacos config service");
        Properties properties = nacosConfigProperties.assembleConfigServiceProperties();
        return NacosFactory.createConfigService(properties);
    }

    /**
     * 异地nacos NamingService
     * 只有配置了, 定义bean
     */
    @Bean("backNamingService")
    @ConditionalOnProperty(prefix = "forward.backNacos", name = "serverAddr")
    public NamingService backNamingService() throws NacosException {
        Properties properties = getBackProperties();
        log.info("init nacos back naming service");
        return NacosFactory.createNamingService(properties);
    }

    private Properties getBackProperties() {
        ForwardProperties.BackNacos backNacos = forwardProperties.getBackNacos();
        Properties properties = new Properties();
        properties.setProperty(SERVER_ADDR, backNacos.getServerAddr());
        properties.setProperty(NAMESPACE, backNacos.getNamespace());
        properties.setProperty(USERNAME, StrUtil.isEmpty(backNacos.getUsername()) ? nacosConfigProperties.getUsername() : backNacos.getUsername());
        properties.setProperty(PASSWORD, StrUtil.isEmpty(backNacos.getPassword()) ? nacosConfigProperties.getPassword() : backNacos.getPassword());
        return properties;
    }

}
