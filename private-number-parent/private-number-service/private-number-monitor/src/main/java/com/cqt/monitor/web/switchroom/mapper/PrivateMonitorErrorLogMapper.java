package com.cqt.monitor.web.switchroom.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cqt.model.monitor.entity.PrivateMonitorErrorLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author linshiqiang
 * @date 2022/1/24 10:29
 * 监控 异常日志
 */
@Mapper
@DS("ms")
public interface PrivateMonitorErrorLogMapper extends BaseMapper<PrivateMonitorErrorLog> {
}
