package com.cqt.monitor.web.switchroom.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cqt.model.monitor.entity.MonitorConfigInfo;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author hlx
 * @date 2022-01-21
 */
@Mapper
@DS("ms")
public interface MonitorConfigInfoMapper extends BaseMapper<MonitorConfigInfo> {
}
