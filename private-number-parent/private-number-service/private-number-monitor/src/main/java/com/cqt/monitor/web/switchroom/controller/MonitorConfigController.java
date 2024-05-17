package com.cqt.monitor.web.switchroom.controller;

import com.cqt.model.common.Result;
import com.cqt.model.monitor.entity.PrivateMonitorInfo;
import com.cqt.monitor.web.switchroom.mapper.PrivateMonitorInfoMapper;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author linshiqiang
 * @date 2022/3/10 9:33
 */
@RequestMapping("config")
@RestController
public class MonitorConfigController {

    private final PrivateMonitorInfoMapper privateMonitorInfoMapper;

    public MonitorConfigController(PrivateMonitorInfoMapper privateMonitorInfoMapper) {
        this.privateMonitorInfoMapper = privateMonitorInfoMapper;
    }

    @PostMapping()
    public Result add(@RequestBody PrivateMonitorInfo privateMonitorInfo) {
        privateMonitorInfoMapper.insert(privateMonitorInfo);
        return Result.ok();
    }

}
