package com.yyl;

import com.linkcircle.basecom.annotation.EnableAutoFillBaseEntity;
import com.linkcircle.basecom.annotation.EnableJwtFilter;
import com.linkcircle.basecom.annotation.EnableOperatelog;
import com.linkcircle.basecom.annotation.EnableSignCheck;
import com.linkcircle.mybatis.annotation.EnableFieldEncrypt;
import com.linkcircle.mybatis.annotation.EnableFieldSensitive;
import com.linkcircle.redis.annotation.EnableRepeatSubmit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author yang.yonglian
 * @version 1.0.0
 * @Description TODO
 * @createTime 2024/3/13 14:27
 */
@SpringBootApplication
//@EnableJwtFilter(tokenHandler = MyTokenHandler2.class)
@EnableSignCheck
//@EnableOperatelog(operateLogService = MyDefaultOperateLogService.class)
@EnableAutoFillBaseEntity
@EnableRepeatSubmit(userIdHandler = MyDefaultUserIdHandler.class)
//@MapperScan("com.yyl.mapper")
@EnableFieldEncrypt
@EnableFieldSensitive
public class App {
    public static void main(String[] args) {
        System.out.println(System.currentTimeMillis());
        SpringApplication.run(App.class,args);
    }
}
