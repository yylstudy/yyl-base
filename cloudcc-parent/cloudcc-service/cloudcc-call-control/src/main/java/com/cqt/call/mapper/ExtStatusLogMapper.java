package com.cqt.call.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cqt.model.cdr.entity.ExtStatusLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author linshiqiang
 * date:  2023-07-05 17:58
 */
@Mapper
@DS("cdr")
public interface ExtStatusLogMapper extends BaseMapper<ExtStatusLog> {
}
