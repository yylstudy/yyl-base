package com.linkcircle.ss;

import org.apache.shardingsphere.infra.datasource.pool.metadata.DataSourcePoolMetaData;
import org.apache.shardingsphere.infra.datasource.pool.metadata.type.hikari.HikariDataSourcePoolFieldMetaData;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * @author yang.yonglian
 * @version 1.0.0
 * @Description LHikariDataSource的连接池元数据，基于SPI，不然sharding-sphere会提示空指针，找不到Datasource
 * 中的属性url（因为LHikariDataSource继承HikariDataSource，只有jdbcUrl没有url）
 * @createTime 2023/9/11 17:53
 */

public class LHikariDataSourcePoolMetaData implements DataSourcePoolMetaData {
    private static final Map<String, Object> DEFAULT_PROPERTIES = new HashMap<>(6, 1);

    private static final Map<String, Object> INVALID_PROPERTIES = new HashMap<>(2, 1);

    private static final Map<String, String> PROPERTY_SYNONYMS = new HashMap<>(6, 1);

    private static final Collection<String> TRANSIENT_FIELD_NAMES = new LinkedList<>();

    static {
        buildDefaultProperties();
        buildInvalidProperties();
        buildPropertySynonyms();
        buildTransientFieldNames();
    }

    private static void buildDefaultProperties() {
        DEFAULT_PROPERTIES.put("connectionTimeout", 30 * 1000L);
        DEFAULT_PROPERTIES.put("idleTimeout", 60 * 1000L);
        DEFAULT_PROPERTIES.put("maxLifetime", 30 * 70 * 1000L);
        DEFAULT_PROPERTIES.put("maximumPoolSize", 50);
        DEFAULT_PROPERTIES.put("minimumIdle", 1);
        DEFAULT_PROPERTIES.put("readOnly", false);
    }

    private static void buildInvalidProperties() {
        INVALID_PROPERTIES.put("minimumIdle", -1);
        INVALID_PROPERTIES.put("maximumPoolSize", -1);
    }

    private static void buildPropertySynonyms() {
        PROPERTY_SYNONYMS.put("url", "jdbcUrl");
        PROPERTY_SYNONYMS.put("connectionTimeoutMilliseconds", "connectionTimeout");
        PROPERTY_SYNONYMS.put("idleTimeoutMilliseconds", "idleTimeout");
        PROPERTY_SYNONYMS.put("maxLifetimeMilliseconds", "maxLifetime");
        PROPERTY_SYNONYMS.put("maxPoolSize", "maximumPoolSize");
        PROPERTY_SYNONYMS.put("minPoolSize", "minimumIdle");
    }

    private static void buildTransientFieldNames() {
        TRANSIENT_FIELD_NAMES.add("running");
        TRANSIENT_FIELD_NAMES.add("poolName");
        TRANSIENT_FIELD_NAMES.add("closed");
    }

    @Override
    public Map<String, Object> getDefaultProperties() {
        return DEFAULT_PROPERTIES;
    }

    @Override
    public Map<String, Object> getInvalidProperties() {
        return INVALID_PROPERTIES;
    }

    @Override
    public Map<String, String> getPropertySynonyms() {
        return PROPERTY_SYNONYMS;
    }

    @Override
    public Collection<String> getTransientFieldNames() {
        return TRANSIENT_FIELD_NAMES;
    }

    @Override
    public HikariDataSourcePoolFieldMetaData getFieldMetaData() {
        return new HikariDataSourcePoolFieldMetaData();
    }

    @Override
    public String getType() {
        return "com.linkcircle.ss.LHikariDataSource";
    }
}
