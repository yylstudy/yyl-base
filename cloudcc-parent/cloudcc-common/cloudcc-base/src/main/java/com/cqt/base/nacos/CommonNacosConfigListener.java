package com.cqt.base.nacos;

import cn.hutool.core.util.StrUtil;
import com.alibaba.cloud.nacos.NacosConfigManager;
import com.alibaba.nacos.api.config.listener.AbstractListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.Set;

/**
 * @author linshiqiang
 * date 2022/8/25 09:15
 * 监听器，用于监听 nacos 上配置文件的变化
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CommonNacosConfigListener {

    private final NacosConfigManager nacosConfigManager;

    private final Map<String, AbstractNacosConfig> nacosConfigMap;

    /**
     * 在该类的初始化方法中，完成监听器的注册逻辑
     *
     * @throws Exception 异常
     */
    @PostConstruct
    public void init() throws Exception {

        Set<String> dataIds = nacosConfigMap.keySet();
        // 遍历集合，为每个需要监听的文件设置监听器
        for (String dataId : dataIds) {

            AbstractNacosConfig abstractNacosConfig = nacosConfigMap.get(dataId);

            String content = nacosConfigManager.getConfigService()
                    .getConfigAndSignListener(dataId, abstractNacosConfig.getGroup(), 10000,
                            new AbstractListener() {
                                @Override
                                public void receiveConfigInfo(String configInfo) {
                                    // 配置变更后，调用 onReceived 方法
                                    log.info("config: {}", configInfo);
                                    abstractNacosConfig.onReceived(configInfo);
                                }
                            });
            if (StrUtil.isNotEmpty(content)) {
                abstractNacosConfig.onReceived(content);
            }

        }
    }
}
