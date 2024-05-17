package com.cqt.cdr.cloudccsfaftersales.conf;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@RefreshScope
@Configuration
@Data
public class DynamicConfig {
    @Value("${cdrinfo.quality-all-push}")
    private List<String> qualityAllPush;

    @Value("${pzinfo.quality-company}")
    private List<String> qualityCompanys;

    @Value("${pzinfo.push-num}")
    private Integer pushnum;

    @Value("${pzinfo.sf-key-sfdiscern}")
    private String sfkeysfdiscern;

    @Value("${pzinfo.sleep}")
    private Long sleepTime;

    @Value("${pzinfo.xfsfstatu-key}")
    private String xfsfstatukey;
}