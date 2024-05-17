package com.cqt.monitor.web.switchroom.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cqt.model.monitor.entity.MonitorExecuteInfo;
import org.apache.ibatis.annotations.Mapper;

@Mapper
@DS("ms")
public interface MonitorExecuteInfoMapper extends BaseMapper<MonitorExecuteInfo> {
}
