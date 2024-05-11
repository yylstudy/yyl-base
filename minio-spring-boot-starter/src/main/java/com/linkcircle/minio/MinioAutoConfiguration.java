package com.linkcircle.minio;

import com.linkcircle.minio.config.MinioProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * @author yang.yonglian
 * @version 1.0.0
 * @Description TODO
 * @createTime 2024/4/12 18:28
 */
@Configuration
@EnableConfigurationProperties(MinioProperties.class)
@ComponentScan("com.linkcircle.minio")
public class MinioAutoConfiguration {
}
