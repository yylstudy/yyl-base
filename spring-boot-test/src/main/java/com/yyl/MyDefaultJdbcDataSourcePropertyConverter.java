//package com.yyl;
//
//import com.baomidou.dynamic.datasource.creator.DataSourceProperty;
//import com.linkcircle.dynamic.datasource.factory.JdbcDataSourceFactory;
//import org.springframework.stereotype.Component;
//
//import javax.sql.DataSource;
//import java.sql.Connection;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
///**
// * @author yang.yonglian
// * @version 1.0.0
// * @Description TODO
// * @createTime 2024/4/23 13:56
// */
//@Component
//public class MyDefaultJdbcDataSourcePropertyConverter implements JdbcDataSourceFactory {
//    @Override
//    public Map<String, DataSourceProperty> getDataSourcePropertyListFromDatabase(Connection conn) {
//        return new HashMap<>();
//    }
//
//    @Override
//    public DataSource getDataSourceFromDatabaseByDsName(Connection conn, String dsName) {
//        return null;
//    }
//
//    @Override
//    public List<String> getUsingDataSourceNamesFromDatabase(Connection conn) {
//        return null;
//    }
//}
