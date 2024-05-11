package com.linkcircle.dynamic.datasource.factory;

import com.baomidou.dynamic.datasource.creator.DataSourceProperty;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.List;
import java.util.Map;

/**
 * @author yang.yonglian
 * @version 1.0.0
 * @Description TODO
 * @createTime 2024/4/22 14:49
 */

public interface JdbcDataSourceFactory {
    /**
     * 从数据库中获取DataSourceProperty
     * @param conn
     * @return
     */
    Map<String, DataSourceProperty> getDataSourcePropertyListFromDatabase(Connection conn);

    /**
     * 根据dbName从数据库中获取DataSource
     * @param conn
     * @param dsName
     * @return
     */
    DataSource getDataSourceFromDatabaseByDsName(Connection conn, String dsName);

    /**
     * 数据库中获取当前正在使用的数据源
     * @param conn
     * @return
     */
    List<String> getUsingDataSourceNamesFromDatabase(Connection conn);

}
