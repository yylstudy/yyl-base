package com.cqt.monitor.web.switchroom.controller;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.cqt.model.common.Result;
import com.cqt.monitor.web.switchroom.job.BindHealthCheckJob;
import com.cqt.monitor.web.switchroom.job.BusinessHealthCheckJob;
import com.cqt.monitor.web.switchroom.job.MonitorInfoInit;
import com.cqt.monitor.web.switchroom.job.StartRunner;
import com.cqt.monitor.web.switchroom.service.MonitorService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


/**
 * @author hlx
 * @since 2022-01-21
 */
@Api(tags = "双机房切换监控")
@Slf4j
@RestController
@RequiredArgsConstructor
public class MonitorController {

    private final StartRunner startRunner;

    private final MonitorInfoInit monitorInfoInit;

    private final MonitorService monitorService;

    private final BindHealthCheckJob bindHealthCheckJob;

    private final BusinessHealthCheckJob businessHealthCheckJob;


    @ApiOperation("双机房redis mq监控切换测试")
    @GetMapping("startBindMonitorJob/{business}")
    public Result startBindMonitorJob(@PathVariable("business") String business) {
        bindHealthCheckJob.doTaskUnlock(business);
        return Result.ok();
    }

    @GetMapping("startPlatformJob/{platform}")
    public Result startPlatformJob(@PathVariable String platform){
        businessHealthCheckJob.doTaskUnlock(platform);
        return Result.ok();
    }

    /**
     * 重新加载数据库数据
     *
     * @return ok
     */
    @GetMapping("/config/refresh")
    public Result refreshConfig() {
        // 刷新缓存
        startRunner.run(null);
        monitorInfoInit.init();
        log.info("数据库配置写入缓存");
        return Result.ok();
    }

    /**
     * 执行一次该平台的切换脚本
     * @param platform 平台名
     * @return 成功
     */
    @GetMapping("/toggle")
    public Result toggle(@RequestParam("platform") String platform) {
        monitorService.toggle(platform, "接口执行切换",null);
        return Result.ok();
    }

    /**
     * 执行一次该平台的恢复脚本
     *
     * @param platform 平台名
     * @return 成功
     */
    @GetMapping("/recover")
    public Result recover(@RequestParam("platform") String platform) {
        monitorService.recover(platform, null);
        return Result.ok();
    }

    /**
     * 手动执行切换
     *
     * @param id
     * @return 成功
     */
    @GetMapping("/toggleByIp")
    public Result toggleByIp(@RequestParam("id") String id) {
        monitorService.toggleByIp(id);
        return Result.ok();
    }

    /**
     * 手动执行恢复
     *
     * @param id
     * @return 成功
     */
    @GetMapping("/recoverByIp")
    public Result recoverByIp(@RequestParam("id") String id) {
        monitorService.recoverByIp(id);
        return Result.ok();
    }

    @GetMapping("/test")
    public Result test() {
        try (HttpResponse httpResponse = HttpRequest.post("http://172.16.246.97:18802/private-agent/command/execute")
                .timeout(10000)
                .contentType("application/json")
                .body("ps -ef | grep CTD_MAP_slave| grep -v grep | awk '{print $2}'")
                .execute()) {
            String body = httpResponse.body();
            if (StringUtils.isEmpty(body)) {
                log.info("111");
            }
            log.info(body);

        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return Result.ok();
    }

}
