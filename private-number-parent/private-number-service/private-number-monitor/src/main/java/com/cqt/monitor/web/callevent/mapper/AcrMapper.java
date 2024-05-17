package com.cqt.monitor.web.callevent.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * @author huweizhong
 * date  2023/10/31 10:09
 */
@DS("smp")
public interface AcrMapper {

    List<Map<String, Object>> getAcrCount(@Param("tableName") String tableName);
}
