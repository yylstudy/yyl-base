package com.linkcircle.dynamic.datasource.config;

import com.linkcircle.dynamic.datasource.factory.JdbcDataSourceFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author yang.yonglian
 * @version 1.0.0
 * @Description 释放资源
 * @createTime 2022/4/14 18:08
 */
@Component
@Slf4j
@EnableScheduling
public class DyncmicDatasourceMonitor {
    @Autowired
    private JdbcDataSourceFactory jdbcDataSourceFactory;
    @Autowired
    private DynamicRoutingDataSource dynamicRoutingDataSource;

    /**
     * 每天晚上10点执行
     */
    @Scheduled(cron = "${clearUnUseDataSource.cron:0 0 22 * * ?}")
    public void clearUnUseDataSource(){
        try(Connection connection = dynamicRoutingDataSource.getConnection()){
            List<String> usingDataSourceNames = jdbcDataSourceFactory.getUsingDataSourceNamesFromDatabase(connection);
            Map<String, DataSource> dataSourceMap = dynamicRoutingDataSource.getDataSources();
            List<String> removeDatasource = dataSourceMap.keySet().stream()
                    .filter(key->!"master".equals(key)&&!usingDataSourceNames.contains(key)).collect(Collectors.toList());
            log.info("removeDatasource:{}",removeDatasource);
            removeDatasource.stream().forEach(dynamicRoutingDataSource::removeDataSource);
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }
}
