package com.cqt.sdk.cache;

import com.alibaba.cloud.nacos.NacosConfigProperties;
import com.alibaba.nacos.api.config.ConfigService;
import com.cqt.base.nacos.AbstractNacosConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author linshiqiang
 * date:  2023-09-11 9:31
 */
@Slf4j
@Component(CdrSqlNacosConfig.DATA_ID)
@RequiredArgsConstructor
public class CdrSqlNacosConfig extends AbstractNacosConfig {

    public static final String DATA_ID = "cloudcc-cdr-sql.sql";

    private final NacosConfigProperties nacosConfigProperties;

    private final ConfigService configService;

    @Override
    public void onReceived(String content) {
        CdrSqlCache.put(content);
    }

    @Override
    public String getDataId() {
        return DATA_ID;
    }

    @Override
    public String getGroup() {
        return nacosConfigProperties.getGroup();
    }

    @Override
    public ConfigService configService() {
        return configService;
    }
}
