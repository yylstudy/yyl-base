package com.cqt.broadnet.web.axb.jsch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author linshiqiang
 * date:  2023-03-29 11:02
 * sftp连接池配置
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class SftpPoolConfig {

    private final SftpProperties sftpProperties;

    @RefreshScope
    @Bean
    public JschConnectionPool jschConnectionPool() {
        SftpProperties.Pool pool = sftpProperties.getPool();
        JschConnectionPool connectionPool = new JschConnectionPool(sftpProperties.getHost(),
                sftpProperties.getPort(),
                sftpProperties.getUsername(),
                sftpProperties.getPassword(),
                pool.getMaxTotal(),
                pool.getMinIdle(),
                pool.getMaxIdle(),
                sftpProperties.getConnectTimeout());
        log.info("jsch connect pool init.");
        return connectionPool;
    }
}
