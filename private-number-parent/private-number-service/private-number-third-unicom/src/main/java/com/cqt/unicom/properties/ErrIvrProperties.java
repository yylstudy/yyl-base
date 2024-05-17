package com.cqt.unicom.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Data
@Component
@ConfigurationProperties(prefix = "err.ivr" )
public class ErrIvrProperties {

    private Map<String,String> vccidIvr ;

}
