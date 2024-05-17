package com.cqt.sms;

import cn.hutool.extra.spring.EnableSpringUtil;
import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.cloud.client.SpringCloudApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableAsync;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * @author hlx
 */
@Slf4j
@SpringCloudApplication
@ComponentScan(basePackages = "com.cqt")
@MapperScan("com.cqt.sms.dao.mapper")
@EnableSpringUtil
@EnableAsync
@EnableFeignClients
public class SmsApplication {

    public static void main(String[] args) throws UnknownHostException {
        System.setProperty("JM.SNAPSHOT.PATH", "/home/logs");
        System.setProperty("csp.sentinel.log.dir", "/home/logs/sentinel");
        ConfigurableApplicationContext application = SpringApplication.run(SmsApplication.class, args);
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
