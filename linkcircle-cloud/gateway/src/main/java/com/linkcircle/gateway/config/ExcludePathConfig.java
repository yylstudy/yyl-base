package com.linkcircle.gateway.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * @author yang.yonglian
 * @version 1.0.0
 * @Description TODO
 * @createTime 2023/4/20 14:48
 */
@ConfigurationProperties(prefix = "execlude")
@Configuration
@Data
public class ExcludePathConfig {

    private List<String> path = new ArrayList<>();

}
