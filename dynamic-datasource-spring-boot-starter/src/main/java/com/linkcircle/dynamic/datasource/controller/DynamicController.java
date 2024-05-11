package com.linkcircle.dynamic.datasource.controller;

import com.baomidou.dynamic.datasource.DynamicRoutingDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;

/**
 * @author yang.yonglian
 * @version 1.0.0
 * @Description TODO
 * @createTime 2022/4/20 16:53
 */
@RestController

public class DynamicController {
    private Logger log = LoggerFactory.getLogger(DynamicController.class);
    @Autowired
    private DynamicRoutingDataSource dynamicRoutingDataSource;

    @RequestMapping("getDynamicDatasourceByName")
    public String getDynamicDatasourceByName(String name){
        if(!StringUtils.hasText(name)){
            log.info("datasource:{}",name, dynamicRoutingDataSource.getDataSources().keySet());
        }else{
            DataSource dataSource = dynamicRoutingDataSource.getDataSource(name);
            try(Connection connection = dataSource.getConnection()){
                log.info("tenantId:{} get connection success:{}",name, connection);
            }catch (Exception e){
                throw new RuntimeException(e);
            }
        }
        return "success";
    }
}
