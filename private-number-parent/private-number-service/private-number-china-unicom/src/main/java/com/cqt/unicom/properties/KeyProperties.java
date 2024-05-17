package com.cqt.unicom.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "keys" )
public class KeyProperties {

    private String publicKey;

    private String privateKey;

}
