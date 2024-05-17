package com.cqt.client.config.nacos;

import com.google.common.collect.Lists;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * nacos统一动态配置类
 *
 * @author tyg
 * @date 2021-04-15 15:57
 */
@Data
@Component
@ConfigurationProperties(prefix = "netty")
public class CloudNettyProperties {

    private Integer port;

    private String url;

    private List<String> callType;

    private List<String> sdkType;

    private List<String> passAuthList = Lists.newArrayList("get_token", "checkin");

    private Integer readOutTime;

    private Integer reconnectionTime;

    private String syncChannelUrl;

}
