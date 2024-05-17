package com.cqt.monitor.web.switchroom.mysql;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author linshiqiang
 * @date 2022/3/18 18:09
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MysqlCheckResult {

    private String ip;

    private Integer secondsBehindMaster;

    private String slaveIoRunning;

    private String slaveSqlRunning;

    private Boolean success;

    private String message;
}
