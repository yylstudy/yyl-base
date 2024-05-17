package com.cqt.wechat.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author huweizhong
 * date  2023/2/22 14:36
 */
@ConfigurationProperties(value = "private-wechat-push")
@Component
@Data
public class WechatPushProperties {

    private String billUrl;

    private String msgUrl;

    private String statusUrl;

    private String tokenUrl;

    private Integer smsRetryTimes;

    private Integer smsRetryInterval;

    /**
     * 号码池变更通知URL
     */
    private String poolNotifyUrl;
}
