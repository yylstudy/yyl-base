package com.cqt.hmyc.config.nacos;

import com.alibaba.cloud.nacos.NacosConfigProperties;
import com.alibaba.nacos.api.config.ConfigService;
import com.cqt.common.config.nacos.AbstractNacosConfig;
import com.cqt.common.constants.DataIdConstant;
import com.cqt.hmyc.web.cache.PrivateFixedPhoneCache;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * @author linshiqiang
 * @date 2022/8/25 09:15
 * 固话号码 json配置
 * {
 * "6608": [
 * "059188888880",
 * "059188888881",
 * "059188888882",
 * "059188888883"
 * ],
 * "1007": [
 * "059111118880",
 * "059111118881",
 * "059111118882",
 * "059111118883"
 * ]
 * }
 */
@Slf4j
@Component(PrivateFixedPhoneConfig.DATA_ID)
@RequiredArgsConstructor
public class PrivateFixedPhoneConfig extends AbstractNacosConfig {

    public static final String DATA_ID = DataIdConstant.PRIVATE_FIXED_PHONE;

    private final NacosConfigProperties nacosConfigProperties;

    private final ConfigService configService;

    private final ObjectMapper objectMapper;

    @Override
    public void onReceived(String content) {

        try {
            Map<String, List<String>> map = objectMapper.readValue(content, new TypeReference<Map<String, List<String>>>() {
            });
            PrivateFixedPhoneCache.clear();
            PrivateFixedPhoneCache.putAll(map);
        } catch (JsonProcessingException e) {
            log.error("fixed phone config read fail: ", e);
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
