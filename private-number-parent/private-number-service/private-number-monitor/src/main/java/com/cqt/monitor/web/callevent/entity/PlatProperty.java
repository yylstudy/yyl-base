package com.cqt.monitor.web.callevent.entity;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(value = "platform")
@Component
@Data
public class PlatProperty {

    private String formValue;

}
