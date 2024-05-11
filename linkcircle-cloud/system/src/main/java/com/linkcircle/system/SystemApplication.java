package com.linkcircle.system;

import com.linkcircle.basecom.annotation.EnableAutoFillBaseEntity;
import com.linkcircle.basecom.annotation.EnableDefaultRequestInterceptor;
import com.linkcircle.basecom.annotation.EnableJwtFilter;
import com.linkcircle.basecom.annotation.EnableOperatelog;
import com.linkcircle.system.config.SystemTokenHandler;
import org.apache.ibatis.annotations.Mapper;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * @Description:
 * @Author: yang.yonglian
 * @CreateDate: 2024/3/1 20:18
 * @Version: 1.0
 */
@MapperScan(value = "com.linkcircle.system.mapper", annotationClass = Mapper.class)
@EnableAspectJAutoProxy(exposeProxy = true)
@SpringBootApplication
@EnableJwtFilter(tokenHandler = SystemTokenHandler.class)
@EnableOperatelog
@EnableAutoFillBaseEntity
public class SystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(SystemApplication.class,args);
    }
}
