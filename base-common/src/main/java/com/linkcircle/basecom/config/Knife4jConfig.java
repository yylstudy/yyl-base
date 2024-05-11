package com.linkcircle.basecom.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author yang.yonglian
 * @version 1.0.0
 * @Description TODO
 * @createTime 2023/6/5 11:47
 */
@Slf4j
@Configuration("defaultKnife4jConfig")
@ConditionalOnMissingBean(name = "knife4jConfig")
public class Knife4jConfig {
    public Knife4jConfig(){
        log.info("初始化：{}完成============================",this.getClass().getSimpleName());
    }
    @Value("${knife4j.title:接口文档}")
    private String title;
    @Value("${knife4j.version:1.0.0}")
    private String version;
    @Bean
    public OpenAPI api() {
        return new OpenAPI()
                .components(new Components())
                .info(new Info()
                        .title(title)
                        .contact(new Contact().name("北京承启通科技有限公司"))
                        .version(version)
                );
    }
}

