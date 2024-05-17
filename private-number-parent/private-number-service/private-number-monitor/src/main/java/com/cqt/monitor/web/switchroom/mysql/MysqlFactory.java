package com.cqt.monitor.web.switchroom.mysql;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.db.Db;
import cn.hutool.db.Entity;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.util.List;

/**
 * @author linshiqiang
 * @date 2022/2/22 13:50
 */
@Slf4j
public class MysqlFactory {

    public static MysqlCheckResult syncCheck(String ip, DataSource dataSource) {
        try {
            List<Entity> result = Db.use(dataSource).query("show slave status");
            if (CollUtil.isNotEmpty(result)) {
                Entity entity = result.get(0);
                Integer secondsBehindMaster = entity.getInt("seconds_behind_master");
                String slaveIoRunning = entity.getStr("slave_io_running");
                String slaveSqlRunning = entity.getStr("slave_sql_running");
                if (secondsBehindMaster > 100 || !"Yes".equals(slaveIoRunning) || !"Yes".equals(slaveSqlRunning)) {
                    log.info("主从同步异常: {}", JSON.toJSONString(entity, true));
                    return MysqlCheckResult.builder()
                            .ip(ip)
                            .success(false)
                            .slaveIoRunning(slaveSqlRunning)
                            .slaveIoRunning(slaveIoRunning)
                            .secondsBehindMaster(secondsBehindMaster)
                            .build();
                }
            }
        } catch (Exception e) {
            log.error("mysql connection fail: {}", e.getMessage());
            return MysqlCheckResult.builder().success(false).ip(ip).message(e.getMessage()).build();
        }
        return MysqlCheckResult.builder().success(true).ip(ip).build();
    }

    public static String buildUrl(String ip, Integer port) {
        return "jdbc:mysql://" + ip + ":" + port + "/?serverTimezone=GMT%2B8&useUnicode=true&characterEncoding=utf-8&useSSL=false";
    }

}
