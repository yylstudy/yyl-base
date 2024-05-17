package com.cqt.unicom.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

@Data
@Component
@ConfigurationProperties(prefix = "vccid.check" )
public class VccIdCheckProperties {

    private String common;

    private String thirdUrl;
}
