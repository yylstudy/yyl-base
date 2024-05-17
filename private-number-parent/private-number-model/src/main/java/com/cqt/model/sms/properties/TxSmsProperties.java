package com.cqt.model.sms.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @Description: 腾讯短信推送特有字段配置
 * @author: scott
 * @date: 2022年03月28日 15:17
 */
@Data
@Component
@ConfigurationProperties(prefix = "tx-sms")
public class TxSmsProperties {

    /**
     * 开发者id, 无填空
     * */
    private String devId;

    /**
     * 应用id, 无填空
     * */
    private String appId;

    /**
     * 客户应用ID，由腾讯侧提供。需要与X号码绑定区分
     * */
    private String sdkAppId;
}
