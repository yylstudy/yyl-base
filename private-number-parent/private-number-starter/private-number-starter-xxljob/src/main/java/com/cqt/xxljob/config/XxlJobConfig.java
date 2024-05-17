package com.cqt.xxljob.config;

import com.xxl.job.core.executor.impl.XxlJobSpringExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.commons.util.InetUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * @author linshiqiang
 * @date 2022/2/14 10:34
 */
@ComponentScan(basePackages = "com.cqt")
@Configuration
@ConditionalOnProperty(prefix = "xxljob", name = "adminAddresses")
public class XxlJobConfig {

    private static final Logger log = LoggerFactory.getLogger(XxlJobConfig.class);

    private final XxlJobProperties xxlJobProperties;

    private final InetUtils inetUtils;

    public XxlJobConfig(XxlJobProperties xxlJobProperties, InetUtils inetUtils) {
        this.xxlJobProperties = xxlJobProperties;
        this.inetUtils = inetUtils;
    }

    @Bean
    public XxlJobSpringExecutor xxlJobExecutor() {
        String ipAddress = inetUtils.findFirstNonLoopbackHostInfo().getIpAddress();
        log.info(">>>>>>>>>>> xxl-job config init. {}", ipAddress);
        XxlJobSpringExecutor xxlJobSpringExecutor = new XxlJobSpringExecutor();
        xxlJobSpringExecutor.setAdminAddresses(xxlJobProperties.getAdminAddresses());
        xxlJobSpringExecutor.setAppname(xxlJobProperties.getAppName());
        xxlJobSpringExecutor.setIp(ipAddress);
        xxlJobSpringExecutor.setPort(xxlJobProperties.getPort());
        xxlJobSpringExecutor.setAccessToken(xxlJobProperties.getAccessToken());
        xxlJobSpringExecutor.setLogPath(xxlJobProperties.getLogPath());
        xxlJobSpringExecutor.setLogRetentionDays(xxlJobProperties.getLogRetentionDays());
        return xxlJobSpringExecutor;
    }

}
