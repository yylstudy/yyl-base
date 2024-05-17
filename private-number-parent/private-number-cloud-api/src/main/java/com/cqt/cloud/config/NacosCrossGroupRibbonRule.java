package com.cqt.cloud.config;

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

/**
 * @author linshiqiang
 * @since 2022/3/3 17:05
 */
@Slf4j
public class NacosCrossGroupRibbonRule extends AbstractLoadBalancerRule {

    @Autowired
    private NamingService namingService;

    @Autowired
    private Environment environment;

    @SuppressWarnings("all")
    @Override
    public Server choose(Object key) {
        try {
            DynamicServerListLoadBalancer loadBalancer = (DynamicServerListLoadBalancer) getLoadBalancer();
            String name = loadBalancer.getName();
            String group = environment.getProperty(name + ".ribbon.nacos.group");
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
