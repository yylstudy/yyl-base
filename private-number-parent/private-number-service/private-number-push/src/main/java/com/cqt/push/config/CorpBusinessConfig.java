package com.cqt.push.config;

import com.alibaba.cloud.nacos.NacosConfigProperties;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.nacos.api.config.ConfigService;
import com.cqt.common.config.nacos.AbstractNacosConfig;
import com.cqt.common.constants.DataIdConstant;
import com.cqt.model.corpinfo.dto.PrivateCorpBusinessInfoDTO;
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

        JSONObject jsonObject = JSON.parseObject(configInfo);
        for (Map.Entry<String, Object> entry : jsonObject.entrySet()) {
            String vccId = entry.getKey();
            Object value = entry.getValue();
            PrivateCorpBusinessInfoDTO privateCorpBusinessInfoDTO = JSON.parseObject(JSON.toJSONString(value), PrivateCorpBusinessInfoDTO.class);
            CorpBusinessConfigCache.put(vccId, privateCorpBusinessInfoDTO);
        }
        log.info("refresh corp business config, size: {}", CorpBusinessConfigCache.size());
    }
}
