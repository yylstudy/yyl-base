package com.cqt.broadnet.web.axb.jsch;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * @author linshiqiang
 * date:  2023-05-31 10:16
 * Sftp连接池信息
 */
@Data
@Component
@ConfigurationProperties(prefix = "sftp")
public class SftpProperties {

    private String path;

    private String host;

    private Integer port = 22;

    private String username;

    private String password;

    private Duration connectTimeout = Duration.ofSeconds(3);

    private Pool pool = new Pool();

    @Data
    public static class Pool {
        private Integer maxIdle = 5;

        private Integer minIdle = 1;

        private Integer maxTotal = 5;
    }
}
