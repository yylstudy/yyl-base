package com.linkcircle.dynamic.datasource.factory;

import com.baomidou.dynamic.datasource.creator.DataSourceProperty;
import com.baomidou.dynamic.datasource.creator.DefaultDataSourceCreator;
import com.baomidou.dynamic.datasource.spring.boot.autoconfigure.DynamicDataSourceProperties;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.support.JdbcUtils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @author yang.yonglian
 * @version 1.0.0
 * @Description TODO
 * @createTime 2022/4/15 13:39
 */
public class DefaultJdbcDataSourceFactory implements JdbcDataSourceFactory {
    @Value("${mysql.url.format:jdbc:mysql://%s:%s/%s?characterEncoding=UTF-8&useSSL=true&serverTimezone=Asia/Shanghai&rewriteBatchedStatements=true&allowMultiQueries=true&nullCatalogMeansCurrent=true}")
    private String urlFormat;
    @Autowired
    private DynamicDataSourceProperties properties;
    @Autowired
    private DefaultDataSourceCreator defaultDataSourceCreator;

    /**
     * 从数据库中获取DataSourceProperty
     * @param conn
     * @return
     */
    @Override
    public Map<String, DataSourceProperty> getDataSourcePropertyListFromDatabase(Connection conn){
        List<SysTenantDb> sysTenantDbs = getDataSourceConfigFromDatabase(conn);
        Map<String, DataSourceProperty> dataSourcePropertiesMap = new ConcurrentHashMap();
        for(SysTenantDb sysTenantDb:sysTenantDbs){
            DataSourceProperty dataSourceProperty = sysTenantDb2DataSourceProperty(sysTenantDb);
            dataSourcePropertiesMap.put(sysTenantDb.getTenantId(),dataSourceProperty);
        }
        return dataSourcePropertiesMap;
    }

    /**
     * 根据dbName从数据库中获取DataSource
     * @param conn
     * @param dsName
     * @return
     */
    @Override
    public DataSource getDataSourceFromDatabaseByDsName(Connection conn,String dsName){
        PreparedStatement stmt = null;
        ResultSet resultSet = null;
        try {
            stmt = conn.prepareStatement("select tenant_id,db_host,db_port,db_database,db_user,db_password from sys_tenant_db where status=1  and tenant_id=?");
            stmt.setString(1,dsName);
            resultSet = stmt.executeQuery();
            DataSource newDataSource = null;
            List<SysTenantDb> list = resultSet2SysTenantDb(resultSet);
            if(list.isEmpty()||list.size()>1){
                throw new RuntimeException("企业不存在");
            }
            DataSourceProperty dataSourceProperty = sysTenantDb2DataSourceProperty(list.get(0));
            newDataSource = defaultDataSourceCreator.createDataSource(dataSourceProperty);
            return newDataSource;
        } catch (Exception e) {
            throw new RuntimeException("query getAllSysTenantDb error",e);
        } finally {
            JdbcUtils.closeResultSet(resultSet);
            JdbcUtils.closeStatement(stmt);
        }
    }

    /**
     * 数据库中获取当前正在使用的数据源
     * @param conn
     * @return
     */
    @Override
    public List<String> getUsingDataSourceNamesFromDatabase(Connection conn) {
        List<SysTenantDb> list = getDataSourceConfigFromDatabase(conn);
        return list.stream().map(SysTenantDb::getTenantId).collect(Collectors.toList());
    }

    private List<SysTenantDb> getDataSourceConfigFromDatabase(Connection conn){
        PreparedStatement stmt = null;
        ResultSet resultSet = null;
        try {
            stmt = conn.prepareStatement("select tenant_id,db_host,db_port,db_database,db_user,db_password from sys_tenant_db where status=1 ");
            resultSet = stmt.executeQuery();
            List<SysTenantDb> list = resultSet2SysTenantDb(resultSet);
            return list;
        } catch (Exception e) {
            throw new RuntimeException("query getAllSysTenantDb error",e);
        } finally {
            JdbcUtils.closeResultSet(resultSet);
            JdbcUtils.closeStatement(stmt);
        }
    }

    private List<SysTenantDb> resultSet2SysTenantDb(ResultSet resultSet) throws Exception{
        List<SysTenantDb> list = new ArrayList<>();
        while (resultSet.next()){
            SysTenantDb sysTenantDb = new SysTenantDb();
            sysTenantDb.setTenantId(resultSet.getString(1));
            sysTenantDb.setDbHost(resultSet.getString(2));
            sysTenantDb.setDbPort(resultSet.getString(3));
            sysTenantDb.setDbDatabase(resultSet.getString(4));
            sysTenantDb.setDbUser(resultSet.getString(5));
            sysTenantDb.setDbPassword(resultSet.getString(6));
            list.add(sysTenantDb);
        }
        return list;
    }

    private DataSourceProperty sysTenantDb2DataSourceProperty(SysTenantDb sysTenantDb){
        try{
            DataSourceProperty dataSourceProperty = new DataSourceProperty();
            String url = String.format(urlFormat,sysTenantDb.getDbHost(),sysTenantDb.getDbPort(), sysTenantDb.getDbDatabase());
            dataSourceProperty.setUrl(url);
            dataSourceProperty.setPoolName("tenant-"+sysTenantDb.getTenantId());
            DataSourceProperty masterDataSourceProperty = properties.getDatasource().get(properties.getPrimary());
            dataSourceProperty.setType(masterDataSourceProperty.getType());
            dataSourceProperty.setHikari(properties.getHikari());
            dataSourceProperty.setUsername(sysTenantDb.getDbUser());
            dataSourceProperty.setPassword(sysTenantDb.getDbPassword());
            return dataSourceProperty;
        }catch (Exception e){
            throw new RuntimeException("初始化企业失败");
        }

    }

    @Data
    static class SysTenantDb {
        /**
         * 租户ID
         */
        private String tenantId;
        /**
         * 数据库IP
         */
        private String dbHost;
        /**
         * 数据库端口
         */
        private String dbPort;
        /**
         * database
         */
        private String dbDatabase;
        /**
         * 数据库用户
         */
        private String dbUser;
        /**
         * 数据库密码
         */
        private String dbPassword;
    }
}
