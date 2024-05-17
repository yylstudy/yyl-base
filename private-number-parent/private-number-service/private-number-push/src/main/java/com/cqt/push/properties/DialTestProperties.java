package com.cqt.push.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author huweizhong
 * date  2023/10/8 14:45
 */
@Data
@Component
@ConfigurationProperties(prefix = "dial-test")
public class DialTestProperties {

    private Integer num;

    private String checkPushUrl;
}
