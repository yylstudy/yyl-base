package com.cqt.cdr.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.cqt.cdr.entity.PushErr;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
* @author Administrator
* @description 针对表【cc_push_err】的数据库操作Mapper
* @createDate 2023-08-28 16:26:02
* @Entity com.cqt.cdr.entity.PushErr
*/
@Mapper
@DS("ms")
public interface PushErrMapper extends BaseMapper<PushErr> {
    void createTable(String str);
}




