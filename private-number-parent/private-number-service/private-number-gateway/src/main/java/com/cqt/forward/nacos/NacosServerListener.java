package com.cqt.forward.nacos;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.listener.NamingEvent;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.cqt.common.constants.SystemConstant;
import com.cqt.forward.cache.LocalCache;
import com.cqt.model.common.properties.ForwardProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.stream.Collectors;


/**
 * @author linshiqiang
 * @date 2021/8/26 16:38
 * 监听nacos服务列表变化
 */
@Component
@Slf4j
public class NacosServerListener {

    private final NamingService backNamingService;

    private final ForwardProperties forwardProperties;

    private final ObjectMapper objectMapper;

    public NacosServerListener(@Autowired(required = false) NamingService backNamingService,
                               ForwardProperties forwardProperties,
                               ObjectMapper objectMapper) {
        this.backNamingService = backNamingService;
        this.forwardProperties = forwardProperties;
        this.objectMapper = objectMapper;
    }

    /**
     * 获取异地nacos集群服务列表
     */
    @PostConstruct
    public void getBackServerList() {
        ForwardProperties.BackNacos backNacos = forwardProperties.getBackNacos();
        // 未配置异地nacos, 就不需要开启服务列表监听了
        if (ObjectUtil.isEmpty(backNacos) || ObjectUtil.isEmpty(backNamingService)) {
            log.info("未配置异地nacos");
            return;
        }
        try {
            subscribe(backNacos, forwardProperties.getBackServiceName());
            subscribe(backNacos, forwardProperties.getBackThirdServiceName());
            subscribe(backNacos, forwardProperties.getBackBroadNetThirdServiceName());
        } catch (Exception e) {
            log.error("异地nacos: {}, 异常: ", backNacos.getServerAddr(), e);
        }
    }

    /**
     * 订阅服务列表
     */
    private void subscribe(ForwardProperties.BackNacos backNacos,
                           String serviceName) throws NacosException {
        backNamingService.subscribe(serviceName, backNacos.getGroup(), getBackClusterName(), event -> {
            NamingEvent namingEvent = (NamingEvent) event;
            List<Instance> instanceList = namingEvent.getInstances();
            LocalCache.put(serviceName, instanceList);
            if (log.isInfoEnabled()) {
                try {
                    List<String> ipList = instanceList.stream().map(item -> item.getIp() + ":" + item.getPort()).collect(Collectors.toList());
                    log.info("异地 {} service: {}", serviceName, objectMapper.writeValueAsString(ipList));
                } catch (JsonProcessingException e) {
                    log.error("json error: ", e);
                }
            }
            log.info("异地nacos: {}, {}服务列表发生变化, 当前服务数量: {} 个", backNacos.getServerAddr(), serviceName, LocalCache.size(serviceName));
        });
    }

    /**
     * 获取异地nacos hmyc服务cluster-name
     */
    public List<String> getBackClusterName() {
        String curLocation = forwardProperties.getCurLocation();
        if (SystemConstant.A.equals(curLocation)) {
            return Lists.newArrayList(SystemConstant.B);
        }
        return Lists.newArrayList(SystemConstant.A);
    }

}
