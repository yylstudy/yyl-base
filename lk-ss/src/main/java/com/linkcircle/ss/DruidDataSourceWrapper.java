package com.linkcircle.ss;//package com.linkcircle.system.autoconfig;

import com.alibaba.druid.filter.Filter;
import com.alibaba.druid.filter.config.ConfigFilter;
import com.alibaba.druid.filter.config.ConfigTools;
import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;
import java.util.Properties;

/**
 * @author yang.yonglian
 * @version 1.0.0
 * @Description TODO
 * @createTime 2022/1/12 17:03
 */
@ConfigurationProperties("spring.datasource.druid")
public class DruidDataSourceWrapper extends DruidDataSource implements InitializingBean {
    private static final Logger log = LoggerFactory.getLogger(DruidDataSourceWrapper.class);
    protected volatile boolean refreshScope = false;
    @Autowired
    private DataSourceProperties basicProperties;
    @Override
    public void afterPropertiesSet() throws Exception {
        //if not found prefix 'spring.datasource.druid' jdbc properties ,'spring.datasource' prefix jdbc properties will be used.
        if (super.getUsername() == null) {
            super.setUsername(basicProperties.determineUsername());
        }
        if (super.getPassword() == null) {
            super.setPassword(basicProperties.determinePassword());
        }else{
            if(refreshScope){
                for(Filter filter:this.getProxyFilters()){
                    if(filter instanceof ConfigFilter){
                        ConfigFilter configFilter = (ConfigFilter) filter;
                        Properties connectionProperties = this.getConnectProperties();
                        boolean decrypt = configFilter.isDecrypt(connectionProperties, null);
                        if(decrypt){
                            String publickey = System.getProperty("druid.config.decrypt.key");
                            if(!StringUtils.isEmpty(publickey)){
                                String password;
                                try{
                                    password = ConfigTools.decrypt(publickey,super.getPassword());
                                    super.setPassword(password);
                                }catch (Exception e){
                                    log.error("decrypt druid password error");
                                }
                            }

                        }
                        break;
                    }
                }
            }
        }
        if (super.getUrl() == null) {
            super.setUrl(basicProperties.determineUrl());
        }
        if (super.getDriverClassName() == null) {
            super.setDriverClassName(basicProperties.getDriverClassName());
        }
        if(!refreshScope){
            refreshScope = true;
        }else{
            log.info("---------------------数据源配置刷新成功，请注意------------------");
        }
    }

    @Autowired(required = false)
    public void autoAddFilters(List<Filter> filters){
        super.filters.addAll(filters);
    }

    /**
     * Ignore the 'maxEvictableIdleTimeMillis < minEvictableIdleTimeMillis' validate,
     * it will be validated again in {@link DruidDataSource#init()}.
     *
     * for fix issue #3084, #2763
     *
     * @since 1.1.14
     */
    @Override
    public void setMaxEvictableIdleTimeMillis(long maxEvictableIdleTimeMillis) {
        try {
            super.setMaxEvictableIdleTimeMillis(maxEvictableIdleTimeMillis);
        } catch (IllegalArgumentException ignore) {
            super.maxEvictableIdleTimeMillis = maxEvictableIdleTimeMillis;
        }
    }
}
