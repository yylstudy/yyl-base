package com.cqt.cdr;

import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * @author linshiqiang
 * date:  2023-06-30 15:21
 */
@Slf4j
@EnableDubbo
@ComponentScan(basePackages = "com.cqt")
@MapperScan(basePackages = {"com.cqt.mapper", "com.cqt.cdr.mapper"})
@SpringBootApplication
public class CdrSaveInsideApp {

    public static void main(String[] args) {
        System.setProperty("JM.SNAPSHOT.PATH", "/home/logs");
        System.setProperty("csp.sentinel.log.dir", "/home/logs/sentinel");
        SpringApplication.run(CdrSaveInsideApp.class, args);
    }

}
