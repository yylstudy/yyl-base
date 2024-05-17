package com.cqt.unicom.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author huweizhong
 * date  2023/11/7 10:12
 */
@Data
@Component
@ConfigurationProperties(prefix = "private-push")
public class RepushProperties {
    /**
     * 重推间隔
     */
    private int retryMinute;

    /**
     * 重推次数
     */
    private int retryNum;
}
