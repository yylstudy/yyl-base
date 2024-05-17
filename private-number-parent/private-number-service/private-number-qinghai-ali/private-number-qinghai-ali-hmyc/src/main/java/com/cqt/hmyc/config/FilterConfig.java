package com.cqt.hmyc.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.ShallowEtagHeaderFilter;

import java.util.Collections;

/**
 * @author linshiqiang
 * @date 2021/11/4 13:55
 */
@Configuration
public class FilterConfig {

    @Bean
    public FilterRegistrationBean<ShallowEtagHeaderFilter> filterRegistrationBean() {
        FilterRegistrationBean<ShallowEtagHeaderFilter> filterBean = new FilterRegistrationBean<>();
        filterBean.setFilter(new ShallowEtagHeaderFilter());
        filterBean.setUrlPatterns(Collections.singletonList("*"));
        return filterBean;
    }

}
