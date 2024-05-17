package com.cqt.cdr.conf;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@RefreshScope
@Configuration
@Data
public class DynamicConfig {
    /**
     * 质检企业
     */
    @Value("${pzinfo.quality-company}")
    private List<String> qualityCompanys;

    /**
     * 重推次数
     */
    @Value("${pzinfo.push-num}")
    private Integer pushnum;

    /**
     * 每次重推睡眠时间
     */
    @Value("${pzinfo.sleep}")
    private Long sleepTime;


    /**
     * md5转换的token携带的特殊的key
     */
    @Value("${pzinfo.sf-key-sfdiscern}")
    private String sfkeysfdiscern;


}