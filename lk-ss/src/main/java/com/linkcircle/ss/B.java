package com.linkcircle.ss;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.spring.boot.autoconfigure.DruidDataSourceAutoConfigure;
import com.alibaba.druid.spring.boot.autoconfigure.properties.DruidStatProperties;
import com.alibaba.druid.spring.boot.autoconfigure.stat.DruidFilterConfiguration;
import com.alibaba.druid.spring.boot.autoconfigure.stat.DruidSpringAopConfiguration;
import com.alibaba.druid.spring.boot.autoconfigure.stat.DruidStatViewServletConfiguration;
import com.alibaba.druid.spring.boot.autoconfigure.stat.DruidWebStatFilterConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import javax.sql.DataSource;
import java.lang.reflect.Method;

/**
 * @description:
 * @author: yangyonglian
 * @time: 2021/11/11 15:23
 */
@Configuration("BB")
@ConditionalOnClass({DruidDataSourceAutoConfigure.class,DruidDataSource.class})
@AutoConfigureBefore({DruidDataSourceAutoConfigure.class,DataSourceAutoConfiguration.class})
@EnableConfigurationProperties({DruidStatProperties.class, DataSourceProperties.class})
@Import({DruidSpringAopConfiguration.class,
        DruidStatViewServletConfiguration.class,
        DruidWebStatFilterConfiguration.class,
        DruidFilterConfiguration.class})
public class B {
    private static final Logger log = LoggerFactory.getLogger(B.class);
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

    @Bean(initMethod = "init")
    @ConditionalOnMissingBean
    public DataSource dataSource() {
        log.info("-------------------------------custom Init DruidDataSource-----------------------------------------------");
        return new DruidDataSourceWrapper();
    }

}
