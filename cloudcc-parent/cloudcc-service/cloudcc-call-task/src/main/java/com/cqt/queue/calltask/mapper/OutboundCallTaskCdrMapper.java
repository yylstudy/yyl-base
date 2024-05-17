package com.cqt.queue.calltask.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cqt.model.calltask.entity.OutboundCallTaskCdr;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author linshiqiang
 * date:  2023-10-24 14:41
 */
@DS("cdr")
@Mapper
public interface OutboundCallTaskCdrMapper extends BaseMapper<OutboundCallTaskCdr> {
}
