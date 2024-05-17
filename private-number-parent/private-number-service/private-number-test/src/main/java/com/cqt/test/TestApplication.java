package com.cqt.test;

import cn.hutool.extra.ftp.Ftp;
import com.baomidou.mybatisplus.autoconfigure.IdentifierGeneratorAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

/**
 * 通用小号  接口测试模拟服务
 *
 * @author hlx
 * @date 2022-03-03
 */
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class, IdentifierGeneratorAutoConfiguration.class})
public class TestApplication {

    public static void main(String[] args) {

        SpringApplication.run(TestApplication.class);
        Ftp ftp = new Ftp("");
    }

}
