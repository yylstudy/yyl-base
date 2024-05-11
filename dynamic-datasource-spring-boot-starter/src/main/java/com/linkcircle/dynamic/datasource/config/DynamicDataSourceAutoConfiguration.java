package com.linkcircle.dynamic.datasource.config;

import com.baomidou.dynamic.datasource.creator.DefaultDataSourceCreator;
import com.baomidou.dynamic.datasource.provider.DynamicDataSourceProvider;
import com.baomidou.dynamic.datasource.spring.boot.autoconfigure.DynamicDataSourceAopConfiguration;
import com.baomidou.dynamic.datasource.spring.boot.autoconfigure.DynamicDataSourceAssistConfiguration;
import com.baomidou.dynamic.datasource.spring.boot.autoconfigure.DynamicDataSourceCreatorAutoConfiguration;
import com.baomidou.dynamic.datasource.spring.boot.autoconfigure.DynamicDataSourceProperties;
import com.linkcircle.dynamic.datasource.factory.DefaultJdbcDataSourceFactory;
import com.linkcircle.dynamic.datasource.factory.JdbcDataSourceFactory;
import com.linkcircle.ss.LHikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.Order;

import javax.sql.DataSource;
import java.util.List;

/**
 * @author yang.yonglian
 * @version 1.0.0
 * @Description 动态数据源自动配置类
 * @createTime 2022/4/14 14:13
 */
@Slf4j
@Configuration
@AutoConfigureBefore(value = DataSourceAutoConfiguration.class, name = "com.alibaba.druid.spring.boot.autoconfigure.DruidDataSourceAutoConfigure")
@Import({DynamicDataSourceCreatorAutoConfiguration.class, DynamicDataSourceAopConfiguration.class, DynamicDataSourceAssistConfiguration.class})
@ComponentScan("com.linkcircle.dynamic.datasource")
public class DynamicDataSourceAutoConfiguration {
    public static final int LINKCIRCLE_HIKARI_ORDER = 2999;

    public DynamicDataSourceAutoConfiguration(){
        log.info("动态数据源加载中");
    }

    @Autowired
    private DynamicDataSourceProperties properties;
    @Autowired
    private DefaultDataSourceCreator defaultDataSourceCreator;

    /**
     * 适配加密的HikariDataSource，否则Datasource不是HikariDataSource，主要重写了support方法
     */
    @ConditionalOnClass(LHikariDataSource.class)
    @Configuration
    static class LHikariDataSourceCreatorConfiguration {
        @Bean
        @Order(LINKCIRCLE_HIKARI_ORDER)
        public LHikariDataSourceCreator lhikariDataSourceCreator(DynamicDataSourceProperties dynamicDataSourceProperties) {
            return new LHikariDataSourceCreator(dynamicDataSourceProperties.getHikari());
        }
    }
    @Bean
    public DataSource dataSource(List<DynamicDataSourceProvider> providers) {
        DynamicRoutingDataSource dataSource = new DynamicRoutingDataSource(providers);
        dataSource.setPrimary(properties.getPrimary());
        dataSource.setStrict(properties.getStrict());
        dataSource.setStrategy(properties.getStrategy());
        dataSource.setP6spy(properties.getP6spy());
        dataSource.setSeata(properties.getSeata());
        dataSource.setGraceDestroy(properties.getGraceDestroy());
        return dataSource;
    }
    @Bean
    public JdbcDataSourceProvider jdbcDataSourceProvider(){
        return new JdbcDataSourceProvider(defaultDataSourceCreator,properties);
    }
    @Bean
    @ConditionalOnMissingBean(JdbcDataSourceFactory.class)
    public JdbcDataSourceFactory jdbcDataSourceFactory(){
        return new DefaultJdbcDataSourceFactory();
    }

}
