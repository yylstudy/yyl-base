package com.cqt.unicom.config.nacos;

import io.swagger.annotations.Api;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;

/**
 * @author zhengsuhao
 * @date 2022/12/6
 */
@Api(tags = "Nacos配置")
@Configuration
@RefreshScope
@Data
public class NacosConfig {

    @Value("${unicom.bindnumerUrl}")
    private String bindnumerUrl;

    @Value("${unicom.supplierId}")
    private String supplierId;


}
