package com.cqt.call;

import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * @author linshiqiang
 * date:  2023-06-16 16:09
 */
@Slf4j
@EnableAsync
@EnableDubbo
@EnableRetry
@ComponentScan(basePackages = "com.cqt")
@MapperScan(basePackages = {"com.cqt.mapper", "com.cqt.call.mapper"})
@SpringBootApplication
public class CallControlApp {

    public static void main(String[] args) {
        System.setProperty("JM.SNAPSHOT.PATH", "/home/logs");
        System.setProperty("csp.sentinel.log.dir", "/home/logs/sentinel");
        SpringApplication.run(CallControlApp.class, args);
    }

}
