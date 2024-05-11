package com.sankuai.inf.leaf.common;

import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.config.ConfigService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.StringReader;
import java.util.Properties;

@Component("nacosPropertyFactory")
public class PropertyFactory {
    public static final long DEFAULT_TIMEOUT = 30000;

    @Value("${spring.cloud.nacos.discovery.server-addr}")
    private String nacosServerAddr;
    @Value("${spring.cloud.nacos.discovery.namespace}")
    private String nacosNamespace;
    @Value("${spring.cloud.nacos.config.username}")
    private String nacosUsername;
    @Value("${spring.cloud.nacos.config.password}")
    private String nacosPassword;
    @Value("${spring.cloud.nacos.config.group}")
    private String group;
    @Value("${spring.cloud.nacos.config.leafDataId}")
    private String leafDataId;

    private static Properties leafProperties;

    @PostConstruct
    public void loadLeafConfig() throws Exception{
        Properties nacosProperties = new Properties();
        nacosProperties.setProperty("serverAddr", nacosServerAddr);
        nacosProperties.setProperty("namespace", nacosNamespace);
        nacosProperties.setProperty("username",nacosUsername);
        nacosProperties.setProperty("password",nacosPassword);
        ConfigService configService = NacosFactory.createConfigService(nacosProperties);
        String configInfoStr = configService.getConfig(leafDataId, group, DEFAULT_TIMEOUT);
        leafProperties = new Properties();
        leafProperties.load(new StringReader(configInfoStr));
    }

    public static Properties getProperties() {
        return leafProperties;
    }






}
