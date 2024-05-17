package com.cqt.monitor.web.switchroom.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cqt.model.monitor.entity.MonitorPlatformInfo;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author hlx
 * @date 2022-01-26
 */
@Mapper
@DS("ms")
public interface MonitorPlatformInfoMapper extends BaseMapper<MonitorPlatformInfo> {
}
