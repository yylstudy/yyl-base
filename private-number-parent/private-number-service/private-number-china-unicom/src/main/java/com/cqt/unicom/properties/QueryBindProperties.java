package com.cqt.unicom.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @author huweizhong
 * date  2023/10/24 15:31
 */
@Data
@Component
@ConfigurationProperties(prefix = "unicom")
public class QueryBindProperties {

    /**
     * AXB绑定url
     */
    private Map<String,String> axbUrlMap;

    /**
     * AXE绑定url
     */
    private Map<String,String> axeUrlMap;

    /**
     * 话单推送url
     */
    private Map<String,String> billUrlMap;

    /**
     * 状态推送url
     */
    private Map<String,String> eventUrlMap;

    /**
     * 请求通用号码隐藏url
     */
    private String bindnumerUrl;

    /**
     * 运满满vccid
     */
    private String vccIds;

}
