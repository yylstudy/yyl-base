package com.cqt.unicom;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.env.Environment;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * @author linshiqiang
 * @since 2022-11-29 17:57
 */
@Slf4j
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
@ComponentScan(basePackages = "com.cqt")
public class ThirdUnicomApp {

    public static void main(String[] args) throws UnknownHostException {
        System.setProperty("JM.SNAPSHOT.PATH", "/home/logs");
        System.setProperty("csp.sentinel.log.dir", "/home/logs/sentinel");
        ConfigurableApplicationContext application = SpringApplication.run(ThirdUnicomApp.class, args);
        Environment env = application.getEnvironment();
        String ip = InetAddress.getLocalHost().getHostAddress();
        String port = env.getProperty("server.port");
        String path = env.getProperty("server.servlet.context-path");
        log.info("\n----------------------------------------------------------\n\t" +
                "Application is running! Access URLs:\n\t" +
                "Version: \thttp://" + ip + ":" + port + path + "/get-version\n\t" +
                "Health: \thttp://" + ip + ":" + port + path + "/actuator/health\n\t" +
                "Swagger文档: \thttp://" + ip + ":" + port + path + "/doc.html\n\t" +
                "NACOS_SERVER: \t" + env.getProperty("NACOS_SERVER") + " \n\t" +
                "----------------------------------------------------------");
    }
}
