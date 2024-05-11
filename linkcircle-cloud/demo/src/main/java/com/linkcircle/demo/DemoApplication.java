package com.linkcircle.demo;

import com.linkcircle.basecom.annotation.EnableDefaultRequestInterceptor;
import com.linkcircle.basecom.annotation.EnableJwtFilter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * @Description:
 * @Author: yang.yonglian
 * @CreateDate: 2024/3/1 20:18
 * @Version: 1.0
 */
@SpringBootApplication
@EnableJwtFilter
@EnableFeignClients("com.linkcircle.demo")
@EnableDefaultRequestInterceptor
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class,args);
    }
}
