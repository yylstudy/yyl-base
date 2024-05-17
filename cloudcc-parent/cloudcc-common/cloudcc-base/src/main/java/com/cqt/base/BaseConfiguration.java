package com.cqt.base;

import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.HashUtil;
import com.cqt.base.util.NetUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author linshiqiang
 * date:  2023-08-17 17:12
 */
@Configuration
public class BaseConfiguration {

    @Bean
    public Snowflake snowflake() {
        String localIp = NetUtil.getLocalIp();
        int hash = HashUtil.fnvHash(localIp);
        int id = hash % 32;
        return new Snowflake(id, id);
    }
}
