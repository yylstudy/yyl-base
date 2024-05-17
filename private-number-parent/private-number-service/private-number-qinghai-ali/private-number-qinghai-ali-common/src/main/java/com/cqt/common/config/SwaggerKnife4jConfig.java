package com.cqt.common.config;

import com.github.xiaoymin.knife4j.spring.annotations.EnableKnife4j;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;
import springfox.bean.validators.configuration.BeanValidatorPluginsConfiguration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * @author linshiqiang
 * @date 2021/7/6 18:11
 */
@Configuration
@EnableSwagger2
@EnableKnife4j
@RequiredArgsConstructor
@Import(BeanValidatorPluginsConfiguration.class)
public class SwaggerKnife4jConfig {

    private final static String BASE_PACKAGE = "com.cqt";
    
    private final static String VERSION = "2.3.0";

    private final Environment environment;

    @Bean
    public Docket createRestApi() {
        String appName = environment.getProperty("spring.application.name");
        return new Docket(DocumentationType.SWAGGER_2)
                .pathMapping("/")
                .select()
                .apis(RequestHandlerSelectors.basePackage(BASE_PACKAGE))
                .paths(PathSelectors.any())
                .build().apiInfo(new ApiInfoBuilder()
                        .title(appName)
                        .description(appName)
                        .version(VERSION)
                        .build());
    }
}
