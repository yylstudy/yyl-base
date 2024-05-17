package com.cqt.mybatisplus.config.core;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @author linshiqiang
 * date 2023-07-22 15:23:00
 * 动态表名规则
 */
@Component
@ConfigurationProperties(prefix = "dynamic")
public class DynamicTableProperties {

    private Map<String, String> tableRule;

    public Map<String, String> getTableRule() {
        return tableRule;
    }

    public void setTableRule(Map<String, String> tableRule) {
        this.tableRule = tableRule;
    }
}
