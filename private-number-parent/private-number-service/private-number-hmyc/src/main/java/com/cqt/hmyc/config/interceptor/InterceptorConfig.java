package com.cqt.hmyc.config.interceptor;

import com.cqt.common.constants.SystemConstant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author linshiqiang
 * @date 2022/2/22 16:49
 */
@Slf4j
@Configuration
public class InterceptorConfig implements WebMvcConfigurer {

    private final AuthInterceptor authInterceptor;

    public InterceptorConfig(AuthInterceptor authInterceptor) {
        this.authInterceptor = authInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                .addPathPatterns(SystemConstant.URL_PATTERNS);
        log.info("init interceptor.");
    }

}
