package com.cqt.push.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author hlx
 */
@Data
@Component
@ConfigurationProperties(prefix = "mt-bill")
public class BillProperties {

    /**
     * 美团小号appId
     */
    private String appId;

    /**
     * 美团小号appKey
     */
    private String appKey;

    /**
     * 有录音地址的阈值，通话时长小于多少时有录音地址置空
     */
    private Integer callDuration;
}
