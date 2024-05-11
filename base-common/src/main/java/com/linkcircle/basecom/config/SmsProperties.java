package com.linkcircle.basecom.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author yang.yonglian
 * @version 1.0.0
 * @Description TODO
 * @createTime 2024/4/1 16:16
 */
@ConfigurationProperties(prefix = "spring.sms")
@Component
@Data
public class SmsProperties {
    /**
     * 短信url
     */
    private  String smsUrl;
    /**
     * 短信签名
     */
    private  String msgSign;
    /**
     * 账户
     */
    private  String msgAccount;
    /**
     * 密码
     */
    private  String msgPwd;
}
