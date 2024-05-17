package com.cqt.cdr.cloudccsfaftersales;

import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@Slf4j
@ComponentScan(basePackages = "com.cqt")
@MapperScan(basePackages = {"com.cqt.mapper","com.cqt.cdr.cloudccsfaftersales.mapper"})
@SpringBootApplication
public class CloudccSfAftersalesApplication {

    public static void main(String[] args) {
        System.setProperty("JM.SNAPSHOT.PATH", "/home/logs");
        System.setProperty("csp.sentinel.log.dir", "/home/logs/sentinel");
        SpringApplication.run(CloudccSfAftersalesApplication.class, args);
    }
}
