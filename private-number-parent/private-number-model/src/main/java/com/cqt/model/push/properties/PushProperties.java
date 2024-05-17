package com.cqt.model.push.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author hlx
 */
@Data
@Component
@ConfigurationProperties(prefix = "private-push")
public class PushProperties {

    /**
     * 推送失败时重推次数
     */
    private Integer retryNum;

    /**
     * 推送失败时重推时间
     */
    private Integer retryMinute;

}
