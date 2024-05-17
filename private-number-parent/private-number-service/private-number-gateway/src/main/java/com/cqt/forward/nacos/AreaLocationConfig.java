package com.cqt.forward.nacos;

import com.alibaba.cloud.nacos.NacosConfigProperties;
import com.alibaba.nacos.api.config.ConfigService;
import com.cqt.common.config.nacos.AbstractNacosConfig;
import com.cqt.common.constants.DataIdConstant;
import com.cqt.forward.cache.LocalCache;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author linshiqiang
 * @date 2022/8/25 09:15
 * 地市-机房对应关系 json配置
 */
@Slf4j
@Component(AreaLocationConfig.DATA_ID)
@RequiredArgsConstructor
public class AreaLocationConfig extends AbstractNacosConfig {

    public static final String DATA_ID = DataIdConstant.PRIVATE_AREA_LOCATION;

    private final NacosConfigProperties nacosConfigProperties;

    private final ConfigService configService;

    private final ObjectMapper objectMapper;

    @Override
    public void onReceived(String content) {
        try {
            ConcurrentHashMap<String, String> map = objectMapper.readValue(content, new TypeReference<ConcurrentHashMap<String, String>>() {
            });
            LocalCache.AREA_LOCATION_CACHE.putAll(map);
            log.info("地市-机房对应关系更新完成, 数量: {}", LocalCache.AREA_LOCATION_CACHE.size());
        } catch (JsonProcessingException e) {
            log.error("read area location config error: ", e);
        }
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
