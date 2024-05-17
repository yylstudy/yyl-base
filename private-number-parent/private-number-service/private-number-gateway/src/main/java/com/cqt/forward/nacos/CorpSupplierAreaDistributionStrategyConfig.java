package com.cqt.forward.nacos;

import com.alibaba.cloud.nacos.NacosConfigProperties;
import com.alibaba.nacos.api.config.ConfigService;
import com.cqt.common.config.nacos.AbstractNacosConfig;
import com.cqt.common.constants.DataIdConstant;
import com.cqt.forward.cache.CorpSupplierAreaDistributionStrategyConfigCache;
import com.cqt.model.corpinfo.dto.SupplierWeight;
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
 * 地市-机房对应关系 json配置
 */
@Slf4j
@Component(CorpSupplierAreaDistributionStrategyConfig.DATA_ID)
@RequiredArgsConstructor
public class CorpSupplierAreaDistributionStrategyConfig extends AbstractNacosConfig {

    public static final String DATA_ID = DataIdConstant.SUPPLIER_DISTRIBUTION_STRATEGY;

    private final NacosConfigProperties nacosConfigProperties;

    private final ConfigService configService;

    private final ObjectMapper objectMapper;

    @Override
    public void onReceived(String content) {
        try {
            Map<String, List<SupplierWeight>> map = objectMapper.readValue(content, new TypeReference<Map<String, List<SupplierWeight>>>() {
            });
            CorpSupplierAreaDistributionStrategyConfigCache.putAll(map);
            log.info("刷新企业-供应商地市号码分配策略: {}", CorpSupplierAreaDistributionStrategyConfigCache.size());
        } catch (JsonProcessingException e) {
            log.error("read CorpSupplierAreaDistributionStrategyConfig error: ", e);
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
