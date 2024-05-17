package com.cqt.monitor.config.nacos;

import cn.hutool.core.util.StrUtil;
import com.alibaba.cloud.nacos.NacosConfigProperties;
import com.alibaba.fastjson.JSON;
import com.alibaba.nacos.api.config.ConfigService;
import com.cqt.common.config.nacos.AbstractNacosConfig;
import com.cqt.common.constants.DataIdConstant;
import com.cqt.monitor.cache.SbcDistributorMonitorConfigCache;
import com.cqt.monitor.web.distributor.SbcDistributorMonitorConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author linshiqiang
 * @since 2022-12-02 9:38
 * 中间号 sn服务监控配置信息
 */
@Slf4j
@Component(SbcDistributorMonitorConfigListener.DATA_ID)
@RequiredArgsConstructor
public class SbcDistributorMonitorConfigListener extends AbstractNacosConfig {

    public static final String DATA_ID = DataIdConstant.SBC_DISTRIBUTOR_MONITOR_CONFIG;

    private final NacosConfigProperties nacosConfigProperties;

    private final ConfigService configService;

    @Override
    public void onReceived(String content) {
        if (StrUtil.isEmpty(content)) {
            // TODO 钉钉告警
            return;
        }
        try {
            SbcDistributorMonitorConfig configDTO = JSON.parseObject(content, SbcDistributorMonitorConfig.class);
            SbcDistributorMonitorConfigCache.put(configDTO);
        } catch (Exception e) {
            log.error("SbcDistributorMonitorConfig error: ", e);
            // TODO 钉钉告警
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
