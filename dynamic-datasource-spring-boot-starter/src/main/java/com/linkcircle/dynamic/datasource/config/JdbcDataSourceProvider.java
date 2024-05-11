package com.linkcircle.dynamic.datasource.config;

import com.baomidou.dynamic.datasource.creator.DataSourceProperty;
import com.baomidou.dynamic.datasource.creator.DefaultDataSourceCreator;
import com.baomidou.dynamic.datasource.provider.AbstractDataSourceProvider;
import com.baomidou.dynamic.datasource.provider.DynamicDataSourceProvider;
import com.baomidou.dynamic.datasource.spring.boot.autoconfigure.DynamicDataSourceProperties;
import com.linkcircle.dynamic.datasource.factory.JdbcDataSourceFactory;
import com.linkcircle.ss.F;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Map;

/**
 * @author yang.yonglian
 * @version 1.0.0
 * @Description TODO
 * @createTime 2024/4/22 13:56
 */
@Slf4j
public class JdbcDataSourceProvider extends AbstractDataSourceProvider implements DynamicDataSourceProvider {
    private String url;
    private String username;
    private String password;
    @Autowired
    private JdbcDataSourceFactory jdbcDataSourceFactory;

    public JdbcDataSourceProvider(DefaultDataSourceCreator defaultDataSourceCreator,DynamicDataSourceProperties properties){
        super(defaultDataSourceCreator);
        try{
            DataSourceProperty dataSourceProperty = properties.getDatasource().get(properties.getPrimary());
            String driverClassName = dataSourceProperty.getDriverClassName();
            if (StringUtils.hasText(driverClassName)) {
                Class.forName(driverClassName);
                log.info("成功加载数据库驱动程序");
            }
            url = dataSourceProperty.getUrl();
            username = dataSourceProperty.getUsername();
            password = dataSourceProperty.getPassword();
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }


    @Override
    public Map<String, DataSource> loadDataSources() {
        Connection conn = null;
        try {
            String publicKey = System.getProperty("druid.config.decrypt.key");
            String decryptPassword = F.decrypt(F.getPublicKey(publicKey),password);
            conn = DriverManager.getConnection(url, username, decryptPassword);
            log.info("成功获取master数据库连接");
            Map<String, DataSourceProperty> dataSourcePropertiesMap = jdbcDataSourceFactory.getDataSourcePropertyListFromDatabase(conn);
            return createDataSourceMap(dataSourcePropertiesMap);
        } catch (Exception e) {
            log.error("init datasource error",e);
            throw new RuntimeException("init datasource error");
        } finally {
            JdbcUtils.closeConnection(conn);
        }
    }

}
