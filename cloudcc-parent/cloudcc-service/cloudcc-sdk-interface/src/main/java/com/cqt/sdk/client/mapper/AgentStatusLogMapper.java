package com.cqt.sdk.client.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cqt.model.cdr.entity.AgentStatusLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author linshiqiang
 * date:  2023-07-14 18:04
 */
@Mapper
@DS("cdr")
public interface AgentStatusLogMapper extends BaseMapper<AgentStatusLog> {
}
