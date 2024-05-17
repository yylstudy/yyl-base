package com.cqt.monitor.web.switchroom.job;

import com.cqt.monitor.common.util.DingUtil;
import com.cqt.monitor.web.switchroom.mapper.PrivateAreaLocationMapper;
import com.cqt.monitor.web.switchroom.mapper.PrivateMonitorErrorLogMapper;
import com.cqt.monitor.web.switchroom.mapper.PrivateMonitorInfoMapper;
import com.cqt.monitor.web.switchroom.service.CommonBindHealthCheck;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/**
 * @author linshiqiang
 * @date 2022/1/25 14:47
 * 动态执行绑定关系 mq redis 监控任务
 */
@Slf4j
@Component
@Lazy(false)
public class BindHealthCheckJob  {

    private final PrivateMonitorErrorLogMapper privateMonitorErrorLogMapper;

    private final PrivateAreaLocationMapper areaLocationMapper;

    private final PrivateMonitorInfoMapper privateMonitorInfoMapper;

    private final DingUtil dingUtil;

    public BindHealthCheckJob(PrivateMonitorErrorLogMapper privateMonitorErrorLogMapper, PrivateAreaLocationMapper areaLocationMapper, PrivateMonitorInfoMapper privateMonitorInfoMapper, DingUtil dingUtil) {
        this.privateMonitorErrorLogMapper = privateMonitorErrorLogMapper;
        this.areaLocationMapper = areaLocationMapper;
        this.privateMonitorInfoMapper = privateMonitorInfoMapper;
        this.dingUtil = dingUtil;
    }

    public void doTaskUnlock(String business) {
        CommonBindHealthCheck commonBindHealthCheck = new CommonBindHealthCheck(business, privateMonitorErrorLogMapper,
                areaLocationMapper, privateMonitorInfoMapper, dingUtil);
        commonBindHealthCheck.healthCheck();
    }

}
