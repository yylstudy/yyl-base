package com.cqt.monitor.web.callevent.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import com.cqt.monitor.web.callevent.entity.AreaTable;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * @Description: 地市管理
 * @Author: jeecg-boot
 * @Date: 2022-05-23
 * @Version: V1.0
 */
@DS("ms")
public interface AreaTableMapper extends BaseMapper<AreaTable> {

    /**
     * 根据城市区号查询城市名称
     *
     * @param areaCode
     * @return
     */
    @Select("select details from t_area where tel_code = #{areaCode}")
    String getAreaName(@Param("areaCode") String areaCode);
}
