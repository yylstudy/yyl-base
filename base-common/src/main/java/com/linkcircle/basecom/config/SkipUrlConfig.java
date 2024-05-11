package com.linkcircle.basecom.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * @author yang.yonglian
 * @version 1.0.0
 * @Description TODO
 * @createTime 2024/2/29 15:03
 */
@ConfigurationProperties(prefix = "system")
@Data
@Component("skipUrlConfig")
public class SkipUrlConfig {
    private List<String> skipUrl = new ArrayList<>();
}
