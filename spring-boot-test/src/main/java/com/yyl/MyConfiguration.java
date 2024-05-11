package com.yyl;

import com.linkcircle.basecom.annotation.EnableAutoFillBaseEntity;
import com.linkcircle.basecom.annotation.EnableOperatelog;
import com.linkcircle.basecom.annotation.EnableSignCheck;
import com.linkcircle.mybatis.annotation.EnableFieldEncrypt;
import com.linkcircle.mybatis.annotation.EnableFieldSensitive;
import com.linkcircle.redis.annotation.EnableRepeatSubmit;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author yang.yonglian
 * @version 1.0.0
 * @Description TODO
 * @createTime 2024/3/26 14:47
 */
@Configuration
@EnableFieldSensitive
public class MyConfiguration {
}
