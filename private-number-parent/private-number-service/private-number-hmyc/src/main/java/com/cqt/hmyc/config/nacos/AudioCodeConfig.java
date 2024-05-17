package com.cqt.hmyc.config.nacos;

import com.alibaba.cloud.nacos.NacosConfigProperties;
import com.alibaba.nacos.api.config.ConfigService;
import com.cqt.common.config.nacos.AbstractNacosConfig;
import com.cqt.hmyc.web.cache.LocalCacheService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author linshiqiang
 * @date 2022/4/11 15:38
 */
@Slf4j
@Component(AudioCodeConfig.DATA_ID)
@RequiredArgsConstructor
public class AudioCodeConfig extends AbstractNacosConfig {

    public static final String DATA_ID = "private-audio-code.json";

    private final NacosConfigProperties nacosConfigProperties;

    private final ConfigService configService;

    private final ObjectMapper objectMapper;

    @Override
    public void onReceived(String content) {
        try {
            ConcurrentHashMap<String, String> map = objectMapper.readValue(content, new TypeReference<ConcurrentHashMap<String, String>>() {
            });
            LocalCacheService.AUDIO_CODE_CACHE.putAll(map);
            log.info("更新 AUDIO_CODE_CACHE: {}", LocalCacheService.AUDIO_CODE_CACHE.size());
        } catch (Exception e) {
            log.error("更新 AUDIO_CODE_CACHE异常: ", e);
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
