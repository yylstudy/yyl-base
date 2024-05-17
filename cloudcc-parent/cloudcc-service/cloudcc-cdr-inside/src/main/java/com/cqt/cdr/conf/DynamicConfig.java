package com.cqt.cdr.conf;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;

@RefreshScope
@Configuration
@Data
public class DynamicConfig {
    @Value("${pzinfo.push-num}")
    private Integer pushnum;

    @Value("${pzinfo.sleep}")
    private Long sleepTime;
}