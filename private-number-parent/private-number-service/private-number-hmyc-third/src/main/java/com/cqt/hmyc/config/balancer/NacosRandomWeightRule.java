package com.cqt.hmyc.config.balancer;

import cn.hutool.core.util.StrUtil;
import com.alibaba.cloud.nacos.NacosDiscoveryProperties;
import com.alibaba.cloud.nacos.NacosServiceManager;
import com.alibaba.cloud.nacos.ribbon.ExtendBalancer;
import com.alibaba.cloud.nacos.ribbon.NacosServer;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.netflix.client.config.IClientConfig;
import com.netflix.loadbalancer.AbstractLoadBalancerRule;
import com.netflix.loadbalancer.DynamicServerListLoadBalancer;
import com.netflix.loadbalancer.Server;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * @author linshiqiang
 * @date 2022/3/3 17:05
 */
@Slf4j
public class NacosRandomWeightRule extends AbstractLoadBalancerRule {

    @Autowired
    private NacosDiscoveryProperties nacosDiscoveryProperties;

    @Autowired
    private Environment environment;

    @Override
    public Server choose(Object key) {
        try {
            DynamicServerListLoadBalancer loadBalancer = (DynamicServerListLoadBalancer) getLoadBalancer();
            String name = loadBalancer.getName();
            String group = environment.getProperty(name + ".ribbon.nacos.group");
            NamingService namingService = nacosDiscoveryProperties.namingServiceInstance();
            List<Instance> instances = namingService.selectInstances(name, group, true);
            if (CollectionUtils.isEmpty(instances)) {
                log.warn("no instance in service {}", name);
                return null;
            }
            Instance instance = ExtendBalancer.getHostByRandomWeight2(instances);
            return new NacosServer(instance);
        } catch (Exception e) {
            log.warn("NacosRule error", e);
            return null;
        }
    }

    @Override
    public void initWithNiwsConfig(IClientConfig iClientConfig) {
    }
}