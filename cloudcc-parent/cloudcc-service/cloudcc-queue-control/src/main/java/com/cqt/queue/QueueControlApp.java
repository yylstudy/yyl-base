package com.cqt.queue;

import cn.hutool.extra.spring.EnableSpringUtil;
import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @author linshiqiang
 * date 2023-06-16 21:59:00
 */
@EnableDubbo
@EnableAsync
@EnableSpringUtil
@EnableScheduling
@ComponentScan(basePackages = "com.cqt")
@MapperScan(basePackages = {"com.cqt.mapper", "com.cqt.*.*.mapper"})
@SpringBootApplication
public class QueueControlApp {

    public static void main(String[] args) {
        System.setProperty("JM.SNAPSHOT.PATH", "/home/logs");
        System.setProperty("csp.sentinel.log.dir", "/home/logs/sentinel");
        SpringApplication.run(QueueControlApp.class, args);
    }
}
