package com.linkcircle.dynamic.datasource.config;

import com.baomidou.dynamic.datasource.creator.DataSourceCreator;
import com.baomidou.dynamic.datasource.creator.DataSourceProperty;
import com.baomidou.dynamic.datasource.creator.hikaricp.HikariCpConfig;
import com.baomidou.dynamic.datasource.toolkit.ConfigMergeCreator;
import com.baomidou.dynamic.datasource.toolkit.DsStrUtils;
import com.linkcircle.ss.LHikariDataSource;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;


/**
 * @author yang.yonglian
 * @version 1.0.0
 * @Description 加密LHikariDataSource创建器
 * @createTime 2023/6/8 10:00
 */

public class LHikariDataSourceCreator implements DataSourceCreator {
    private static final ConfigMergeCreator<HikariCpConfig, HikariConfig> MERGE_CREATOR = new ConfigMergeCreator<>("HikariCp", HikariCpConfig.class, HikariConfig.class);
    private String LHIKARI_DATASOURCE = "com.linkcircle.ss.LHikariDataSource";
    private HikariCpConfig gConfig;
    private static Method configCopyMethod = null;
    public LHikariDataSourceCreator(HikariCpConfig gConfig){
        this.gConfig = gConfig;
    }
    static {
        fetchMethod();
    }
    private static void fetchMethod() {
        Class hikariConfigClass = HikariConfig.class;

        try {
            configCopyMethod = hikariConfigClass.getMethod("copyState", hikariConfigClass);
        } catch (NoSuchMethodException var3) {
            try {
                configCopyMethod = hikariConfigClass.getMethod("copyStateTo", hikariConfigClass);
            } catch (NoSuchMethodException var2) {
                throw new RuntimeException("HikariConfig does not has 'copyState' or 'copyStateTo' method!");
            }
        }
    }
    @Override
    public DataSource createDataSource(DataSourceProperty dataSourceProperty) {
        HikariConfig config = MERGE_CREATOR.create(this.gConfig, dataSourceProperty.getHikari());
        config.setUsername(dataSourceProperty.getUsername());
        config.setPassword(dataSourceProperty.getPassword());
        config.setJdbcUrl(dataSourceProperty.getUrl());
        config.setPoolName(dataSourceProperty.getPoolName());
        String driverClassName = dataSourceProperty.getDriverClassName();
        if (DsStrUtils.hasText(driverClassName)) {
            config.setDriverClassName(driverClassName);
        }

        if (Boolean.FALSE.equals(dataSourceProperty.getLazy())) {
            return new LHikariDataSource(config);
        } else {
            config.validate();
            LHikariDataSource dataSource = new LHikariDataSource();
            try {
                configCopyMethod.invoke(config, dataSource);
                return dataSource;
            } catch (InvocationTargetException | IllegalAccessException var6) {
                throw new RuntimeException("HikariConfig failed to copy to HikariDataSource", var6);
            }
        }
    }

    @Override
    public boolean support(DataSourceProperty dataSourceProperty) {
        Class<? extends DataSource> type = dataSourceProperty.getType();
        return type == null || LHIKARI_DATASOURCE.equals(type.getName());
    }
}
