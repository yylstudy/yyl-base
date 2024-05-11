package com.linkcircle.ss;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

/**
 * @author yang.yonglian
 * @version 1.0.0
 * @Description TODO
 * @createTime 2021/11/25 10:21
 */

public class LHikariDataSource extends HikariDataSource {
    public LHikariDataSource(){
        super();
    }
    public LHikariDataSource(HikariConfig configuration){
        super(configuration);
    }
    @Override
    public String getPassword() {
        String password = super.getPassword();
        return decryptPassword(password);
    }
    private String decryptPassword(String passwordStr){
        String publicKeyStr = System.getProperty("druid.config.decrypt.key");
        return F.decryptPassword(passwordStr,publicKeyStr);
    }


}
