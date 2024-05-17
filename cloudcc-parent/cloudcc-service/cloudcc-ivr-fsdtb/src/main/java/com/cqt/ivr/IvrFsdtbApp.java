package com.cqt.ivr;

import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * @author xinson
 * date:  2023-07-12 15:21
 */
@Slf4j
@EnableDubbo
@ComponentScan(basePackages = "com.cqt")
@SpringBootApplication
public class IvrFsdtbApp {

    public static void main(String[] args) {
        System.setProperty("JM.SNAPSHOT.PATH", "/home/logs");
        System.setProperty("csp.sentinel.log.dir", "/home/logs/sentinel");
        SpringApplication.run(IvrFsdtbApp.class, args);
    }

}
