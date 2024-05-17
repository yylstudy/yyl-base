package com.cqt.monitor.web.switchroom.job;

import com.cqt.monitor.cache.LocalCache;
import com.cqt.monitor.web.switchroom.service.MysqlMasterSlaveSyncCheck;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import org.springframework.stereotype.Component;

/**
 * @author linshiqiang
 * @date 2022/1/27 11:15
 */
@Component
public class HttpXxlJob {

    private final MysqlMasterSlaveSyncCheck mysqlMasterSlaveSyncCheck;

    private final BindHealthCheckJob bindHealthCheckJob;

    private final BusinessHealthCheckJob businessHealthCheckJob;

    private final MonitorInfoInit monitorInfoInit;

    private final StartRunner startRunner;


    public HttpXxlJob(MysqlMasterSlaveSyncCheck mysqlMasterSlaveSyncCheck, BindHealthCheckJob bindHealthCheckJob,
                      BusinessHealthCheckJob businessHealthCheckJob, MonitorInfoInit monitorInfoInit, StartRunner startRunner) {
        this.mysqlMasterSlaveSyncCheck = mysqlMasterSlaveSyncCheck;
        this.bindHealthCheckJob = bindHealthCheckJob;
        this.businessHealthCheckJob = businessHealthCheckJob;
        this.monitorInfoInit = monitorInfoInit;
        this.startRunner = startRunner;

    }

    @XxlJob("refreshLocalCache")
    public void refreshLocalCache() {
        monitorInfoInit.init();
        startRunner.run(null);
        XxlJobHelper.handleSuccess("刷新成功: " + LocalCache.REDIS_INFO_CACHE.size());
    }


    @XxlJob("bindHealthCheckJobHandler")
    public void bindHealthCheckJobHandler() {
        String param = XxlJobHelper.getJobParam();
        bindHealthCheckJob.doTaskUnlock(param);
        XxlJobHelper.log("finish bindHealthCheckJobHandler");
        XxlJobHelper.handleSuccess("执行成功");
    }

    @XxlJob("businessHealthCheckJobHandler")
    public void businessHealthCheckJobHandler() {
        String param = XxlJobHelper.getJobParam();
        businessHealthCheckJob.doTaskUnlock(param);
        XxlJobHelper.log("finish businessHealthCheckJobHandler");
        XxlJobHelper.handleSuccess("执行成功");
    }

    @XxlJob("mysqlMasterSlaveSyncCheckJobHandler")
    public void mysqlMasterSlaveSyncCheckJobHandler() {
        mysqlMasterSlaveSyncCheck.check();
        XxlJobHelper.log("finish mysqlMasterSlaveSyncCheckJobHandler");
        XxlJobHelper.handleSuccess("执行成功");
    }



}
