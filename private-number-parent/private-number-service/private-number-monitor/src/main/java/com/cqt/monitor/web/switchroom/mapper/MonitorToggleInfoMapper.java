package com.cqt.monitor.web.switchroom.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cqt.model.monitor.entity.MonitorToggleInfo;
import org.apache.ibatis.annotations.Mapper;

@Mapper
@DS("ms")
public interface MonitorToggleInfoMapper extends BaseMapper<MonitorToggleInfo> {
}
