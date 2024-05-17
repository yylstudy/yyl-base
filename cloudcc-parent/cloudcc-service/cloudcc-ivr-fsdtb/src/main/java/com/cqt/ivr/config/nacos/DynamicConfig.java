package com.cqt.ivr.config.nacos;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

/**
 * nacos统一动态配置类
 * @author xinson
 * @date 2023-08-11 15:57
 */
@RefreshScope
@Configuration
@Data
public class DynamicConfig {

    @Value("${cloudcc.configuration.queue-timeout}")
    private Integer queueTimeout;

    @Value("${cloudcc.configuration.server_time_url}")
    private String serverTimeUrl;

    @Value("${cloudcc.configuration.leave-message-timeout}")
    private Integer leaveMessageTimeout;

    @Value("${cloudcc.configuration.uuid-expire-time}")
    private String uuidexpiretime;

    @Value("${cloudcc.configuration.default_leavemsg_start_vidiau}")
    private String defaultleavemsgstartvidiau;

    @Value("${cloudcc.configuration.tts-url}")
    private String ttsurl;

    @Value("${cloudcc.configuration.sbc-lua-url}")
    private String sbcluaurl;
}