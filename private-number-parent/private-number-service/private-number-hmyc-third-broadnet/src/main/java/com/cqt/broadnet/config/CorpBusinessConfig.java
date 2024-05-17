package com.cqt.broadnet.config;

import com.alibaba.cloud.nacos.NacosConfigProperties;
import com.alibaba.nacos.api.config.ConfigService;
import com.cqt.broadnet.common.cache.CorpBusinessConfigCache;
import com.cqt.common.config.nacos.AbstractNacosConfig;
import com.cqt.common.constants.DataIdConstant;
import com.cqt.model.corpinfo.dto.PrivateCorpBusinessInfoDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @author linshiqiang
 * @date 2022/8/25 09:15
 * 企业业务配置信息
 */
@Slf4j
@Component(CorpBusinessConfig.DATA_ID)
@RequiredArgsConstructor
public class CorpBusinessConfig extends AbstractNacosConfig {

    public static final String DATA_ID = DataIdConstant.PRIVATE_NUMBER_CORP_BUSINESS_INFO_DATA_ID;

    private final NacosConfigProperties nacosConfigProperties;

    private final ConfigService configService;

    private final ObjectMapper objectMapper;

    @Override
    public void onReceived(String content) {
        refreshCorpBusinessConfig(content);
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

    private void refreshCorpBusinessConfig(String configInfo) {
        try {
            Map<String, PrivateCorpBusinessInfoDTO> map = objectMapper.readValue(configInfo, new TypeReference<Map<String, PrivateCorpBusinessInfoDTO>>() {
            });
            CorpBusinessConfigCache.putAll(map);
            log.info("刷新企业业务配置: {}", CorpBusinessConfigCache.size());
        } catch (JsonProcessingException e) {
            log.error("read refreshCorpBusinessConfig error: ", e);
        }
    }
}
