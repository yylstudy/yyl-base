package com.yyl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author yang.yonglian
 * @version 1.0.0
 * @Description TODO
 * @createTime 2021/12/17 16:21
 */
@SpringBootApplication
@EnableScheduling
@RestController
@Slf4j
public class Start {

    public static void main(String[] args) {
        SpringApplication.run(Start.class,args);
    }
}
