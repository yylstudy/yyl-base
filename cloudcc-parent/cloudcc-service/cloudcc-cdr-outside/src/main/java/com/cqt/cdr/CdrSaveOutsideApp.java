package com.cqt.cdr;

import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * @author linshiqiang
 * date:  2023-06-30 15:21
 */
@Slf4j
@ComponentScan(basePackages = "com.cqt")
@MapperScan(basePackages = {"com.cqt.mapper","com.cqt.cdr.mapper"})
@EnableScheduling
@EnableAsync
@SpringBootApplication
public class CdrSaveOutsideApp {

    public static void main(String[] args) {
        System.setProperty("JM.SNAPSHOT.PATH", "/home/logs");
        System.setProperty("csp.sentinel.log.dir", "/home/logs/sentinel");
        SpringApplication.run(CdrSaveOutsideApp.class, args);
    }

}
