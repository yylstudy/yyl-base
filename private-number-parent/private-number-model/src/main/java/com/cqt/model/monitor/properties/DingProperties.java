package com.cqt.model.monitor.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 钉钉配置
 *
 * @author hlx
 * @date 2021-11-23
 */
@Data
@Component
@ConfigurationProperties(prefix = "ding")
public class DingProperties {

    /**
     * 钉钉的推送的机器人url
     */
    private List<String> robotUrlList;

    /**
     * 钉钉的秘钥
     */
    private String dingSecret;
}