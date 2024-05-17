package com.cqt.push;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.env.Environment;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * @author linshiqiang
 * date:  2023-01-16 10:18
 */
@Slf4j
@ComponentScan(basePackages = "com.cqt")
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class QinghaiAliPushApp {

    public static String ip;

    public static void main(String[] args) throws UnknownHostException {
        System.setProperty("JM.SNAPSHOT.PATH", "/home/logs");
        System.setProperty("csp.sentinel.log.dir", "/home/logs/sentinel");
        ConfigurableApplicationContext application = SpringApplication.run(QinghaiAliPushApp.class, args);
        Environment env = application.getEnvironment();
        ip = InetAddress.getLocalHost().getHostAddress();
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
