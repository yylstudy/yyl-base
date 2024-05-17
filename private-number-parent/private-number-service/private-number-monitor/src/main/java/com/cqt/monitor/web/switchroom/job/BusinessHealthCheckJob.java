package com.cqt.monitor.web.switchroom.job;

import com.cqt.model.monitor.properties.MonitorProperties;
import com.cqt.monitor.web.switchroom.service.MonitorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/**
 * @author hlx
 * @date 2022-01-21
 */
@Slf4j
@Component
@Lazy(false)
public class BusinessHealthCheckJob {

    @Autowired
    private MonitorService monitorService;

    @Autowired
    private MonitorProperties monitorProperties;

    public void doTaskUnlock(String platform){
        if (monitorProperties.getOpenMonitor()){
            monitorService.checkStart(platform);
        }else {
            log.info("monitor is close");
        }
    }
}
