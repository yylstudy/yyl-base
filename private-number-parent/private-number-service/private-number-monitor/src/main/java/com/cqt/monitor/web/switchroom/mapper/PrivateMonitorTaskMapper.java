package com.cqt.monitor.web.switchroom.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cqt.model.monitor.entity.PrivateMonitorTask;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author linshiqiang
 * @date 2022/1/25 15:17
 */
@Mapper
@DS("ms")
public interface PrivateMonitorTaskMapper extends BaseMapper<PrivateMonitorTask> {
}
