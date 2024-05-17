package com.cqt.hmyc.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author Xienx
 * @date 2023-06-07 16:57:16:57
 */
@Data
@Component
@ConfigurationProperties(prefix = "bind-query")
public class BindProperties {

    /**
     * 绑定查询接口URL
     */
    private String url;

    /**
     * 接口超时时间, 单位毫秒
     */
    private Integer timeout = 5000;
}
