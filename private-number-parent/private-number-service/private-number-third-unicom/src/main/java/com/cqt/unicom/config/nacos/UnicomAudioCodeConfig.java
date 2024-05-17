package com.cqt.unicom.config.nacos;

import com.alibaba.cloud.nacos.NacosConfigProperties;
import com.alibaba.nacos.api.config.ConfigService;
import com.cqt.common.config.nacos.AbstractNacosConfig;
import com.cqt.unicom.config.cache.UnicomLocalCacheService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author zhengsuhao
 * @date 2023/1/17
 * Nacos放音文件配置
 */
@Slf4j
@Component(UnicomAudioCodeConfig.DATA_ID)
@RequiredArgsConstructor
public class UnicomAudioCodeConfig extends AbstractNacosConfig {

    public static final String DATA_ID = "private-unicom-audio-code.json";


    private final NacosConfigProperties nacosConfigProperties;

    private final ConfigService configService;

    private final ObjectMapper objectMapper;

    @Override
    public void onReceived(String content) {
        try {
            ConcurrentHashMap<String, String> map = objectMapper.readValue(content, new TypeReference<ConcurrentHashMap<String, String>>() {
            });
            UnicomLocalCacheService.AUDIO_CODE_CACHE.clear();
            UnicomLocalCacheService.AUDIO_CODE_CACHE.putAll(map);
            log.info("更新UNICOM_AUDIO_CODE_CACHE: {}", UnicomLocalCacheService.AUDIO_CODE_CACHE.size());
        } catch (Exception e) {
            log.error("更新UNICOM_AUDIO_CODE_CACHE异常: ", e);
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
