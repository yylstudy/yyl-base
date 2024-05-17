package com.cqt.hmyc.config;

import com.alibaba.cloud.nacos.NacosConfigProperties;
import com.alibaba.fastjson.JSON;
import com.alibaba.nacos.api.config.ConfigService;
import com.cqt.common.config.nacos.AbstractNacosConfig;
import com.cqt.model.call.vo.CallControlResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author linshiqiang
 * @date 2022/4/11 15:38
 * 监听放音编码nacos配置
 */
@Slf4j
@Component(TaobaoBindInfoQueryTestAxbConfig.DATA_ID)
@RequiredArgsConstructor
public class TaobaoBindInfoQueryTestAxbConfig extends AbstractNacosConfig {

    public static final Map<String, CallControlResponse> CACHE = new ConcurrentHashMap<>(16);

    public static final String DATA_ID = "ali-bind-info-query-test-axb.json";

    private final NacosConfigProperties nacosConfigProperties;

    private final ConfigService configService;

    @Override
    public void onReceived(String content) {
        try {
            CACHE.clear();
            CallControlResponse callControlResponse = JSON.parseObject(content, CallControlResponse.class);
            CACHE.put("default", callControlResponse);
        } catch (Exception e) {
            log.error("更新 AUDIO_CODE_CACHE 异常: ", e);
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
