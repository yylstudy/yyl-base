package com.cqt.hmyc.config.nacos;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.StrUtil;
import com.alibaba.cloud.nacos.NacosConfigProperties;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.listener.Listener;
import com.alibaba.nacos.api.exception.NacosException;
import com.cqt.hmyc.config.properties.HdhProperties;
import com.cqt.hmyc.web.bind.cache.LocalCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executor;

/**
 * @author linshiqiang
 * date 2021/8/26 16:38
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class NacosConfigListener implements CommandLineRunner {

    private final NacosConfigProperties nacosConfigProperties;

    private final HdhProperties hdhProperties;

    @Override
    public void run(String... args) throws Exception {
        Properties properties = nacosConfigProperties.assembleConfigServiceProperties();
        ConfigService configService = NacosFactory.createConfigService(properties);

        getAudioCodeConfig(configService);
    }

    /**
     * 放音文件编码
     */
    private void getAudioCodeConfig(ConfigService configService) throws NacosException {
        String configInfo = configService.getConfigAndSignListener(hdhProperties.getAudioCodeDataId(), nacosConfigProperties.getGroup(), 10000,
                new Listener() {

                    @Override
                    public Executor getExecutor() {
                        return null;
                    }

                    @Override
                    public void receiveConfigInfo(String configInfo) {
                        initConfig(configInfo);
                    }
                });

        if (StrUtil.isNotBlank(configInfo)) {
            initConfig(configInfo);
        }
    }

    @SuppressWarnings("all")
    public void initConfig(String configInfo) {
        try {
            JSONObject jsonObject = JSONObject.parseObject(configInfo);
            for (Map.Entry<String, Object> entry : jsonObject.entrySet()) {
                LocalCacheService.HDH_AUDIO_CODE_CACHE.put(entry.getKey(), Convert.toStr(entry.getValue()));
            }
        } catch (Exception e) {
            log.error("更新 HDH_AUDIO_CODE_CACHE异常: ", e);
        }
    }

}
