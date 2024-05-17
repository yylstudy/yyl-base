package com.cqt.monitor.web.switchroom.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cqt.model.monitor.entity.PrivateMonitorInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * @author linshiqiang
 * @date 2022/1/21 15:45
 */
@Mapper
@DS("ms")
public interface PrivateMonitorInfoMapper extends BaseMapper<PrivateMonitorInfo> {

    /**
     * 更新状态
     *
     * @param status   状态
     * @param business 业务
     */
    @Update("update private_monitor_info set status = #{status} where business = #{business}")
    void updateStatusByBusiness(@Param("status") Integer status, @Param("business") String business);
}
