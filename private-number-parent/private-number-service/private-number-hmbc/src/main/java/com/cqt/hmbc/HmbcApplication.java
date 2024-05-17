package com.cqt.hmbc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author hlx
 * @date 2021-07-12
 */
@SpringBootApplication(scanBasePackages = "com.cqt")
public class HmbcApplication {

    static {
        System.setProperty("JM.SNAPSHOT.PATH", "/home/logs");
        System.setProperty("csp.sentinel.log.dir", "/home/logs/sentinel");
    }

    public static void main(String[] args) {
        SpringApplication.run(HmbcApplication.class, args);
    }
}
