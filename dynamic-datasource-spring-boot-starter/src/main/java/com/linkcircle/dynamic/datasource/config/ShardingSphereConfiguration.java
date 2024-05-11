//package com.linkcircle.dynamic.datasource.config;
//
//import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
//import org.springframework.boot.context.properties.ConfigurationProperties;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.core.annotation.Order;
//
///**
// * @author yang.yonglian
// * @version 1.0.0
// * @Description TODO
// * @createTime 2023/9/11 14:15
// */
//@ConditionalOnClass(name = {"org.apache.shardingsphere.driver.jdbc.core.datasource.ShardingSphereDataSource"})
//@Configuration
//public class ShardingSphereConfiguration {
//    public static final int ShardingSphere_LINKCIRCLE_HIKARI_ORDER = 2998;
//    @ConfigurationProperties(prefix = "spring.shardingsphere.rules.sharding")
//    @Bean
//    public YamlShardingRuleConfiguration yamlShardingRuleConfiguration(){
//        return new YamlShardingRuleConfiguration();
//    }
//    @Bean
//    @Order(ShardingSphere_LINKCIRCLE_HIKARI_ORDER)
//    public ShardingLHikariDataSourceCreator shardingLHikariDataSourceCreator(){
//        return new ShardingLHikariDataSourceCreator(yamlShardingRuleConfiguration());
//    }
//
//}
