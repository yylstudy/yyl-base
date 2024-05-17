package com.cqt.broadnet.common.model.x.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * @author linshiqiang
 * date:  2023-02-15 15:55
 */
@Data
@Component
@ConfigurationProperties("private-number.call")
public class PrivateNumberBindProperties {

    /**
     * 查询绑定信息url
     */
    private String getBindInfoUrl;

    /**
     * 短信推送url
     */
    private String smsPushUrl;

    private String aybBindUrl;

    /**
     * 查询绑定关系超时时间
     */
    private Integer queryTimeout = 1000;

    /**
     * 推送接口超时时间
     */
    private Integer pushTimeout = 10000;

    /**
     * 重试间隔
     */
    private Duration interval = Duration.ofMinutes(5);

    /**
     * 最大重试次数
     */
    private Integer maxRetry = 5;

    /**
     * rabbitmq 延时消息实现方式, dlx=true 为死信队列 + ttl, false为延时插件实现
     */
    private Boolean dlx = false;

    /**
     * 供应商id
     */
    private String supplierId = "broadnet";

    /**
     * 广电cqt
     */
    private String appKey;

    /**
     * 广电秘钥
     */
    private String secretKey;

    private Boolean auth = false;
}
