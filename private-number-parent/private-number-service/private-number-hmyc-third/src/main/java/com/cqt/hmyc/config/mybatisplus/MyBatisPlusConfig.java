package com.cqt.hmyc.config.mybatisplus;

import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.handler.TableNameHandler;
import com.baomidou.mybatisplus.extension.plugins.inner.DynamicTableNameInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * @author linshiqiang
 * @date 2021/6/28 16:36
 */
@Configuration
public class MyBatisPlusConfig {

    @Bean
    public CustomizedSqlInjector customizedSqlInjector() {
        return new CustomizedSqlInjector();
    }

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());

        DynamicTableNameInnerInterceptor innerInterceptor = new DynamicTableNameInnerInterceptor();

        Map<String, TableNameHandler> tableNameHandlerMap = new HashMap<>(16);
        /*tableNameHandlerMap.put(TableNameConstant.PRIVATE_BIND_INFO_AXB_HIS, (sql, tableName) -> {
            String date = "_" + DateUtil.format(DateUtil.date(), "yyyyMMdd");
            return tableName + date;
        });
        tableNameHandlerMap.put(TableNameConstant.PRIVATE_BIND_INFO_AXEYB_HIS, (sql, tableName) -> {
            String date = "_" + DateUtil.format(DateUtil.date(), "yyyyMMdd");
            return tableName + date;
        });
        tableNameHandlerMap.put(TableNameConstant.PRIVATE_BIND_INFO_AXE_HIS, (sql, tableName) -> {
            String date = "_" + DateUtil.format(DateUtil.date(), "yyyyMMdd");
            return tableName + date;
        });*/
        innerInterceptor.setTableNameHandlerMap(tableNameHandlerMap);

        interceptor.addInnerInterceptor(innerInterceptor);

        return interceptor;
    }


}
