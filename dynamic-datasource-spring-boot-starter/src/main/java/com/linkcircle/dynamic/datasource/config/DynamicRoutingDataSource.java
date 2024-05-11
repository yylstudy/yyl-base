package com.linkcircle.dynamic.datasource.config;

import com.baomidou.dynamic.datasource.provider.DynamicDataSourceProvider;
import com.baomidou.dynamic.datasource.spring.boot.autoconfigure.DynamicDataSourceProperties;
import com.linkcircle.dynamic.datasource.factory.JdbcDataSourceFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.Map;

/**
 * @author yang.yonglian
 * @version 1.0.0
 * @Description TODO
 * @createTime 2022/4/14 16:43
 */
@Slf4j
public class DynamicRoutingDataSource extends com.baomidou.dynamic.datasource.DynamicRoutingDataSource {
    @Autowired
    private JdbcDataSourceFactory jdbcDataSourceFactory;
    @Autowired
    private DynamicDataSourceProperties properties;

    public DynamicRoutingDataSource(List<DynamicDataSourceProvider> providers){
        super(providers);
    }

    @Override
    public DataSource getDataSource(String ds) {
        Map<String, DataSource> dataSourceMap =  getDataSources();
        if (!StringUtils.hasLength(ds)) {
            return getMasterDatasourceSource();
        } else if (dataSourceMap.containsKey(ds)) {
            log.debug("dynamic-datasource switch to the datasource named [{}]", ds);
            return dataSourceMap.get(ds);
        }else{
            DataSource masterDataSource = getMasterDatasourceSource();
            DataSource dataSource = dataSourceMap.computeIfAbsent(ds,key->{
                Connection connection = null;
                PreparedStatement ps = null;
                ResultSet resultSet = null;
                try{
                    connection = masterDataSource.getConnection();
                    DataSource newDataSource = jdbcDataSourceFactory.getDataSourceFromDatabaseByDsName(connection,ds);
                    return newDataSource;
                }catch (Exception e){
                    log.error("获取企业数据源失败",e);
                    throw new RuntimeException("获取企业失败，请重试，请联系管理员");
                }finally {
                    JdbcUtils.closeResultSet(resultSet);
                    JdbcUtils.closeStatement(ps);
                    JdbcUtils.closeConnection(connection);
                }
            });
            return dataSource;
        }
    }

    public DataSource getMasterDatasourceSource(){
        return getDataSources().get(properties.getPrimary());
    }
}
