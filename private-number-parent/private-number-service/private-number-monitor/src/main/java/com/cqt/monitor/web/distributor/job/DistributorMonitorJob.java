package com.cqt.monitor.web.distributor.job;

import com.cqt.monitor.web.distributor.service.DistributorMonitorService;
import com.cqt.xxljob.annotations.XxlJobRegister;
import com.cqt.xxljob.enums.ExecutorRouteStrategyEnum;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author linshiqiang
 * @since 2022-12-02 16:02
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class DistributorMonitorJob {

    private final DistributorMonitorService distributorMonitorService;

    @XxlJobRegister(jobDesc = "中间号服务异常自动切换dis组权重",
            cron = "0/10 * * * * ?",
            triggerStatus = 0,
            executorRouteStrategy = ExecutorRouteStrategyEnum.SHARDING_BROADCAST)
    @XxlJob("distributorMonitorJob")
    public void doTask() {

        distributorMonitorService.startMonitor();

    }

}
