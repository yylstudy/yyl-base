package com.cqt.monitor.web.distributor.controller;

import com.cqt.monitor.cache.SbcDistributorMonitorConfigCache;
import com.cqt.monitor.web.distributor.SbcDistributorMonitorConfig;
import com.cqt.monitor.web.distributor.service.DistributorMonitorService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author linshiqiang
 * @since 2022-12-02 9:58
 */
@Api(tags = "dis组监控自动切换")
@RestController
@RequiredArgsConstructor
public class DistributorMonitorController {

    private final DistributorMonitorService distributorMonitorService;

    @ApiOperation("nacos配置信息")
    @GetMapping("getCache")
    public SbcDistributorMonitorConfig getCache() {

        return SbcDistributorMonitorConfigCache.get();
    }

    @ApiOperation("测试切换")
    @GetMapping("run")
    public void run() {

        distributorMonitorService.startMonitor();
    }
}
