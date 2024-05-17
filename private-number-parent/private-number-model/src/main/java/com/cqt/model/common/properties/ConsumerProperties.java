package com.cqt.model.common.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @author linshiqiang
 * @date 2022/2/8 14:30
 * 消费 属性
 */
@Data
@Component
@ConfigurationProperties(prefix = "consumer")
public class ConsumerProperties {

    private Map<String, String> operateTypeMap;

    private String currentLocation;

}
