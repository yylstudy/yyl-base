package com.cqt.hmbc.config;

import com.github.xiaoymin.knife4j.spring.annotations.EnableKnife4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import springfox.bean.validators.configuration.BeanValidatorPluginsConfiguration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * @author linshiqiang
 * date 2021/7/6 18:11
 */
@EnableKnife4j
@EnableSwagger2
@Configuration(proxyBeanMethods = false)
@Import(BeanValidatorPluginsConfiguration.class)
@ConditionalOnProperty(name = "swagger.enabled", matchIfMissing = true)
public class SwaggerConfig {

    private final static String BASE_PACKAGE = "com.cqt";
    private final static String TITLE = "通用小号拨测服务";
    private final static String DESCRIPTION = "通用小号拨测服务";
    private final static String VERSION = "1.0";

    @Bean
    public Docket createRestApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .pathMapping("/")
                .select()
                .apis(RequestHandlerSelectors.basePackage(BASE_PACKAGE))
                .paths(PathSelectors.any())
                .build().apiInfo(new ApiInfoBuilder()
                        .title(TITLE)
                        .description(DESCRIPTION)
                        .version(VERSION)
                        .build());
    }
}