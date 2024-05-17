package com.cqt.client;

import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * @author linshiqiang
 * date:  2023-06-16 17:23
 */
@Slf4j
@EnableAsync
@EnableDubbo
@ComponentScan(basePackages = "com.cqt")
@SpringBootApplication
public class ClientServerApp {

    public static void main(String[] args)  {
        System.setProperty("JM.SNAPSHOT.PATH", "/home/logs");
        System.setProperty("csp.sentinel.log.dir", "/home/logs/sentinel");
        SpringApplication.run(ClientServerApp.class, args);
    }
}
