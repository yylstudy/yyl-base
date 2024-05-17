package com.cqt.sms.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author huweizhong
 * date  2023/10/7 16:49
 */
@Data
@Component
@ConfigurationProperties(prefix = "meituan")
public class MeituanProperties {

    private String vccId;

    private String appId;
}
