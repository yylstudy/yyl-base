package com.cqt.sms.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.io.Serializable;

/**
 * @author zhengsuhao
 * @date 2023/02/16
 */
@Data
@Component
@ConfigurationProperties(prefix = "sms")
public class SmsProperties implements Serializable {

    private static final long serialVersionUID = -3639714078719991297L;

    /**
     * 通用短信接口请求地址
     */
    private String sendSmsUrl;

}
