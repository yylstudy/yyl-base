package com.cqt.monitor.web.callevent.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cqt.monitor.web.callevent.entity.EventInMin;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.util.Map;

@DS("stats")
@Mapper
public interface EventInMinMapper extends BaseMapper<EventInMin> {

    void insertByCondition(@Param("tableName") String tableName, @Param("eventInMin") EventInMin eventInMin, @Param("id") String id);

    void createTable(@Param("tableName") String tableName);

    Map<String, BigDecimal> getPickupRate(@Param("tableName") String tableName, @Param("time") String time, @Param("vccId") String vccId);


    String getConcurrency(@Param("tableName") String tableName,@Param("time") String time,@Param("vccId") String vccId);
}
