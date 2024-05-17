package com.cqt.monitor.web.switchroom.service;

import com.alibaba.fastjson.JSON;
import com.cqt.monitor.cache.LocalCache;
import com.cqt.monitor.common.util.DingUtil;
import com.cqt.monitor.web.switchroom.mysql.MysqlCheckResult;
import com.cqt.monitor.web.switchroom.mysql.MysqlFactory;
import com.linkcircle.ss.LHikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * @author linshiqiang
 * @date 2022/2/22 13:58
 */
@Slf4j
@Service
public class MysqlMasterSlaveSyncCheck {

    private final DingUtil dingUtil;

    public MysqlMasterSlaveSyncCheck(DingUtil dingUtil) {
        this.dingUtil = dingUtil;
    }

    public void check() {
        Map<String, LHikariDataSource> dataSourceMap = LocalCache.MYSQL_DATA_SOURCE_MAP;
        dataSourceMap.forEach((ip, dataSource) -> {
            MysqlCheckResult mysqlCheckResult = MysqlFactory.syncCheck(ip, dataSource);
            if (!mysqlCheckResult.getSuccess()) {
                log.info("{}, 主从同步存在异常", mysqlCheckResult);
                dingUtil.sendMessage("主从同步存在异常: " + JSON.toJSONString(mysqlCheckResult));
            }
        });
    }
}
