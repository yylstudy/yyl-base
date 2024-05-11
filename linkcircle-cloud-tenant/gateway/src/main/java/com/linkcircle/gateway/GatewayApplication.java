package com.linkcircle.gateway;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.ConfigurableApplicationContext;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * @author yang.yonglian
 * @version 1.0.0
 * @Description TODO
 * @createTime 2023/4/26 11:21
 */
@SpringBootApplication
@Slf4j
@EnableDiscoveryClient
public class GatewayApplication {
    public static void main(String[] args) throws UnknownHostException {
        ConfigurableApplicationContext context = SpringApplication.run(GatewayApplication.class, args);
        String ip = InetAddress.getLocalHost().getHostAddress();
        String port = context.getEnvironment().getProperty("server.port");
        log.info("swagger文档地址：http://{}:{}/doc.html",ip,port);
    }
}
