package com.cqt.client.config;

import com.cqt.client.aspect.RequestAuthInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author linshiqiang
 * date:  2023-11-23 9:19
 */
@Slf4j
@Configuration
public class InterceptorConfig implements WebMvcConfigurer {

    private final RequestAuthInterceptor requestAuthInterceptor;

    public InterceptorConfig(RequestAuthInterceptor requestAuthInterceptor) {
        this.requestAuthInterceptor = requestAuthInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(requestAuthInterceptor)
                .addPathPatterns("/openapi/**");
        log.info("init interceptor.");
    }

}
